package my.uitm.runease.service;

import java.sql.Connection;
import java.util.List;

import my.uitm.runease.config.Db;
import my.uitm.runease.dao.AnnouncementDao;
import my.uitm.runease.dao.AuditDao;
import my.uitm.runease.dao.CategoryDao;
import my.uitm.runease.dao.EventDao;
import my.uitm.runease.dao.OrganizerDao;
import my.uitm.runease.model.Announcement;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.Organizer;

public class AnnouncementService {

    private final AnnouncementDao announcementDao = new AnnouncementDao();
    private final EventDao eventDao = new EventDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final OrganizerDao organizerDao = new OrganizerDao();
    private final AuditDao auditDao = new AuditDao();

    public List<Announcement> forEvent(long eventId) throws Exception {
        return Db.read(c -> announcementDao.listByEvent(c, eventId, false));
    }

    public List<Announcement> forParticipant(long userId) throws Exception {
        return Db.read(c -> announcementDao.listForParticipant(c, userId));
    }

    public List<Announcement> forOrganizer(long userId, Long eventId) throws Exception {
        return Db.read(c -> {
            Organizer organizer = organizerDao.findByUserId(c, userId);
            if (organizer == null) {
                throw new AppException("Organizer profile not found.", 403);
            }
            return announcementDao.listByOrganizer(c, organizer.getOrganizerId(), eventId);
        });
    }

    public void create(long userId, long eventId, String title, String body, String audience, String ip)
            throws Exception {
        Db.transaction(c -> {
            Organizer organizer = requireOrganizer(c, userId);
            Event event = eventDao.findByIdForOrganizer(c, eventId, organizer.getOrganizerId());
            if (event == null) {
                throw new AppException("Event not found.", 404);
            }
            Announcement announcement = new Announcement();
            announcement.setEventId(eventId);
            announcement.setTitle(title);
            announcement.setBody(body);
            announcement.setAudience("CONFIRMED".equals(audience) ? "CONFIRMED" : "ALL");
            announcement.setCreatedBy(userId);
            long id = announcementDao.insert(c, announcement);
            auditDao.log(c, userId, "CREATE", "ANNOUNCEMENT", String.valueOf(id),
                    "Published announcement '" + title + "' for " + event.getTitle(), ip);
            return null;
        });
    }

    public void update(long userId, long announcementId, String title, String body, String audience, String ip)
            throws Exception {
        Db.transaction(c -> {
            Organizer organizer = requireOrganizer(c, userId);
            Announcement announcement = announcementDao.findForOrganizer(c, announcementId, organizer.getOrganizerId());
            if (announcement == null) {
                throw new AppException("Announcement not found.", 404);
            }
            announcement.setTitle(title);
            announcement.setBody(body);
            announcement.setAudience("CONFIRMED".equals(audience) ? "CONFIRMED" : "ALL");
            announcementDao.update(c, announcement);
            auditDao.log(c, userId, "UPDATE", "ANNOUNCEMENT", String.valueOf(announcementId),
                    "Updated announcement '" + title + "'", ip);
            return null;
        });
    }

    public void delete(long userId, long announcementId, String ip) throws Exception {
        Db.transaction(c -> {
            Organizer organizer = requireOrganizer(c, userId);
            Announcement announcement = announcementDao.findForOrganizer(c, announcementId, organizer.getOrganizerId());
            if (announcement == null) {
                throw new AppException("Announcement not found.", 404);
            }
            announcementDao.softDelete(c, announcementId, organizer.getOrganizerId());
            auditDao.log(c, userId, "DELETE", "ANNOUNCEMENT", String.valueOf(announcementId),
                    "Deleted announcement '" + announcement.getTitle() + "'", ip);
            return null;
        });
    }

    private Organizer requireOrganizer(Connection c, long userId) throws Exception {
        Organizer organizer = organizerDao.findByUserId(c, userId);
        if (organizer == null) {
            throw new AppException("Organizer profile not found.", 403);
        }
        return organizer;
    }
}
