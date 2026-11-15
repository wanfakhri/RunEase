package my.uitm.runease.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import my.uitm.runease.model.EventStatus;
import my.uitm.runease.util.Fmt;

public class StatsDao extends BaseDao {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

    public Map<String, Long> usersByRole(Connection c) throws SQLException {
        Map<String, Long> result = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT role, COUNT(*) AS total FROM users WHERE deleted_at IS NULL GROUP BY role");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("role"), rs.getLong("total"));
            }
        }
        return result;
    }

    public Map<String, Long> eventStatuses(Connection c, long organizerId) throws SQLException {
        Map<String, Long> result = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS total FROM events WHERE deleted_at IS NULL"
                + (organizerId > 0 ? " AND organizer_id=?" : "") + " GROUP BY status";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (organizerId > 0) {
                ps.setLong(1, organizerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("status"), rs.getLong("total"));
                }
            }
        }
        return result;
    }

    public Map<String, Long> registrationStatuses(Connection c, long organizerId) throws SQLException {
        Map<String, Long> result = new LinkedHashMap<>();
        String sql = "SELECT r.status, COUNT(*) AS total FROM registrations r "
                + "JOIN event_categories cat ON r.category_id=cat.category_id "
                + "JOIN events e ON cat.event_id=e.event_id "
                + "WHERE r.deleted_at IS NULL" + (organizerId > 0 ? " AND e.organizer_id=?" : "")
                + " GROUP BY r.status";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (organizerId > 0) {
                ps.setLong(1, organizerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("status"), rs.getLong("total"));
                }
            }
        }
        return result;
    }

    public Map<String, Long> monthlyRegistrations(Connection c, long organizerId, int months)
            throws SQLException {
        Map<YearMonth, Long> found = new LinkedHashMap<>();
        String sql = "SELECT EXTRACT(YEAR FROM r.registered_at) AS yr, "
                + "EXTRACT(MONTH FROM r.registered_at) AS mo, COUNT(*) AS total "
                + "FROM registrations r JOIN event_categories cat ON r.category_id=cat.category_id "
                + "JOIN events e ON cat.event_id=e.event_id "
                + "WHERE r.deleted_at IS NULL AND r.registered_at >= ?"
                + (organizerId > 0 ? " AND e.organizer_id=?" : "")
                + " GROUP BY EXTRACT(YEAR FROM r.registered_at), EXTRACT(MONTH FROM r.registered_at)";
        YearMonth start = YearMonth.now().minusMonths(months - 1L);
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(start.atDay(1)));
            if (organizerId > 0) {
                ps.setLong(2, organizerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    YearMonth ym = YearMonth.of(rs.getInt("yr"), rs.getInt("mo"));
                    found.put(ym, rs.getLong("total"));
                }
            }
        }
        Map<String, Long> series = new LinkedHashMap<>();
        for (int i = 0; i < months; i++) {
            YearMonth ym = start.plusMonths(i);
            series.put(ym.atDay(1).format(MONTH_LABEL), found.getOrDefault(ym, 0L));
        }
        return series;
    }

    public Map<String, BigDecimal> monthlyRevenue(Connection c, long organizerId, int months)
            throws SQLException {
        Map<YearMonth, BigDecimal> found = new LinkedHashMap<>();
        String sql = "SELECT EXTRACT(YEAR FROM p.paid_at) AS yr, EXTRACT(MONTH FROM p.paid_at) AS mo, "
                + "COALESCE(SUM(p.amount),0) AS total FROM payments p "
                + "JOIN registrations r ON p.registration_id=r.registration_id "
                + "JOIN event_categories cat ON r.category_id=cat.category_id "
                + "JOIN events e ON cat.event_id=e.event_id "
                + "WHERE p.status='PAID' AND r.deleted_at IS NULL AND p.paid_at >= ?"
                + (organizerId > 0 ? " AND e.organizer_id=?" : "")
                + " GROUP BY EXTRACT(YEAR FROM p.paid_at), EXTRACT(MONTH FROM p.paid_at)";
        YearMonth start = YearMonth.now().minusMonths(months - 1L);
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(start.atDay(1)));
            if (organizerId > 0) {
                ps.setLong(2, organizerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    YearMonth ym = YearMonth.of(rs.getInt("yr"), rs.getInt("mo"));
                    found.put(ym, rs.getBigDecimal("total"));
                }
            }
        }
        Map<String, BigDecimal> series = new LinkedHashMap<>();
        for (int i = 0; i < months; i++) {
            YearMonth ym = start.plusMonths(i);
            series.put(ym.atDay(1).format(MONTH_LABEL),
                    found.getOrDefault(ym, BigDecimal.ZERO));
        }
        return series;
    }

    public List<Map<String, Object>> topEvents(Connection c, long organizerId, int limit) throws SQLException {
        String sql = "SELECT e.event_id, e.title, e.event_date, e.status, "
                + "(SELECT COUNT(*) FROM registrations r JOIN event_categories cat ON r.category_id=cat.category_id "
                + " WHERE cat.event_id=e.event_id AND r.status <> 'CANCELLED' AND r.deleted_at IS NULL) AS participants, "
                + "(SELECT COALESCE(SUM(p.amount),0) FROM payments p JOIN registrations r2 ON p.registration_id=r2.registration_id "
                + " JOIN event_categories cat2 ON r2.category_id=cat2.category_id "
                + " WHERE cat2.event_id=e.event_id AND p.status='PAID' AND r2.deleted_at IS NULL) AS revenue "
                + "FROM events e WHERE e.deleted_at IS NULL"
                + (organizerId > 0 ? " AND e.organizer_id=?" : "")
                + " ORDER BY participants DESC LIMIT ?";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int index = 1;
            if (organizerId > 0) {
                ps.setLong(index++, organizerId);
            }
            ps.setInt(index, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    long eventId = rs.getLong("event_id");
                    String status = rs.getString("status");
                    java.time.LocalDate eventDate = rs.getDate("event_date").toLocalDate();
                    row.put("eventId", eventId);
                    row.put("title", rs.getString("title"));
                    row.put("eventDate", eventDate);
                    row.put("eventDateLabel", Fmt.date(eventDate));
                    row.put("status", status);
                    row.put("statusLabel", EventStatus.parse(status).getLabel());
                    row.put("statusBadge", EventStatus.parse(status).getBadge());
                    row.put("participants", rs.getLong("participants"));
                    row.put("revenue", rs.getBigDecimal("revenue"));
                    row.put("revenueFormatted", Fmt.money(rs.getBigDecimal("revenue")));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public long registrationsThisMonth(Connection c, long organizerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations r "
                + "JOIN event_categories cat ON r.category_id=cat.category_id "
                + "JOIN events e ON cat.event_id=e.event_id "
                + "WHERE r.deleted_at IS NULL AND EXTRACT(MONTH FROM r.registered_at)=EXTRACT(MONTH FROM CURRENT_DATE) "
                + "AND EXTRACT(YEAR FROM r.registered_at)=EXTRACT(YEAR FROM CURRENT_DATE)"
                + (organizerId > 0 ? " AND e.organizer_id=?" : "");
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

    public long checkInsToday(Connection c, long organizerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations r "
                + "JOIN event_categories cat ON r.category_id=cat.category_id "
                + "JOIN events e ON cat.event_id=e.event_id "
                + "WHERE r.checked_in_at IS NOT NULL AND CAST(r.checked_in_at AS DATE)=CURRENT_DATE"
                + (organizerId > 0 ? " AND e.organizer_id=?" : "");
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

    public long countRegistrationsForEvent(Connection c, long eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations r JOIN event_categories cat ON r.category_id=cat.category_id "
                + "WHERE cat.event_id=? AND r.status <> 'CANCELLED' AND r.deleted_at IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
