package my.uitm.runease.web.servlet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.AccountStatus;
import my.uitm.runease.model.AuditLog;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.EventStatus;
import my.uitm.runease.model.Organizer;
import my.uitm.runease.model.Registration;
import my.uitm.runease.model.RegistrationStatus;
import my.uitm.runease.model.Role;
import my.uitm.runease.model.User;
import my.uitm.runease.model.VerificationStatus;
import my.uitm.runease.service.AppException;
import my.uitm.runease.service.AuditService;
import my.uitm.runease.service.AuthService;
import my.uitm.runease.service.EventService;
import my.uitm.runease.service.RegistrationService;
import my.uitm.runease.service.ReportService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.util.Page;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/admin/*")
public class AdminServlet extends BaseServlet {

    private static final int PAGE_SIZE = 10;

    private final ReportService reportService = new ReportService();
    private final EventService eventService = new EventService();
    private final AuthService authService = new AuthService();
    private final AuditService auditService = new AuditService();
    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                dashboard(req, res);
                return;
            }
            String[] segments = path.substring(1).split("/");
            switch (segments[0]) {
                case "events" -> {
                    if (segments.length >= 2) {
                        review(req, res, parseId(segments[1]));
                    } else {
                        events(req, res);
                    }
                }
                case "users" -> users(req, res);
                case "organizers" -> organizers(req, res);
                case "audit" -> audit(req, res);
                case "reports" -> reports(req, res);
                default -> throw new AppException("Page not found.", 404);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void dashboard(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException, Exception {
        Map<String, Object> stats = reportService.adminOverview();
        req.setAttribute("stats", stats);
        req.setAttribute("pendingApprovals", eventService.pendingApprovals());
        req.setAttribute("pendingOrganizers", authService.listOrganizers("PENDING"));
        page(req, "Admin dashboard", Crumb.of("Dashboard"));
        render(req, res, "admin/dashboard");
    }

    private void events(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException, Exception {
        String q = Web.param(req, "q", "");
        String status = Web.param(req, "status", "");
        String sort = Web.param(req, "sort", "created");
        String dir = Web.param(req, "dir", "desc");
        int page = Math.max(1, Web.intParam(req, "page", 1));
        List<Event> events = eventService.allEvents(q, status, sort, dir, (page - 1) * PAGE_SIZE, PAGE_SIZE);
        long total = eventService.countAllEvents(q, status);
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("q", q);
        filters.put("status", status);
        req.setAttribute("events", events);
        req.setAttribute("pager", new Page<>(events, page, PAGE_SIZE, total, filters, sort, dir));
        req.setAttribute("filters", filters);
        req.setAttribute("statuses", EventStatus.values());
        req.setAttribute("pendingApprovals", eventService.pendingApprovals());
        page(req, "Event moderation", Crumb.link("Dashboard", "/admin"), Crumb.of("Events"));
        render(req, res, "admin/events");
    }

    private void review(HttpServletRequest req, HttpServletResponse res, long eventId)
            throws ServletException, java.io.IOException, Exception {
        Event event = eventService.eventWithCategories(eventId);
        req.setAttribute("event", event);
        page(req, "Review event", Crumb.link("Dashboard", "/admin"), Crumb.link("Events", "/admin/events"),
                Crumb.of(event.getTitle()));
        render(req, res, "admin/event-review");
    }

    private void users(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        req.setAttribute("roles", Role.values());
        req.setAttribute("statuses", AccountStatus.values());
        page(req, "User management", Crumb.link("Dashboard", "/admin"), Crumb.of("Users"));
        render(req, res, "admin/users");
    }

    private void organizers(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException, Exception {
        String status = Web.param(req, "status", "");
        req.setAttribute("organizers", authService.listOrganizers(status));
        req.setAttribute("filters", Map.of("status", status));
        req.setAttribute("statuses", VerificationStatus.values());
        page(req, "Organizer verification", Crumb.link("Dashboard", "/admin"), Crumb.of("Organizers"));
        render(req, res, "admin/organizers");
    }

    private void audit(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException, Exception {
        String q = Web.param(req, "q", "");
        String action = Web.param(req, "action", "");
        int page = Math.max(1, Web.intParam(req, "page", 1));
        Page<AuditLog> pager = auditService.page(q, action, page, 15);
        req.setAttribute("pager", pager);
        req.setAttribute("logs", pager.getItems());
        req.setAttribute("filters", pager.getFilterQuery());
        req.setAttribute("q", q);
        req.setAttribute("action", action);
        req.setAttribute("actions",
                List.of("LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", "APPROVE", "REJECT", "CHECK_IN",
                        "PAYMENT", "SUBMIT"));
        page(req, "Audit trail", Crumb.link("Dashboard", "/admin"), Crumb.of("Audit trail"));
        render(req, res, "admin/audit");
    }

    private void reports(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException, Exception {
        String q = Web.param(req, "q", "");
        String status = Web.param(req, "status", "");
        String sort = Web.param(req, "sort", "registered");
        String dir = Web.param(req, "dir", "desc");
        int page = Math.max(1, Web.intParam(req, "page", 1));
        List<Registration> registrations = registrationService.allRegistrations(q, status, sort, dir,
                (page - 1) * 15, 15);
        long total = registrationService.countAllRegistrations(q, status);
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("q", q);
        filters.put("status", status);
        req.setAttribute("registrations", registrations);
        req.setAttribute("pager", new Page<>(registrations, page, 15, total, filters, sort, dir));
        req.setAttribute("filters", filters);
        req.setAttribute("statuses", RegistrationStatus.values());
        req.setAttribute("stats", reportService.adminOverview());
        page(req, "Platform reports", Crumb.link("Dashboard", "/admin"), Crumb.of("Reports"));
        render(req, res, "admin/reports");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        User admin = currentUser(req);
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            redirect(req, res, "/admin");
            return;
        }
        String[] segments = path.substring(1).split("/");
        if (segments[0].equals("events") && segments.length >= 3) {
            long eventId = parseId(segments[1]);
            if (segments[2].equals("approve")) {
                try {
                    eventService.approveEvent(admin.getUserId(), eventId, Web.clientIp(req));
                    Web.flash(req, "success", "Event approved and published to the public catalogue.");
                } catch (AppException e) {
                    Web.flash(req, "danger", e.getMessage());
                } catch (Exception e) {
                    log("Approve event failed", e);
                    Web.flash(req, "danger", "The event could not be approved right now.");
                }
                redirect(req, res, "/admin/events/" + eventId);
                return;
            }
            if (segments[2].equals("reject")) {
                try {
                    eventService.rejectEvent(admin.getUserId(), eventId, Web.param(req, "reason", ""),
                            Web.clientIp(req));
                    Web.flash(req, "success", "Event rejected. The organizer has been notified with your reason.");
                } catch (AppException e) {
                    Web.flash(req, "danger", e.getMessage());
                } catch (Exception e) {
                    log("Reject event failed", e);
                    Web.flash(req, "danger", "The event could not be rejected right now.");
                }
                redirect(req, res, "/admin/events/" + eventId);
                return;
            }
        }
        if (segments[0].equals("organizers") && segments.length >= 3) {
            long organizerId = parseId(segments[1]);
            VerificationStatus status = VerificationStatus.parse(Web.param(req, "status", "VERIFIED"));
            try {
                authService.verifyOrganizer(organizerId, status, admin.getUserId(), Web.clientIp(req));
                Web.flash(req, "success", "Organizer " + status.getLabel().toLowerCase() + ".");
            } catch (AppException e) {
                Web.flash(req, "danger", e.getMessage());
            } catch (Exception e) {
                log("Organizer verification failed", e);
                Web.flash(req, "danger", "The organizer status could not be updated right now.");
            }
            redirect(req, res, "/admin/organizers");
            return;
        }
        redirect(req, res, "/admin");
    }

    private long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new AppException("Record not found.", 404);
        }
    }
}
