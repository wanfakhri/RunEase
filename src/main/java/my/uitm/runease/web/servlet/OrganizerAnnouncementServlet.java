package my.uitm.runease.web.servlet;

import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.Announcement;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.User;
import my.uitm.runease.service.AnnouncementService;
import my.uitm.runease.service.AppException;
import my.uitm.runease.service.EventService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.util.Validation;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/organizer/announcements/*")
public class OrganizerAnnouncementServlet extends BaseServlet {

    private final AnnouncementService announcementService = new AnnouncementService();
    private final EventService eventService = new EventService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        User user = currentUser(req);
        Long eventId = Web.longParamOrNull(req, "eventId");
        try {
            List<Announcement> announcements = announcementService.forOrganizer(user.getUserId(), eventId);
            List<Event> events = eventService.organizerEvents(user.getUserId(), "", "APPROVED", "date", "desc",
                    0, 100);
            List<Event> pending = eventService.organizerEvents(user.getUserId(), "", "COMPLETED", "date", "desc",
                    0, 100);
            events.addAll(pending);
            req.setAttribute("announcements", announcements);
            req.setAttribute("organizedEvents", events);
            req.setAttribute("selectedEventId", eventId);
            page(req, "Announcements", Crumb.link("Dashboard", "/organizer"), Crumb.of("Announcements"));
            render(req, res, "organizer/announcements");
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
            create(req, res, user);
            return;
        }
        String[] segments = path.substring(1).split("/");
        long announcementId;
        try {
            announcementId = Long.parseLong(segments[0]);
        } catch (NumberFormatException e) {
            throw new AppException("Announcement not found.", 404);
        }
        if (segments.length >= 2 && segments[1].equals("delete")) {
            delete(req, res, user, announcementId);
            return;
        }
        update(req, res, user, announcementId);
    }

    private void create(HttpServletRequest req, HttpServletResponse res, User user) throws java.io.IOException {
        Long eventId = Web.longParamOrNull(req, "eventId");
        String title = Web.param(req, "title", "");
        String body = Web.param(req, "body", "");
        String audience = Web.param(req, "audience", "ALL");
        Validation validation = new Validation();
        validation.required("eventId", "Event", eventId == null ? null : String.valueOf(eventId));
        validation.required("title", "Title", title);
        validation.length("title", "Title", title, 150);
        validation.required("body", "Message", body);
        validation.length("body", "Message", body, 2000);
        validation.oneOf("audience", "Audience", audience, "ALL", "CONFIRMED");
        if (validation.hasErrors()) {
            Web.flash(req, "danger", "Announcement not published: " + validation.getFirstError());
            redirect(req, res, back(eventId));
            return;
        }
        try {
            announcementService.create(user.getUserId(), eventId, title, body, audience, Web.clientIp(req));
            Web.flash(req, "success", "Announcement published.");
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
        } catch (Exception e) {
            log("Publish announcement failed", e);
            Web.flash(req, "danger", "We could not publish this announcement right now.");
        }
        redirect(req, res, back(eventId));
    }

    private void update(HttpServletRequest req, HttpServletResponse res, User user, long announcementId)
            throws java.io.IOException {
        Long eventId = Web.longParamOrNull(req, "eventId");
        String title = Web.param(req, "title", "");
        String body = Web.param(req, "body", "");
        String audience = Web.param(req, "audience", "ALL");
        Validation validation = new Validation();
        validation.required("title", "Title", title);
        validation.length("title", "Title", title, 150);
        validation.required("body", "Message", body);
        validation.length("body", "Message", body, 2000);
        validation.oneOf("audience", "Audience", audience, "ALL", "CONFIRMED");
        if (validation.hasErrors()) {
            Web.flash(req, "danger", "Announcement not updated: " + validation.getFirstError());
            redirect(req, res, back(eventId));
            return;
        }
        try {
            announcementService.update(user.getUserId(), announcementId, title, body, audience,
                    Web.clientIp(req));
            Web.flash(req, "success", "Announcement updated.");
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
        } catch (Exception e) {
            log("Update announcement failed", e);
            Web.flash(req, "danger", "We could not update this announcement right now.");
        }
        redirect(req, res, back(eventId));
    }

    private void delete(HttpServletRequest req, HttpServletResponse res, User user, long announcementId)
            throws java.io.IOException {
        Long eventId = Web.longParamOrNull(req, "eventId");
        try {
            announcementService.delete(user.getUserId(), announcementId, Web.clientIp(req));
            Web.flash(req, "success", "Announcement deleted.");
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
        } catch (Exception e) {
            log("Delete announcement failed", e);
            Web.flash(req, "danger", "We could not delete this announcement right now.");
        }
        redirect(req, res, back(eventId));
    }

    private String back(Long eventId) {
        return eventId == null ? "/organizer/announcements"
                : "/organizer/announcements?eventId=" + eventId;
    }
}
