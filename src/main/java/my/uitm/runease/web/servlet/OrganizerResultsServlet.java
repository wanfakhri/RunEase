package my.uitm.runease.web.servlet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.Registration;
import my.uitm.runease.model.RegistrationStatus;
import my.uitm.runease.model.User;
import my.uitm.runease.service.AppException;
import my.uitm.runease.service.EventService;
import my.uitm.runease.service.RegistrationService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.util.Page;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/organizer/results/*")
public class OrganizerResultsServlet extends BaseServlet {

    private static final int PAGE_SIZE = 10;

    private final RegistrationService registrationService = new RegistrationService();
    private final EventService eventService = new EventService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        User user = currentUser(req);
        Long eventId = Web.longParamOrNull(req, "eventId");
        String q = Web.param(req, "q", "");
        String status = Web.param(req, "status", "");
        String sort = Web.param(req, "sort", "name");
        String dir = Web.param(req, "dir", "asc");
        int page = Math.max(1, Web.intParam(req, "page", 1));
        try {
            List<Registration> registrations = registrationService.organizerRegistrations(user.getUserId(),
                    eventId, q, status, sort, dir, (page - 1) * PAGE_SIZE, PAGE_SIZE);
            long total = registrationService.countOrganizerRegistrations(user.getUserId(), eventId, q, status);
            Map<String, String> filters = new LinkedHashMap<>();
            filters.put("eventId", eventId == null ? "" : String.valueOf(eventId));
            filters.put("q", q);
            filters.put("status", status);
            req.setAttribute("registrations", registrations);
            req.setAttribute("pager", new Page<>(registrations, page, PAGE_SIZE, total, filters, sort, dir));
            req.setAttribute("filters", filters);
            req.setAttribute("statuses", RegistrationStatus.values());
            List<Event> events = eventService.organizerEvents(user.getUserId(), "", "", "date", "desc", 0, 100);
            req.setAttribute("organizedEvents", events);
            req.setAttribute("selectedEventId", eventId);
            page(req, "Results", Crumb.link("Dashboard", "/organizer"), Crumb.of("Results"));
            render(req, res, "organizer/results");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        User user = currentUser(req);
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            redirect(req, res, "/organizer/results");
            return;
        }
        long registrationId;
        try {
            registrationId = Long.parseLong(path.substring(1).split("/")[0]);
        } catch (NumberFormatException e) {
            throw new AppException("Participant not found.", 404);
        }
        String action = Web.param(req, "action", "save");
        try {
            if ("delete".equals(action)) {
                registrationService.deleteResult(registrationId, user.getUserId(), Web.clientIp(req));
                Web.flash(req, "success", "Result removed.");
            } else {
                String duration = Web.param(req, "duration", "");
                registrationService.saveResult(registrationId, duration, user.getUserId(), Web.clientIp(req));
                Web.flash(req, "success", "Finish time saved and rankings recalculated.");
            }
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
        } catch (Exception e) {
            log("Result save failed", e);
            Web.flash(req, "danger", "We could not save this result right now.");
        }
        String returnTo = Web.safeNext(req.getParameter("returnTo"));
        redirect(req, res, returnTo == null ? "/organizer/results" : returnTo);
    }
}
