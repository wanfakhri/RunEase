package my.uitm.runease.web.servlet;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import my.uitm.runease.model.User;
import my.uitm.runease.service.AppException;
import my.uitm.runease.service.AuthService;
import my.uitm.runease.util.Validation;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.CsrfFilter;
import my.uitm.runease.web.Web;

@WebServlet({"/login", "/logout", "/register"})
public class AuthServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String path = Web.currentPath(req);
        if (path.equals("/logout")) {
            redirect(req, res, "/login");
            return;
        }
        User user = Web.user(req);
        if (user != null && path.equals("/login")) {
            redirect(req, res, user.getRole().getHome());
            return;
        }
        if (path.equals("/register")) {
            page(req, "Create an account");
            req.setAttribute("mode", "organizer".equals(Web.param(req, "type")) ? "organizer" : "participant");
            render(req, res, "auth/register");
            return;
        }
        page(req, "Sign in");
        req.setAttribute("prefillEmail", Web.param(req, "email", ""));
        render(req, res, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String path = Web.currentPath(req);
        switch (path) {
            case "/login" -> login(req, res);
            case "/logout" -> logout(req, res);
            case "/register" -> register(req, res);
            default -> res.sendError(404);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String email = Web.param(req, "email", "");
        String password = req.getParameter("password");

        Validation validation = new Validation();
        validation.email("email", email);
        validation.required("password", "Password", password);
        if (validation.hasErrors()) {
            req.setAttribute("errors", validation.getErrors());
            req.setAttribute("form", Map.of("email", email));
            page(req, "Sign in");
            render(req, res, "auth/login");
            return;
        }

        try {
            User user = authService.login(email, password, Web.clientIp(req));
            HttpSession session = req.getSession();
            req.changeSessionId();
            user.setPasswordHash(null);
            session.setAttribute(Web.SESSION_USER, user);
            session.setAttribute(Web.SESSION_CSRF, CsrfFilter.newToken());
            Web.flash(req, "success", "Welcome back, " + user.getFirstName() + ".");
            String next = Web.safeNext(req.getParameter("next"));
            redirect(req, res, next != null ? next : user.getRole().getHome());
        } catch (AppException e) {
            req.setAttribute("loginError", e.getMessage());
            req.setAttribute("form", Map.of("email", email));
            page(req, "Sign in");
            render(req, res, "auth/login");
        } catch (Exception e) {
            log("Login failed", e);
            req.setAttribute("loginError", "We could not sign you in right now. Please try again.");
            req.setAttribute("form", Map.of("email", email));
            page(req, "Sign in");
            render(req, res, "auth/login");
        }
    }

    private void logout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        User user = Web.user(req);
        if (user != null) {
            authService.log(user.getUserId(), "LOGOUT", "USER", String.valueOf(user.getUserId()),
                    user.getFullName() + " signed out", Web.clientIp(req));
        }
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirect(req, res, "/login?logout=1");
    }

    private void register(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String mode = Web.param(req, "mode", "participant");
        boolean organizer = "organizer".equals(mode);

        String fullName = Web.param(req, "fullName", "");
        String email = Web.param(req, "email", "");
        String phone = Web.param(req, "phone", "");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String city = Web.param(req, "city", "");
        String organizationName = Web.param(req, "organizationName", "");
        String registrationNo = Web.param(req, "registrationNo", "");
        String website = Web.param(req, "website", "");

        Validation validation = new Validation();
        validation.name("fullName", "Full name", fullName);
        validation.email("email", email);
        validation.phone("phone", "Phone number", phone, true);
        validation.password("password", password);
        validation.matches("confirmPassword", "Password confirmation", confirmPassword, password);
        validation.required("terms", "Terms and conditions", req.getParameter("terms"));

        if (organizer) {
            validation.required("organizationName", "Organization name", organizationName);
            validation.length("organizationName", "Organization name", organizationName, 150);
            validation.length("registrationNo", "SSM registration number", registrationNo, 50);
            validation.length("website", "Website", website, 160);
        } else {
            validation.length("city", "City", city, 80);
        }

        if (validation.hasErrors()) {
            showRegisterForm(req, res, mode, validation.getErrors());
            return;
        }

        try {
            if (organizer) {
                authService.registerOrganizer(fullName, email, password, phone, organizationName,
                        registrationNo, website, Web.clientIp(req));
                Web.flash(req, "info",
                        "Your organizer application has been submitted. An administrator will verify your account shortly.");
            } else {
                authService.registerParticipant(fullName, email, password, phone, city, Web.clientIp(req));
                Web.flash(req, "success", "Your account is ready. Sign in to register for your first run.");
            }
            redirect(req, res, "/login?email=" + Web.encode(email));
        } catch (AppException e) {
            Validation serverError = new Validation();
            serverError.error("email", e.getMessage());
            showRegisterForm(req, res, mode, serverError.getErrors());
        } catch (Exception e) {
            log("Registration failed", e);
            Validation serverError = new Validation();
            serverError.error("form", "We could not create your account right now. Please try again.");
            showRegisterForm(req, res, mode, serverError.getErrors());
        }
    }

    private void showRegisterForm(HttpServletRequest req, HttpServletResponse res, String mode,
                                  Map<String, String> errors) throws ServletException, IOException {
        req.setAttribute("errors", errors);
        req.setAttribute("mode", mode);
        req.setAttribute("form", Web.values(req,
                "fullName", "email", "phone", "city", "organizationName", "registrationNo", "website"));
        page(req, "Create an account");
        render(req, res, "auth/register");
    }
}
