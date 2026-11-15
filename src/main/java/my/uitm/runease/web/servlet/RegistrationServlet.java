package my.uitm.runease.web.servlet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.Registration;
import my.uitm.runease.model.User;
import my.uitm.runease.service.AnnouncementService;
import my.uitm.runease.service.AppException;
import my.uitm.runease.service.RegistrationService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.util.Page;
import my.uitm.runease.util.Validation;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/registrations/*")
public class RegistrationServlet extends BaseServlet {

    private static final int PAGE_SIZE = 8;

    private final RegistrationService registrationService = new RegistrationService();
    private final AnnouncementService announcementService = new AnnouncementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        String path = req.getPathInfo();
        if (path != null && path.length() > 1) {
            detail(req, res);
            return;
        }
        list(req, res);
    }

    private void list(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException {
        User user = currentUser(req);
        String status = Web.param(req, "status", "");
        String sort = Web.param(req, "sort", "event");
        String dir = Web.param(req, "dir", "desc");
        int page = Math.max(1, Web.intParam(req, "page", 1));
        try {
            List<Registration> registrations = registrationService.myRegistrations(user.getUserId(), status,
                    sort, dir, (page - 1) * PAGE_SIZE, PAGE_SIZE);
            long total = registrationService.countMyRegistrations(user.getUserId(), status);
            Map<String, String> filters = new LinkedHashMap<>();
            filters.put("status", status);
            req.setAttribute("registrations", registrations);
            req.setAttribute("pager", new Page<>(registrations, page, PAGE_SIZE, total, filters, sort, dir));
            req.setAttribute("filters", filters);
            page(req, "My registrations", Crumb.link("Dashboard", "/dashboard"), Crumb.of("Registrations"));
            render(req, res, "participant/registrations");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void detail(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException {
        User user = currentUser(req);
        try {
            long id = pathId(req);
            Registration registration = registrationService.myRegistration(user.getUserId(), id);
            req.setAttribute("registration", registration);
            req.setAttribute("announcements", announcementService.forEvent(registration.getEventId()));
            page(req, "Registration " + registration.getRegistrationCode(),
                    Crumb.link("Dashboard", "/dashboard"), Crumb.link("Registrations", "/registrations"),
                    Crumb.of(registration.getRegistrationCode()));
            render(req, res, "participant/registration-detail");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            redirect(req, res, "/registrations");
            return;
        }
        User user = currentUser(req);
        long id;
        try {
            id = pathId(req);
        } catch (AppException e) {
            throw e;
        }
        String action = Web.param(req, "action", "");
        if ("pay".equals(action)) {
            String method = Web.param(req, "method", "FPX");
            Validation validation = new Validation();
            validation.oneOf("method", "Payment method", method, "FPX", "CARD", "EWALLET");
            if (validation.hasErrors()) {
                Web.flash(req, "danger", validation.getFirstError());
                redirect(req, res, "/registrations/" + id);
                return;
            }
            try {
                Registration registration = registrationService.pay(id, user.getUserId(), method, Web.clientIp(req));
                Web.flash(req, "success", "Payment received. Your bib number is "
                        + registration.getBibDisplay() + ". See you at the start line!");
            } catch (AppException e) {
                Web.flash(req, "danger", e.getMessage());
            } catch (Exception e) {
                log("Payment failed", e);
                Web.flash(req, "danger", "We could not process the payment right now. Please try again.");
            }
            redirect(req, res, "/registrations/" + id);
            return;
        }
        if ("cancel".equals(action)) {
            try {
                registrationService.cancel(id, user.getUserId(), Web.clientIp(req));
                Web.flash(req, "success", "Your registration has been cancelled. Any payment will be refunded.");
            } catch (AppException e) {
                Web.flash(req, "danger", e.getMessage());
            } catch (Exception e) {
                log("Cancellation failed", e);
                Web.flash(req, "danger", "We could not cancel this registration right now.");
            }
            redirect(req, res, "/registrations/" + id);
            return;
        }
        redirect(req, res, "/registrations/" + id);
    }
}
