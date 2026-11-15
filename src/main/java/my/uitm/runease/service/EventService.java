package my.uitm.runease.service;

import java.sql.Connection;
import java.util.List;

import my.uitm.runease.config.Db;
import my.uitm.runease.dao.AuditDao;
import my.uitm.runease.dao.CategoryDao;
import my.uitm.runease.dao.EventDao;
import my.uitm.runease.dao.OrganizerDao;
import my.uitm.runease.dao.StatsDao;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.EventCategory;
import my.uitm.runease.model.EventStatus;
import my.uitm.runease.model.Organizer;
import my.uitm.runease.model.VerificationStatus;

public class EventService {

    private final EventDao eventDao = new EventDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final OrganizerDao organizerDao = new OrganizerDao();
    private final StatsDao statsDao = new StatsDao();
    private final AuditDao auditDao = new AuditDao();

    public List<Event> publicEvents(String q, String type, String state, boolean upcomingOnly,
                                    String sort, String dir, int offset, int limit) throws Exception {
        return Db.read(c -> {
            List<Event> events = eventDao.listPublic(c, q, type, state, upcomingOnly, sort, dir, offset, limit);
            for (Event event : events) {
                event.setCategories(categoryDao.listByEvent(c, event.getEventId()));
            }
            return events;
        });
    }

    public long countPublicEvents(String q, String type, String state, boolean upcomingOnly) throws Exception {
        return Db.read(c -> eventDao.countPublic(c, q, type, state, upcomingOnly));
    }

    public List<Event> featuredEvents(int limit) throws Exception {
        return Db.read(c -> {
            List<Event> events = eventDao.featured(c, limit);
            for (Event event : events) {
                event.setCategories(categoryDao.listByEvent(c, event.getEventId()));
            }
            return events;
        });
    }

    public Event publicEvent(long eventId) throws Exception {
        return Db.read(c -> {
            Event event = eventDao.findById(c, eventId);
            if (event == null || !event.getStatus().isPubliclyVisible()) {
                throw new AppException("The event you are looking for is not available.", 404);
            }
            event.setCategories(categoryDao.listByEvent(c, eventId));
            return event;
        });
    }

    public Event eventWithCategories(long eventId) throws Exception {
        return Db.read(c -> {
            Event event = eventDao.findById(c, eventId);
            if (event == null) {
                throw new AppException("Event not found.", 404);
            }
            event.setCategories(categoryDao.listByEvent(c, eventId));
            return event;
        });
    }

    public Organizer organizerForUser(long userId) throws Exception {
        return Db.read(c -> organizerDao.findByUserId(c, userId));
    }

    public Event organizerEvent(long userId, long eventId) throws Exception {
        return Db.read(c -> {
            Organizer organizer = organizerDao.findByUserId(c, userId);
            if (organizer == null) {
                throw new AppException("Organizer profile not found.", 403);
            }
            Event event = eventDao.findByIdForOrganizer(c, eventId, organizer.getOrganizerId());
            if (event == null) {
                throw new AppException("Event not found.", 404);
            }
            event.setCategories(categoryDao.listByEvent(c, eventId));
            return event;
        });
    }

    public List<Event> organizerEvents(long userId, String q, String status, String sort, String dir,
                                       int offset, int limit) throws Exception {
        return Db.read(c -> {
            Organizer organizer = requireOrganizer(c, userId);
            List<Event> events = eventDao.listByOrganizer(c, organizer.getOrganizerId(), q, status, sort, dir,
                    offset, limit);
            for (Event event : events) {
                event.setCategories(categoryDao.listByEvent(c, event.getEventId()));
            }
            return events;
        });
    }

    public List<Event> allEvents(String q, String status, String sort, String dir, int offset, int limit)
            throws Exception {
        return Db.read(c -> {
            List<Event> events = eventDao.listAll(c, q, status, sort, dir, offset, limit);
            for (Event event : events) {
                event.setCategories(categoryDao.listByEvent(c, event.getEventId()));
            }
            return events;
        });
    }

    public long countAllEvents(String q, String status) throws Exception {
        return Db.read(c -> eventDao.countAllFiltered(c, q, status));
    }

    public long countOrganizerEvents(long userId, String q, String status) throws Exception {
        return Db.read(c -> {
            Organizer organizer = requireOrganizer(c, userId);
            return eventDao.countByOrganizer(c, organizer.getOrganizerId(), q, status);
        });
    }

    public long createEvent(long userId, Event event, String ip) throws Exception {
        return Db.transaction(c -> {
            Organizer organizer = requireVerifiedOrganizer(c, userId);
            event.setOrganizerId(organizer.getOrganizerId());
            event.setStatus(EventStatus.DRAFT);
            event.setSlug(Slug.of(event.getTitle()) + "-" + (1000 + (int) (Math.random() * 9000)));
            long id = eventDao.insert(c, event);
            auditDao.log(c, userId, "CREATE", "EVENT", String.valueOf(id),
                    "Created event " + event.getTitle(), ip);
            return id;
        });
    }

    public void updateEvent(long userId, Event event, String ip) throws Exception {
        Db.transaction(c -> {
            Organizer organizer = requireVerifiedOrganizer(c, userId);
            Event existing = eventDao.findByIdForOrganizer(c, event.getEventId(), organizer.getOrganizerId());
            if (existing == null) {
                throw new AppException("Event not found.", 404);
            }
            if (existing.getStatus() == EventStatus.COMPLETED || existing.getStatus() == EventStatus.CANCELLED) {
                throw new AppException("Completed or cancelled events can no longer be edited.");
            }
            event.setStatus(existing.getStatus());
            event.setOrganizerId(organizer.getOrganizerId());
            event.setSlug(existing.getSlug());
            eventDao.update(c, event);
            auditDao.log(c, userId, "UPDATE", "EVENT", String.valueOf(event.getEventId()),
                    "Updated event " + event.getTitle(), ip);
            return null;
        });
    }

    public void submitForApproval(long userId, long eventId, String ip) throws Exception {
        Db.transaction(c -> {
            Organizer organizer = requireVerifiedOrganizer(c, userId);
            Event event = eventDao.findByIdForOrganizer(c, eventId, organizer.getOrganizerId());
            if (event == null) {
                throw new AppException("Event not found.", 404);
            }
            if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.REJECTED) {
                throw new AppException("Only draft or rejected events can be submitted for approval.");
            }
            long quota = categoryDao.totalQuota(c, eventId);
            if (quota <= 0) {
                throw new AppException("Add at least one race category before submitting the event.");
            }
            eventDao.updateStatus(c, eventId, EventStatus.PENDING, null);
            auditDao.log(c, userId, "SUBMIT", "EVENT", String.valueOf(eventId),
                    "Submitted event for approval: " + event.getTitle(), ip);
            return null;
        });
    }

    public void deleteEvent(long userId, long eventId, String ip) throws Exception {
        Db.transaction(c -> {
            Organizer organizer = requireVerifiedOrganizer(c, userId);
            Event event = eventDao.findByIdForOrganizer(c, eventId, organizer.getOrganizerId());
            if (event == null) {
                throw new AppException("Event not found.", 404);
            }
            long registrations = statsDao.countRegistrationsForEvent(c, eventId);
            if (registrations > 0) {
                throw new AppException("This event already has " + registrations
                        + " registration(s) and cannot be deleted. Cancel the event instead.");
            }
            eventDao.softDelete(c, eventId);
            auditDao.log(c, userId, "DELETE", "EVENT", String.valueOf(eventId),
                    "Deleted event " + event.getTitle(), ip);
            return null;
        });
    }

    public void approveEvent(long adminId, long eventId, String ip) throws Exception {
        Db.transaction(c -> {
            Event event = eventDao.findById(c, eventId);
            if (event == null) {
                throw new AppException("Event not found.", 404);
            }
            if (event.getStatus() != EventStatus.PENDING) {
                throw new AppException("Only events pending review can be approved.");
            }
            eventDao.updateStatus(c, eventId, EventStatus.APPROVED, null);
            auditDao.log(c, adminId, "APPROVE", "EVENT", String.valueOf(eventId),
                    "Approved event " + event.getTitle(), ip);
            return null;
        });
    }

    public void rejectEvent(long adminId, long eventId, String reason, String ip) throws Exception {
        if (reason == null || reason.trim().length() < 10) {
            throw new AppException("Please provide a rejection reason of at least 10 characters.");
        }
        Db.transaction(c -> {
            Event event = eventDao.findById(c, eventId);
            if (event == null) {
                throw new AppException("Event not found.", 404);
            }
            if (event.getStatus() != EventStatus.PENDING) {
                throw new AppException("Only events pending review can be rejected.");
            }
            eventDao.updateStatus(c, eventId, EventStatus.REJECTED, reason.trim());
            auditDao.log(c, adminId, "REJECT", "EVENT", String.valueOf(eventId),
                    "Rejected event " + event.getTitle() + ": " + reason.trim(), ip);
            return null;
        });
    }

    public void saveCategory(long userId, EventCategory category, Long categoryId, String ip) throws Exception {
        Db.transaction(c -> {
            Organizer organizer = requireVerifiedOrganizer(c, userId);
            Event event = eventDao.findByIdForOrganizer(c, category.getEventId(), organizer.getOrganizerId());
            if (event == null) {
                throw new AppException("Event not found.", 404);
            }
            if (categoryId == null) {
                try {
                    long id = categoryDao.insert(c, category);
                    auditDao.log(c, userId, "CREATE", "CATEGORY", String.valueOf(id),
                            "Added category " + category.getName() + " to " + event.getTitle(), ip);
                } catch (java.sql.SQLException e) {
                    throw new AppException("A category with this name already exists for the event.");
                }
            } else {
                EventCategory existing = categoryDao.findById(c, categoryId);
                if (existing == null || existing.getEventId() != event.getEventId()) {
                    throw new AppException("Category not found.", 404);
                }
                long active = categoryDao.activeCount(c, categoryId);
                if (category.getQuota() < active) {
                    throw new AppException("Quota cannot be lower than the " + active + " active registration(s).");
                }
                category.setCategoryId(categoryId);
                categoryDao.update(c, category);
                auditDao.log(c, userId, "UPDATE", "CATEGORY", String.valueOf(categoryId),
                        "Updated category " + category.getName() + " for " + event.getTitle(), ip);
            }
            return null;
        });
    }

    public void deleteCategory(long userId, long categoryId, String ip) throws Exception {
        Db.transaction(c -> {
            Organizer organizer = requireVerifiedOrganizer(c, userId);
            EventCategory category = categoryDao.findById(c, categoryId);
            if (category == null) {
                throw new AppException("Category not found.", 404);
            }
            Event event = eventDao.findByIdForOrganizer(c, category.getEventId(), organizer.getOrganizerId());
            if (event == null) {
                throw new AppException("You cannot modify this category.", 403);
            }
            long active = categoryDao.activeCount(c, categoryId);
            if (active > 0) {
                throw new AppException("This category has " + active
                        + " registration(s) and cannot be deleted.");
            }
            categoryDao.delete(c, categoryId);
            auditDao.log(c, userId, "DELETE", "CATEGORY", String.valueOf(categoryId),
                    "Deleted category " + category.getName() + " from " + event.getTitle(), ip);
            return null;
        });
    }

    public List<Event> pendingApprovals() throws Exception {
        return Db.read(c -> {
            List<Event> events = eventDao.listForApproval(c);
            for (Event event : events) {
                event.setCategories(categoryDao.listByEvent(c, event.getEventId()));
            }
            return events;
        });
    }

    public List<String> states() throws Exception {
        return Db.read(eventDao::listStates);
    }

    private Organizer requireOrganizer(Connection c, long userId) throws Exception {
        Organizer organizer = organizerDao.findByUserId(c, userId);
        if (organizer == null) {
            throw new AppException("Organizer profile not found.", 403);
        }
        return organizer;
    }

    private Organizer requireVerifiedOrganizer(Connection c, long userId) throws Exception {
        Organizer organizer = requireOrganizer(c, userId);
        if (organizer.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new AppException("Your organizer account has not been verified yet.", 403);
        }
        return organizer;
    }
}
