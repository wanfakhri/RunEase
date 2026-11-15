package my.uitm.runease.web.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.EventType;
import my.uitm.runease.model.User;
import my.uitm.runease.service.AnnouncementService;
import my.uitm.runease.service.EventService;
import my.uitm.runease.service.RegistrationService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.util.Page;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/events/*")
public class CatalogServlet extends BaseServlet {

    private static final int PAGE_SIZE = 9;

    private final EventService eventService = new EventService();
    private final RegistrationService registrationService = new RegistrationService();
    private final AnnouncementService announcementService = new AnnouncementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path != null && path.length() > 1 && !path.equals("/")) {
            detail(req, res);
            return;
        }
        list(req, res);
    }

    private void list(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String q = Web.param(req, "q", "");
        String type = Web.param(req, "type", "");
        String state = Web.param(req, "state", "");
        String when = Web.param(req, "when", "upcoming");
        String sort = Web.param(req, "sort", "date");
        String dir = Web.param(req, "dir", "asc");
        int page = Math.max(1, Web.intParam(req, "page", 1));
        boolean upcomingOnly = !"all".equals(when);

        try {
            List<Event> events = eventService.publicEvents(q, type, state, upcomingOnly, sort, dir,
                    (page - 1) * PAGE_SIZE, PAGE_SIZE);
            long total = eventService.countPublicEvents(q, type, state, upcomingOnly);
            Map<String, String> filters = new LinkedHashMap<>();
            filters.put("q", q);
            filters.put("type", type);
            filters.put("state", state);
            filters.put("when", when);
            req.setAttribute("events", events);
            req.setAttribute("pager", new Page<>(events, page, PAGE_SIZE, total, filters, sort, dir));
            req.setAttribute("states", eventService.states());
            req.setAttribute("types", EventType.values());
            req.setAttribute("filters", filters);
            page(req, "Browse events", Crumb.link("Home", "/"), Crumb.of("Events"));
            render(req, res, "catalog");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void detail(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            long eventId = pathId(req);
            Event event = eventService.publicEvent(eventId);
            req.setAttribute("event", event);
            req.setAttribute("announcements", announcementService.forEvent(eventId));
            User user = Web.user(req);
            boolean registered = false;
            if (user != null && user.isParticipant()) {
                registered = registrationService.isRegisteredInEvent(user.getUserId(), eventId);
            }
            req.setAttribute("registered", registered);
            try {
                req.setAttribute("similar", eventService.publicEvents("", event.getEventType().name(),
                        event.getState(), true, "date", "asc", 0, 4));
            } catch (Exception ignored) {
                req.setAttribute("similar", List.of());
            }
            page(req, event.getTitle(), Crumb.link("Home", "/"), Crumb.link("Events", "/events"),
                    Crumb.of(event.getTitle()));
            render(req, res, "event");
        } catch (Exception e) {
            if (e instanceof my.uitm.runease.service.AppException app) {
                throw app;
            }
            throw new ServletException(e);
        }
    }
}
