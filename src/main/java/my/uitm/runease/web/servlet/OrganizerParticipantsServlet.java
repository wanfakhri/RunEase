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

@WebServlet("/organizer/participants/*")
public class OrganizerParticipantsServlet extends BaseServlet {

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
        String sort = Web.param(req, "sort", "registered");
        String dir = Web.param(req, "dir", "desc");
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
            page(req, "Participants", Crumb.link("Dashboard", "/organizer"), Crumb.of("Participants"));
            render(req, res, "organizer/participants");
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
        String rawId = null;
        if (path != null && path.length() > 1) {
            String[] segments = path.substring(1).split("/");
            if (segments.length >= 2 && segments[1].equals("check-in")) {
                rawId = segments[0];
            }
        }
        if (rawId == null) {
            rawId = Web.param(req, "registrationId", null);
        }
        String returnTo = Web.safeNext(req.getParameter("returnTo"));
        if (returnTo == null) {
            returnTo = "/organizer/participants";
        }
        if (rawId == null) {
            Web.flash(req, "danger", "No participant was selected.");
            redirect(req, res, returnTo);
            return;
        }
        try {
            long id = Long.parseLong(rawId);
            Registration registration = registrationService.checkIn(id, user.getUserId(), Web.clientIp(req));
            Web.flash(req, "success", registration.getParticipantName() + " checked in successfully.");
        } catch (NumberFormatException e) {
            Web.flash(req, "danger", "Participant not found.");
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
        } catch (Exception e) {
            log("Check-in failed", e);
            Web.flash(req, "danger", "Check-in could not be completed right now.");
        }
        redirect(req, res, returnTo);
    }
}
