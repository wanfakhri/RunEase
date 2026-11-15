package my.uitm.runease.web.servlet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.EventCategory;
import my.uitm.runease.model.EventStatus;
import my.uitm.runease.model.EventType;
import my.uitm.runease.model.User;
import my.uitm.runease.service.AppException;
import my.uitm.runease.service.EventService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.util.Page;
import my.uitm.runease.util.Validation;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/organizer/events/*")
public class OrganizerEventServlet extends BaseServlet {

    private static final int PAGE_SIZE = 8;

    private final EventService eventService = new EventService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                list(req, res);
                return;
            }
            if (path.equals("/new")) {
                Event event = new Event();
                event.setEventType(EventType.ROAD);
                event.setRegistrationOpen(LocalDate.now());
                event.setRegistrationClose(LocalDate.now().plusDays(30));
                event.setEventDate(LocalDate.now().plusDays(60));
                req.setAttribute("event", event);
                req.setAttribute("form", formOf(event));
                req.setAttribute("mode", "create");
                req.setAttribute("types", EventType.values());
                page(req, "Create an event", Crumb.link("Events", "/organizer/events"),
                        Crumb.of("Create event"));
                render(req, res, "organizer/event-form");
                return;
            }
            User user = currentUser(req);
            long eventId = pathId(req);
            Event event = eventService.organizerEvent(user.getUserId(), eventId);
            req.setAttribute("event", event);
            req.setAttribute("form", formOf(event));
            req.setAttribute("mode", "edit");
            req.setAttribute("types", EventType.values());
            page(req, event.getTitle(), Crumb.link("Events", "/organizer/events"),
                    Crumb.of(event.getTitle()));
            render(req, res, "organizer/event-form");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException {
        User user = currentUser(req);
        String q = Web.param(req, "q", "");
        String status = Web.param(req, "status", "");
        String sort = Web.param(req, "sort", "date");
        String dir = Web.param(req, "dir", "desc");
        int page = Math.max(1, Web.intParam(req, "page", 1));
        try {
            List<Event> events = eventService.organizerEvents(user.getUserId(), q, status, sort, dir,
                    (page - 1) * PAGE_SIZE, PAGE_SIZE);
            long total = eventService.countOrganizerEvents(user.getUserId(), q, status);
            Map<String, String> filters = new LinkedHashMap<>();
            filters.put("q", q);
            filters.put("status", status);
            req.setAttribute("events", events);
            req.setAttribute("pager", new Page<>(events, page, PAGE_SIZE, total, filters, sort, dir));
            req.setAttribute("filters", filters);
            req.setAttribute("statuses", EventStatus.values());
            page(req, "My events", Crumb.link("Dashboard", "/organizer"), Crumb.of("Events"));
            render(req, res, "organizer/events");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            redirect(req, res, "/organizer/events");
            return;
        }
        String[] segments = path.substring(1).split("/");
        if (segments[0].equals("new")) {
            createOrUpdate(req, res, null);
            return;
        }
        long eventId;
        try {
            eventId = Long.parseLong(segments[0]);
        } catch (NumberFormatException e) {
            throw new AppException("Event not found.", 404);
        }
        if (segments.length == 1) {
            createOrUpdate(req, res, eventId);
            return;
        }
        if (segments[1].equals("submit")) {
            submit(req, res, eventId);
            return;
        }
        if (segments[1].equals("delete")) {
            delete(req, res, eventId);
            return;
        }
        if (segments[1].equals("categories")) {
            if (segments.length == 2) {
                saveCategory(req, res, eventId, null);
            } else if (segments.length >= 4 && segments[3].equals("delete")) {
                deleteCategory(req, res, eventId, segments[2]);
            } else if (segments.length == 3) {
                try {
                    saveCategory(req, res, eventId, Long.parseLong(segments[2]));
                } catch (NumberFormatException e) {
                    throw new AppException("Category not found.", 404);
                }
            } else {
                redirect(req, res, "/organizer/events/" + eventId);
            }
            return;
        }
        redirect(req, res, "/organizer/events/" + eventId);
    }

    private void createOrUpdate(HttpServletRequest req, HttpServletResponse res, Long eventId)
            throws ServletException, java.io.IOException {
        User user = currentUser(req);
        String title = Web.param(req, "title", "");
        String description = Web.param(req, "description", "");
        String eventType = Web.param(req, "eventType", "ROAD");
        String venue = Web.param(req, "venue", "");
        String city = Web.param(req, "city", "");
        String state = Web.param(req, "state", "");
        String eventDate = Web.param(req, "eventDate", "");
        String flagOffTime = Web.param(req, "flagOffTime", "");
        String registrationOpen = Web.param(req, "registrationOpen", "");
        String registrationClose = Web.param(req, "registrationClose", "");

        Validation validation = new Validation();
        validation.required("title", "Event title", title);
        validation.length("title", "Event title", title, 150);
        if (!title.isBlank() && !Validation.EVENT_TITLE.matcher(title).matches()) {
            validation.error("title", "Use 3-150 characters; letters, numbers and basic punctuation only.");
        }
        validation.required("description", "Description", description);
        validation.length("description", "Description", description, 4000);
        if (description.length() > 0 && description.trim().length() < 30) {
            validation.error("description", "Describe the event in at least 30 characters.");
        }
        validation.oneOf("eventType", "Event type", eventType, "ROAD", "TRAIL", "FUN_RUN", "VIRTUAL");
        validation.required("venue", "Venue", venue);
        validation.length("venue", "Venue", venue, 150);
        validation.required("city", "City", city);
        validation.length("city", "City", city, 80);
        validation.required("state", "State", state);
        validation.length("state", "State", state, 80);
        validation.date("eventDate", "Event date", eventDate, true);
        validation.time("flagOffTime", "Flag-off time", flagOffTime, false);
        validation.date("registrationOpen", "Registration opening date", registrationOpen, true);
        validation.date("registrationClose", "Registration closing date", registrationClose, true);
        validation.dateNotBefore("registrationClose", "Registration closing date", registrationClose,
                registrationOpen);
        validation.dateNotBefore("eventDate", "Event date", eventDate, registrationClose);

        if (validation.hasErrors()) {
            Event event = new Event();
            if (eventId != null) {
                event.setEventId(eventId);
            }
            event.setTitle(title);
            event.setDescription(description);
            event.setEventType(EventType.parse(eventType));
            event.setVenue(venue);
            event.setCity(city);
            event.setState(state);
            req.setAttribute("event", event);
            req.setAttribute("errors", validation.getErrors());
            req.setAttribute("form", Web.values(req, "title", "description", "eventType", "venue", "city",
                    "state", "eventDate", "flagOffTime", "registrationOpen", "registrationClose"));
            req.setAttribute("mode", eventId == null ? "create" : "edit");
            req.setAttribute("types", EventType.values());
            page(req, eventId == null ? "Create an event" : "Edit event",
                    Crumb.link("Events", "/organizer/events"), Crumb.of("Event details"));
            render(req, res, "organizer/event-form");
            return;
        }

        try {
            Event event = new Event();
            if (eventId != null) {
                event.setEventId(eventId);
            }
            event.setTitle(title);
            event.setDescription(description);
            event.setEventType(EventType.parse(eventType));
            event.setVenue(venue);
            event.setCity(city);
            event.setState(state);
            event.setEventDate(LocalDate.parse(eventDate));
            event.setFlagOffTime(flagOffTime.isBlank() ? null : LocalTime.parse(flagOffTime));
            event.setRegistrationOpen(LocalDate.parse(registrationOpen));
            event.setRegistrationClose(LocalDate.parse(registrationClose));

            if (eventId == null) {
                long id = eventService.createEvent(user.getUserId(), event, Web.clientIp(req));
                Web.flash(req, "success", "Draft saved. Add your race categories, then submit for approval.");
                redirect(req, res, "/organizer/events/" + id);
            } else {
                eventService.updateEvent(user.getUserId(), event, Web.clientIp(req));
                Web.flash(req, "success", "Event details updated.");
                redirect(req, res, "/organizer/events/" + eventId);
            }
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
            redirect(req, res, eventId == null ? "/organizer/events/new" : "/organizer/events/" + eventId);
        } catch (Exception e) {
            log("Event save failed", e);
            Web.flash(req, "danger", "We could not save this event right now. Please try again.");
            redirect(req, res, eventId == null ? "/organizer/events/new" : "/organizer/events/" + eventId);
        }
    }

    private void submit(HttpServletRequest req, HttpServletResponse res, long eventId)
            throws java.io.IOException {
        User user = currentUser(req);
        try {
            eventService.submitForApproval(user.getUserId(), eventId, Web.clientIp(req));
            Web.flash(req, "success", "Event submitted. An administrator will review it shortly.");
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
        } catch (Exception e) {
            log("Submit event failed", e);
            Web.flash(req, "danger", "We could not submit this event right now.");
        }
        redirect(req, res, "/organizer/events/" + eventId);
    }

    private void delete(HttpServletRequest req, HttpServletResponse res, long eventId)
            throws java.io.IOException {
        User user = currentUser(req);
        try {
            eventService.deleteEvent(user.getUserId(), eventId, Web.clientIp(req));
            Web.flash(req, "success", "Event deleted.");
            redirect(req, res, "/organizer/events");
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
            redirect(req, res, "/organizer/events/" + eventId);
        } catch (Exception e) {
            log("Delete event failed", e);
            Web.flash(req, "danger", "We could not delete this event right now.");
            redirect(req, res, "/organizer/events/" + eventId);
        }
    }

    private void saveCategory(HttpServletRequest req, HttpServletResponse res, long eventId, Long categoryId)
            throws java.io.IOException {
        User user = currentUser(req);
        String name = Web.param(req, "name", "");
        String distanceKm = Web.param(req, "distanceKm", "");
        String price = Web.param(req, "price", "");
        String quota = Web.param(req, "quota", "");
        String startTime = Web.param(req, "startTime", "");

        Validation validation = new Validation();
        validation.required("name", "Category name", name);
        validation.length("name", "Category name", name, 60);
        validation.decimal("distanceKm", "Distance", distanceKm, 0.5, 500);
        validation.money("price", "Price", price, 5000);
        validation.integer("quota", "Quota", quota, 1, 100000);
        validation.time("startTime", "Start time", startTime, false);

        if (validation.hasErrors()) {
            Web.flash(req, "danger", "Category not saved: " + validation.getFirstError());
            redirect(req, res, "/organizer/events/" + eventId);
            return;
        }

        try {
            EventCategory category = new EventCategory();
            category.setEventId(eventId);
            category.setName(name);
            category.setDistanceKm(Double.parseDouble(distanceKm));
            category.setPrice(new BigDecimal(price));
            category.setQuota(Integer.parseInt(quota));
            category.setStartTime(startTime.isBlank() ? null : LocalTime.parse(startTime));
            eventService.saveCategory(user.getUserId(), category, categoryId, Web.clientIp(req));
            Web.flash(req, "success", categoryId == null ? "Race category added." : "Race category updated.");
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
        } catch (Exception e) {
            log("Save category failed", e);
            Web.flash(req, "danger", "We could not save this category right now.");
        }
        redirect(req, res, "/organizer/events/" + eventId);
    }

    private void deleteCategory(HttpServletRequest req, HttpServletResponse res, long eventId, String rawId)
            throws java.io.IOException {
        User user = currentUser(req);
        try {
            long categoryId = Long.parseLong(rawId);
            eventService.deleteCategory(user.getUserId(), categoryId, Web.clientIp(req));
            Web.flash(req, "success", "Race category removed.");
        } catch (NumberFormatException e) {
            Web.flash(req, "danger", "Category not found.");
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
        } catch (Exception e) {
            log("Delete category failed", e);
            Web.flash(req, "danger", "We could not delete this category right now.");
        }
        redirect(req, res, "/organizer/events/" + eventId);
    }

    private Map<String, String> formOf(Event event) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("title", event.getTitle() == null ? "" : event.getTitle());
        form.put("description", event.getDescription() == null ? "" : event.getDescription());
        form.put("eventType", event.getEventType() == null ? "ROAD" : event.getEventType().name());
        form.put("venue", event.getVenue() == null ? "" : event.getVenue());
        form.put("city", event.getCity() == null ? "" : event.getCity());
        form.put("state", event.getState() == null ? "" : event.getState());
        form.put("eventDate", event.getEventDate() == null ? "" : event.getEventDate().toString());
        form.put("flagOffTime", event.getFlagOffTime() == null ? "" : event.getFlagOffTime().toString());
        form.put("registrationOpen",
                event.getRegistrationOpen() == null ? "" : event.getRegistrationOpen().toString());
        form.put("registrationClose",
                event.getRegistrationClose() == null ? "" : event.getRegistrationClose().toString());
        return form;
    }
}
