package my.uitm.runease.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import my.uitm.runease.model.EventCategory;

public class CategoryDao extends BaseDao {

    private static final String BASE =
            "SELECT c.category_id, c.event_id, c.name, c.distance_km, c.price, c.quota, c.start_time, "
            + "(SELECT COUNT(*) FROM registrations r WHERE r.category_id=c.category_id "
            + " AND r.status <> 'CANCELLED' AND r.deleted_at IS NULL) AS registered_count, "
            + "(SELECT COUNT(*) FROM registrations r WHERE r.category_id=c.category_id "
            + " AND r.status IN ('CONFIRMED','CHECKED_IN','FINISHED','DISQUALIFIED') "
            + " AND r.deleted_at IS NULL) AS paid_count "
            + "FROM event_categories c ";

    public List<EventCategory> listByEvent(Connection c, long eventId) throws SQLException {
        List<EventCategory> categories = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                BASE + "WHERE c.event_id=? ORDER BY c.distance_km DESC")) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(map(rs));
                }
            }
        }
        return categories;
    }

    public EventCategory findById(Connection c, long categoryId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE c.category_id=?")) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Locks the category row so concurrent registrations cannot oversell a quota. */
    public EventCategory lockForUpdate(Connection c, long categoryId) throws SQLException {
        String sql = "SELECT c.category_id, c.event_id, c.name, c.distance_km, c.price, c.quota, c.start_time, "
                + "0 AS registered_count, 0 AS paid_count FROM event_categories c WHERE c.category_id=? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public long activeCount(Connection c, long categoryId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE category_id=? AND status <> 'CANCELLED' "
                + "AND deleted_at IS NULL")) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public long insert(Connection c, EventCategory category) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO event_categories (event_id, name, distance_km, price, quota, start_time) "
                + "VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, category.getEventId());
            ps.setString(2, category.getName());
            ps.setDouble(3, category.getDistanceKm());
            ps.setBigDecimal(4, category.getPrice());
            ps.setInt(5, category.getQuota());
            if (category.getStartTime() == null) {
                ps.setNull(6, java.sql.Types.TIME);
            } else {
                ps.setTime(6, java.sql.Time.valueOf(category.getStartTime()));
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void update(Connection c, EventCategory category) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE event_categories SET name=?, distance_km=?, price=?, quota=?, start_time=?, "
                + "updated_at=CURRENT_TIMESTAMP WHERE category_id=?")) {
            ps.setString(1, category.getName());
            ps.setDouble(2, category.getDistanceKm());
            ps.setBigDecimal(3, category.getPrice());
            ps.setInt(4, category.getQuota());
            if (category.getStartTime() == null) {
                ps.setNull(5, java.sql.Types.TIME);
            } else {
                ps.setTime(5, java.sql.Time.valueOf(category.getStartTime()));
            }
            ps.setLong(6, category.getCategoryId());
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, long categoryId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM event_categories WHERE category_id=?")) {
            ps.setLong(1, categoryId);
            ps.executeUpdate();
        }
    }

    public void deleteByEvent(Connection c, long eventId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM event_categories WHERE event_id=?")) {
            ps.setLong(1, eventId);
            ps.executeUpdate();
        }
    }

    public long totalQuota(Connection c, long eventId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(quota),0) FROM event_categories WHERE event_id=?")) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private EventCategory map(ResultSet rs) throws SQLException {
        EventCategory category = new EventCategory();
        category.setCategoryId(rs.getLong("category_id"));
        category.setEventId(rs.getLong("event_id"));
        category.setName(text(rs, "name"));
        category.setDistanceKm(rs.getDouble("distance_km"));
        category.setPrice(decimal(rs, "price"));
        category.setQuota(rs.getInt("quota"));
        category.setStartTime(time(rs, "start_time"));
        category.setRegisteredCount(rs.getInt("registered_count"));
        category.setPaidCount(rs.getInt("paid_count"));
        return category;
    }
}
