package my.uitm.runease.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import my.uitm.runease.model.Event;
import my.uitm.runease.model.EventStatus;
import my.uitm.runease.model.EventType;

public class EventDao extends BaseDao {

    private static final String BASE =
            "SELECT e.event_id, e.organizer_id, e.title, e.slug, e.description, e.event_type, e.venue, e.city, "
            + "e.state, e.event_date, e.flag_off_time, e.registration_open, e.registration_close, e.status, "
            + "e.rejection_reason, e.created_at, e.updated_at, e.deleted_at, o.organization_name, "
            + "(SELECT COALESCE(MIN(c.price), 0) FROM event_categories c WHERE c.event_id=e.event_id) AS min_price, "
            + "(SELECT COALESCE(SUM(c.quota), 0) FROM event_categories c WHERE c.event_id=e.event_id) AS total_quota, "
            + "(SELECT COUNT(*) FROM registrations r JOIN event_categories rc ON r.category_id=rc.category_id "
            + " WHERE rc.event_id=e.event_id AND r.status <> 'CANCELLED' AND r.deleted_at IS NULL) AS registered_count, "
            + "(SELECT COUNT(*) FROM registrations r JOIN event_categories rc ON r.category_id=rc.category_id "
            + " WHERE rc.event_id=e.event_id AND r.status IN ('CONFIRMED','CHECKED_IN','FINISHED','DISQUALIFIED') "
            + " AND r.deleted_at IS NULL) AS paid_count, "
            + "(SELECT COALESCE(SUM(p.amount), 0) FROM payments p JOIN registrations r ON p.registration_id=r.registration_id "
            + " JOIN event_categories rc ON r.category_id=rc.category_id "
            + " WHERE rc.event_id=e.event_id AND p.status='PAID' AND r.deleted_at IS NULL) AS revenue "
            + "FROM events e JOIN organizers o ON e.organizer_id=o.organizer_id ";

    private static final Map<String, String> PUBLIC_SORTS = new LinkedHashMap<>();

    static {
        PUBLIC_SORTS.put("date", "e.event_date");
        PUBLIC_SORTS.put("title", "e.title");
        PUBLIC_SORTS.put("price", "min_price");
        PUBLIC_SORTS.put("popular", "paid_count");
    }

    public Event findById(Connection c, long eventId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE e.event_id=? AND e.deleted_at IS NULL")) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Event findByIdForOrganizer(Connection c, long eventId, long organizerId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                BASE + "WHERE e.event_id=? AND e.organizer_id=? AND e.deleted_at IS NULL")) {
            ps.setLong(1, eventId);
            ps.setLong(2, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Event> listPublic(Connection c, String q, String type, String state,
                                  boolean upcomingOnly, String sort, String dir,
                                  int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildPublicWhere(q, type, state, upcomingOnly, params);
        String order = PUBLIC_SORTS.getOrDefault(sort, "e.event_date") + ("desc".equals(dir) ? " DESC" : " ASC");
        String sql = BASE + where + " ORDER BY " + order + ", e.event_id LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(map(rs));
                }
            }
        }
        return events;
    }

    public long countPublic(Connection c, String q, String type, String state, boolean upcomingOnly)
            throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildPublicWhere(q, type, state, upcomingOnly, params);
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM events e " + where)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public List<Event> featured(Connection c, int limit) throws SQLException {
        String sql = BASE + "WHERE e.deleted_at IS NULL AND e.status='APPROVED' AND e.event_date >= CURRENT_DATE "
                + "ORDER BY e.event_date ASC LIMIT ?";
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(map(rs));
                }
            }
        }
        return events;
    }

    public List<Event> listByOrganizer(Connection c, long organizerId, String q, String status,
                                       String sort, String dir, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildOrganizerWhere(organizerId, q, status, params);
        String order = switch (sort == null ? "" : sort) {
            case "title" -> "e.title";
            case "participants" -> "registered_count";
            case "status" -> "e.status";
            default -> "e.event_date";
        };
        order += "desc".equals(dir) ? " DESC" : " ASC";
        String sql = BASE + where + " ORDER BY " + order + ", e.event_id DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(map(rs));
                }
            }
        }
        return events;
    }

    public long countByOrganizer(Connection c, long organizerId, String q, String status) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildOrganizerWhere(organizerId, q, status, params);
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM events e " + where)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public List<Event> listAll(Connection c, String q, String status, String sort, String dir,
                               int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildAdminWhere(q, status, params);
        String order = switch (sort == null ? "" : sort) {
            case "title" -> "e.title";
            case "organizer" -> "o.organization_name";
            case "status" -> "e.status";
            case "participants" -> "registered_count";
            default -> "e.created_at";
        };
        order += "desc".equals(dir) ? " DESC" : " ASC";
        String sql = BASE + where + " ORDER BY " + order + ", e.event_id DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(map(rs));
                }
            }
        }
        return events;
    }

    public long countAllFiltered(Connection c, String q, String status) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildAdminWhere(q, status, params);
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM events e JOIN organizers o ON e.organizer_id=o.organizer_id " + where)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public List<Event> listForApproval(Connection c) throws SQLException {
        String sql = BASE + "WHERE e.deleted_at IS NULL AND e.status='PENDING' ORDER BY e.created_at ASC";
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                events.add(map(rs));
            }
        }
        return events;
    }

    public List<Event> upcomingByOrganizer(Connection c, long organizerId, int limit) throws SQLException {
        String sql = BASE + "WHERE e.organizer_id=? AND e.deleted_at IS NULL AND e.event_date >= CURRENT_DATE "
                + "ORDER BY e.event_date ASC LIMIT ?";
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, organizerId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(map(rs));
                }
            }
        }
        return events;
    }

    public long insert(Connection c, Event event) throws SQLException {
        String sql = "INSERT INTO events (organizer_id, title, slug, description, event_type, venue, city, state, "
                + "event_date, flag_off_time, registration_open, registration_close, status, created_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindEvent(ps, event);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void update(Connection c, Event event) throws SQLException {
        String sql = "UPDATE events SET title=?, slug=?, description=?, event_type=?, venue=?, city=?, state=?, "
                + "event_date=?, flag_off_time=?, registration_open=?, registration_close=?, status=?, "
                + "rejection_reason=NULL, updated_at=CURRENT_TIMESTAMP WHERE event_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getSlug());
            ps.setString(3, event.getDescription());
            ps.setString(4, event.getEventType().name());
            ps.setString(5, event.getVenue());
            ps.setString(6, event.getCity());
            ps.setString(7, event.getState());
            ps.setDate(8, java.sql.Date.valueOf(event.getEventDate()));
            if (event.getFlagOffTime() == null) {
                ps.setNull(9, java.sql.Types.TIME);
            } else {
                ps.setTime(9, java.sql.Time.valueOf(event.getFlagOffTime()));
            }
            ps.setDate(10, java.sql.Date.valueOf(event.getRegistrationOpen()));
            ps.setDate(11, java.sql.Date.valueOf(event.getRegistrationClose()));
            ps.setString(12, event.getStatus().name());
            ps.setLong(13, event.getEventId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection c, long eventId, EventStatus status, String reason) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE events SET status=?, rejection_reason=?, updated_at=CURRENT_TIMESTAMP WHERE event_id=?")) {
            ps.setString(1, status.name());
            ps.setString(2, reason);
            ps.setLong(3, eventId);
            ps.executeUpdate();
        }
    }

    public void softDelete(Connection c, long eventId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE events SET deleted_at=CURRENT_TIMESTAMP, status='CANCELLED' WHERE event_id=?")) {
            ps.setLong(1, eventId);
            ps.executeUpdate();
        }
    }

    public long countByStatus(Connection c, EventStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM events WHERE deleted_at IS NULL AND status=?")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public long countUpcoming(Connection c, long organizerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM events WHERE deleted_at IS NULL AND event_date >= CURRENT_DATE"
                + (organizerId > 0 ? " AND organizer_id=?" : "");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (organizerId > 0) {
                ps.setLong(1, organizerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public long countAll(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM events WHERE deleted_at IS NULL");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public List<String> listStates(Connection c) throws SQLException {
        List<String> states = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT DISTINCT state FROM events WHERE state IS NOT NULL AND deleted_at IS NULL "
                + "AND status IN ('APPROVED','COMPLETED') ORDER BY state");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                states.add(rs.getString(1));
            }
        }
        return states;
    }

    private String buildPublicWhere(String q, String type, String state, boolean upcomingOnly, List<Object> params) {
        StringBuilder where = new StringBuilder(
                "WHERE e.deleted_at IS NULL AND e.status IN ('APPROVED','COMPLETED')");
        if (upcomingOnly) {
            where.append(" AND e.event_date >= CURRENT_DATE");
        }
        if (q != null && !q.isBlank()) {
            where.append(" AND (LOWER(e.title) LIKE ? OR LOWER(e.venue) LIKE ? OR LOWER(e.city) LIKE ?)");
            String term = like(q);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (type != null && !type.isBlank()) {
            where.append(" AND e.event_type=?");
            params.add(type);
        }
        if (state != null && !state.isBlank()) {
            where.append(" AND e.state=?");
            params.add(state);
        }
        return where.toString();
    }

    private String buildAdminWhere(String q, String status, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE e.deleted_at IS NULL");
        if (q != null && !q.isBlank()) {
            where.append(" AND (LOWER(e.title) LIKE ? OR LOWER(e.venue) LIKE ? OR LOWER(e.city) LIKE ? "
                    + "OR LOWER(o.organization_name) LIKE ?)");
            String term = like(q);
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND e.status=?");
            params.add(status);
        }
        return where.toString();
    }

    private String buildOrganizerWhere(long organizerId, String q, String status, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE e.deleted_at IS NULL AND e.organizer_id=?");
        params.add(organizerId);
        if (q != null && !q.isBlank()) {
            where.append(" AND (LOWER(e.title) LIKE ? OR LOWER(e.venue) LIKE ? OR LOWER(e.city) LIKE ?)");
            String term = like(q);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND e.status=?");
            params.add(status);
        }
        return where.toString();
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private void bindEvent(PreparedStatement ps, Event event) throws SQLException {
        ps.setLong(1, event.getOrganizerId());
        ps.setString(2, event.getTitle());
        ps.setString(3, event.getSlug());
        ps.setString(4, event.getDescription());
        ps.setString(5, event.getEventType().name());
        ps.setString(6, event.getVenue());
        ps.setString(7, event.getCity());
        ps.setString(8, event.getState());
        ps.setDate(9, java.sql.Date.valueOf(event.getEventDate()));
        if (event.getFlagOffTime() == null) {
            ps.setNull(10, java.sql.Types.TIME);
        } else {
            ps.setTime(10, java.sql.Time.valueOf(event.getFlagOffTime()));
        }
        ps.setDate(11, java.sql.Date.valueOf(event.getRegistrationOpen()));
        ps.setDate(12, java.sql.Date.valueOf(event.getRegistrationClose()));
        ps.setString(13, event.getStatus().name());
    }

    private Event map(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setOrganizerId(rs.getLong("organizer_id"));
        event.setOrganizerName(text(rs, "organization_name"));
        event.setTitle(text(rs, "title"));
        event.setSlug(text(rs, "slug"));
        event.setDescription(text(rs, "description"));
        event.setEventType(EventType.parse(rs.getString("event_type")));
        event.setVenue(text(rs, "venue"));
        event.setCity(text(rs, "city"));
        event.setState(text(rs, "state"));
        event.setEventDate(date(rs, "event_date"));
        event.setFlagOffTime(time(rs, "flag_off_time"));
        event.setRegistrationOpen(date(rs, "registration_open"));
        event.setRegistrationClose(date(rs, "registration_close"));
        event.setStatus(EventStatus.parse(rs.getString("status")));
        event.setRejectionReason(text(rs, "rejection_reason"));
        event.setCreatedAt(dateTime(rs, "created_at"));
        event.setUpdatedAt(dateTime(rs, "updated_at"));
        event.setDeletedAt(dateTime(rs, "deleted_at"));
        event.setMinPrice(decimal(rs, "min_price"));
        event.setTotalQuota(rs.getInt("total_quota"));
        event.setRegisteredCount(rs.getInt("registered_count"));
        event.setPaidCount(rs.getInt("paid_count"));
        BigDecimal revenue = decimal(rs, "revenue");
        event.setRevenue(revenue == null ? BigDecimal.ZERO : revenue);
        return event;
    }
}
