package my.uitm.runease.web.servlet;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import my.uitm.runease.model.Organizer;
import my.uitm.runease.model.User;
import my.uitm.runease.service.AppException;
import my.uitm.runease.service.AuthService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.util.Validation;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/profile")
public class ProfileServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        User user = currentUser(req);
        try {
            User fresh = authService.findById(user.getUserId());
            if (fresh == null) {
                req.getSession().invalidate();
                redirect(req, res, "/login");
                return;
            }
            fresh.setPasswordHash(null);
            HttpSession session = req.getSession();
            session.setAttribute(Web.SESSION_USER, fresh);
            req.setAttribute("account", fresh);
            Map<String, String> form = new LinkedHashMap<>();
            form.put("fullName", fresh.getFullName() == null ? "" : fresh.getFullName());
            form.put("phone", fresh.getPhone() == null ? "" : fresh.getPhone());
            form.put("gender", fresh.getGender() == null ? "" : fresh.getGender());
            form.put("dateOfBirth", fresh.getDateOfBirth() == null ? "" : fresh.getDateOfBirth().toString());
            form.put("city", fresh.getCity() == null ? "" : fresh.getCity());
            req.setAttribute("form", form);
            if (fresh.isOrganizer()) {
                req.setAttribute("organizer", authService.organizerProfile(fresh.getUserId()));
            }
            page(req, "Account settings", Crumb.of("Account settings"));
            render(req, res, "account/profile");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        User user = currentUser(req);
        String fullName = Web.param(req, "fullName", "");
        String phone = Web.param(req, "phone", "");
        String gender = Web.param(req, "gender", "");
        String dateOfBirth = Web.param(req, "dateOfBirth", "");
        String city = Web.param(req, "city", "");

        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");
        boolean changingPassword = (newPassword != null && !newPassword.isBlank())
                || (currentPassword != null && !currentPassword.isBlank());

        Validation validation = new Validation();
        validation.name("fullName", "Full name", fullName);
        validation.phone("phone", "Phone number", phone, true);
        if (!gender.isBlank()) {
            validation.oneOf("gender", "Gender", gender, "MALE", "FEMALE");
        }
        validation.date("dateOfBirth", "Date of birth", dateOfBirth, false);
        validation.length("city", "City", city, 80);
        if (!dateOfBirth.isBlank()) {
            try {
                if (LocalDate.parse(dateOfBirth).isAfter(LocalDate.now())) {
                    validation.error("dateOfBirth", "Date of birth cannot be in the future.");
                }
            } catch (Exception ignored) {
                // already reported by date()
            }
        }
        if (changingPassword) {
            validation.required("currentPassword", "Current password", currentPassword);
            validation.password("newPassword", newPassword);
            validation.matches("confirmPassword", "New password confirmation", confirmPassword, newPassword);
        }

        if (validation.hasErrors()) {
            showForm(req, res, user, validation);
            return;
        }

        try {
            LocalDate dob = dateOfBirth.isBlank() ? null : LocalDate.parse(dateOfBirth);
            User updated = authService.updateProfile(user.getUserId(), fullName, phone,
                    gender.isBlank() ? null : gender, dob, city, Web.clientIp(req));
            updated.setPasswordHash(null);
            req.getSession().setAttribute(Web.SESSION_USER, updated);
            if (changingPassword) {
                authService.changePassword(user.getUserId(), currentPassword, newPassword, Web.clientIp(req));
                Web.flash(req, "success", "Profile and password updated successfully.");
            } else {
                Web.flash(req, "success", "Profile updated successfully.");
            }
            redirect(req, res, "/profile");
        } catch (AppException e) {
            Validation server = new Validation();
            server.error("currentPassword", e.getMessage());
            showForm(req, res, user, server);
        } catch (Exception e) {
            log("Profile update failed", e);
            Validation server = new Validation();
            server.error("form", "We could not save your profile right now. Please try again.");
            showForm(req, res, user, server);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse res, User user, Validation validation)
            throws ServletException, java.io.IOException {
        try {
            User fresh = authService.findById(user.getUserId());
            if (fresh != null) {
                fresh.setPasswordHash(null);
                req.getSession().setAttribute(Web.SESSION_USER, fresh);
                user = fresh;
            }
            req.setAttribute("account", user);
            if (user.isOrganizer()) {
                Organizer organizer = authService.organizerProfile(user.getUserId());
                req.setAttribute("organizer", organizer);
            }
            req.setAttribute("errors", validation.getErrors());
            Map<String, String> form = new LinkedHashMap<>();
            form.put("fullName", Web.param(req, "fullName", user.getFullName()));
            form.put("phone", Web.param(req, "phone", user.getPhone()));
            form.put("gender", Web.param(req, "gender", user.getGender()));
            form.put("dateOfBirth", Web.param(req, "dateOfBirth",
                    user.getDateOfBirth() == null ? "" : user.getDateOfBirth().toString()));
            form.put("city", Web.param(req, "city", user.getCity()));
            req.setAttribute("form", form);
            page(req, "Account settings", Crumb.of("Account settings"));
            render(req, res, "account/profile");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
