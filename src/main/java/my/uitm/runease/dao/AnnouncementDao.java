package my.uitm.runease.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import my.uitm.runease.model.Announcement;

public class AnnouncementDao extends BaseDao {

    private static final String BASE =
            "SELECT a.announcement_id, a.event_id, a.title, a.body, a.audience, a.created_by, a.published_at, "
            + "a.updated_at, e.title AS event_title, u.full_name AS author_name "
            + "FROM announcements a JOIN events e ON a.event_id=e.event_id "
            + "JOIN users u ON a.created_by=u.user_id ";

    public List<Announcement> listByEvent(Connection c, long eventId, boolean confirmedOnly) throws SQLException {
        String sql = BASE + "WHERE a.event_id=? AND a.deleted_at IS NULL "
                + (confirmedOnly ? "AND a.audience='CONFIRMED' " : "")
                + "ORDER BY a.published_at DESC";
        List<Announcement> announcements = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    announcements.add(map(rs));
                }
            }
        }
        return announcements;
    }

    public List<Announcement> listByOrganizer(Connection c, long organizerId, Long eventId) throws SQLException {
        String sql = BASE + "WHERE e.organizer_id=? AND a.deleted_at IS NULL"
                + (eventId != null && eventId > 0 ? " AND a.event_id=?" : "")
                + " ORDER BY a.published_at DESC";
        List<Announcement> announcements = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, organizerId);
            if (eventId != null && eventId > 0) {
                ps.setLong(2, eventId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    announcements.add(map(rs));
                }
            }
        }
        return announcements;
    }

    public Announcement findById(Connection c, long announcementId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE a.announcement_id=?")) {
            ps.setLong(1, announcementId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Announcement> listForParticipant(Connection c, long userId) throws SQLException {
        String sql = BASE + "WHERE a.deleted_at IS NULL AND a.event_id IN "
                + "(SELECT c.event_id FROM registrations r JOIN event_categories c ON r.category_id=c.category_id "
                + " WHERE r.user_id=? AND r.status IN ('CONFIRMED','CHECKED_IN','FINISHED')) "
                + "ORDER BY a.published_at DESC";
        List<Announcement> announcements = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    announcements.add(map(rs));
                }
            }
        }
        return announcements;
    }

    public long insert(Connection c, Announcement announcement) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO announcements (event_id, title, body, audience, created_by, published_at) "
                + "VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, announcement.getEventId());
            ps.setString(2, announcement.getTitle());
            ps.setString(3, announcement.getBody());
            ps.setString(4, announcement.getAudience());
            ps.setLong(5, announcement.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void update(Connection c, Announcement announcement) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE announcements SET title=?, body=?, audience=?, updated_at=CURRENT_TIMESTAMP "
                + "WHERE announcement_id=?")) {
            ps.setString(1, announcement.getTitle());
            ps.setString(2, announcement.getBody());
            ps.setString(3, announcement.getAudience());
            ps.setLong(4, announcement.getAnnouncementId());
            ps.executeUpdate();
        }
    }

    public void softDelete(Connection c, long announcementId, long organizerId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE announcements SET deleted_at=CURRENT_TIMESTAMP WHERE announcement_id=? AND event_id IN "
                + "(SELECT event_id FROM events WHERE organizer_id=?)")) {
            ps.setLong(1, announcementId);
            ps.setLong(2, organizerId);
            ps.executeUpdate();
        }
    }

    public Announcement findForOrganizer(Connection c, long announcementId, long organizerId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE
                + "WHERE a.announcement_id=? AND e.organizer_id=?")) {
            ps.setLong(1, announcementId);
            ps.setLong(2, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Announcement map(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setAnnouncementId(rs.getLong("announcement_id"));
        announcement.setEventId(rs.getLong("event_id"));
        announcement.setEventTitle(text(rs, "event_title"));
        announcement.setTitle(text(rs, "title"));
        announcement.setBody(text(rs, "body"));
        announcement.setAudience(text(rs, "audience"));
        announcement.setCreatedBy(rs.getLong("created_by"));
        announcement.setAuthorName(text(rs, "author_name"));
        announcement.setPublishedAt(dateTime(rs, "published_at"));
        announcement.setUpdatedAt(dateTime(rs, "updated_at"));
        return announcement;
    }
}
