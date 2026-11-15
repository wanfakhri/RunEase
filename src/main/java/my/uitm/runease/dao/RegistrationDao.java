package my.uitm.runease.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import my.uitm.runease.model.Payment;
import my.uitm.runease.model.PaymentStatus;
import my.uitm.runease.model.Registration;
import my.uitm.runease.model.RegistrationStatus;
import my.uitm.runease.model.Result;

public class RegistrationDao extends BaseDao {

    private static final String BASE =
            "SELECT r.registration_id, r.registration_code, r.user_id, r.category_id, r.bib_number, r.jersey_size, "
            + "r.emergency_name, r.emergency_phone, r.medical_notes, r.status, r.total_amount, r.registered_at, "
            + "r.checked_in_at, r.cancelled_at, "
            + "u.full_name AS participant_name, u.email AS participant_email, u.phone AS participant_phone, "
            + "c.name AS category_name, c.distance_km, c.event_id, "
            + "e.title AS event_title, e.event_date, e.venue, e.city AS event_city, e.organizer_id, "
            + "p.payment_id, p.reference_no, p.method, p.amount AS payment_amount, p.status AS payment_status, "
            + "p.paid_at, "
            + "res.result_id, res.finish_time_seconds, res.overall_position, res.category_position, res.pace_seconds "
            + "FROM registrations r "
            + "JOIN users u ON r.user_id=u.user_id "
            + "JOIN event_categories c ON r.category_id=c.category_id "
            + "JOIN events e ON c.event_id=e.event_id "
            + "LEFT JOIN payments p ON p.registration_id=r.registration_id "
            + "LEFT JOIN results res ON res.registration_id=r.registration_id ";

    private static final Map<String, String> SORTS = new LinkedHashMap<>();

    static {
        SORTS.put("name", "u.full_name");
        SORTS.put("event", "e.event_date");
        SORTS.put("category", "c.distance_km");
        SORTS.put("status", "r.status");
        SORTS.put("registered", "r.registered_at");
        SORTS.put("bib", "r.bib_number");
    }

    public Registration findById(Connection c, long registrationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE r.registration_id=?")) {
            ps.setLong(1, registrationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Registration findByIdForUser(Connection c, long registrationId, long userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                BASE + "WHERE r.registration_id=? AND r.user_id=?")) {
            ps.setLong(1, registrationId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Registration findByCode(Connection c, String code) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE r.registration_code=?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Registration> listByUser(Connection c, long userId, String status, String sort, String dir,
                                         int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE r.user_id=? AND r.deleted_at IS NULL");
        params.add(userId);
        if (status != null && !status.isBlank()) {
            where.append(" AND r.status=?");
            params.add(status);
        }
        String order = switch (sort == null ? "" : sort) {
            case "event" -> "e.event_date";
            case "status" -> "r.status";
            case "registered" -> "r.registered_at";
            default -> "e.event_date";
        };
        String direction = "desc".equals(dir) ? " DESC" : " ASC";
        String sql = BASE + where + " ORDER BY " + order + direction + ", r.registration_id DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<Registration> registrations = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        }
        return registrations;
    }

    public long countByUser(Connection c, long userId, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations r WHERE r.user_id=? AND r.deleted_at IS NULL"
                + (status == null || status.isBlank() ? "" : " AND r.status=?");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            if (status != null && !status.isBlank()) {
                ps.setString(2, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public List<Registration> listForOrganizer(Connection c, long organizerId, Long eventId, String q,
                                               String status, String sort, String dir,
                                               int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = organizerWhere(organizerId, eventId, q, status, params);
        String order = SORTS.getOrDefault(sort, "r.registered_at") + ("asc".equals(dir) ? " ASC" : " DESC");
        String sql = BASE + where + " ORDER BY " + order + ", r.registration_id DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<Registration> registrations = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        }
        return registrations;
    }

    public long countForOrganizer(Connection c, long organizerId, Long eventId, String q, String status)
            throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = organizerWhere(organizerId, eventId, q, status, params);
        String sql = "SELECT COUNT(*) FROM registrations r "
                + "JOIN users u ON r.user_id=u.user_id "
                + "JOIN event_categories c ON r.category_id=c.category_id "
                + "JOIN events e ON c.event_id=e.event_id " + where;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public List<Registration> listAll(Connection c, String q, String status, String sort, String dir,
                                      int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = adminWhere(q, status, params);
        String order = SORTS.getOrDefault(sort, "r.registered_at") + ("asc".equals(dir) ? " ASC" : " DESC");
        String sql = BASE + where + " ORDER BY " + order + ", r.registration_id DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<Registration> registrations = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        }
        return registrations;
    }

    public long countAllFiltered(Connection c, String q, String status) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = adminWhere(q, status, params);
        String sql = "SELECT COUNT(*) FROM registrations r JOIN users u ON r.user_id=u.user_id "
                + "JOIN event_categories c ON r.category_id=c.category_id "
                + "JOIN events e ON c.event_id=e.event_id " + where;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public List<Registration> listByEvent(Connection c, long eventId) throws SQLException {
        List<Registration> registrations = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(BASE
                + "WHERE c.event_id=? AND r.deleted_at IS NULL AND r.status IN "
                + "('CONFIRMED','CHECKED_IN','FINISHED','DISQUALIFIED') ORDER BY u.full_name")) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        }
        return registrations;
    }

    public boolean existsActiveInEvent(Connection c, long userId, long eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations r JOIN event_categories c ON r.category_id=c.category_id "
                + "WHERE r.user_id=? AND c.event_id=? AND r.status <> 'CANCELLED' AND r.deleted_at IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public long insert(Connection c, Registration registration) throws SQLException {
        String sql = "INSERT INTO registrations (registration_code, user_id, category_id, jersey_size, "
                + "emergency_name, emergency_phone, medical_notes, status, total_amount, registered_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, registration.getRegistrationCode());
            ps.setLong(2, registration.getUserId());
            ps.setLong(3, registration.getCategoryId());
            ps.setString(4, registration.getJerseySize());
            ps.setString(5, registration.getEmergencyName());
            ps.setString(6, registration.getEmergencyPhone());
            ps.setString(7, registration.getMedicalNotes());
            ps.setString(8, registration.getStatus().name());
            ps.setBigDecimal(9, registration.getTotalAmount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void updateCode(Connection c, long registrationId, String code) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE registrations SET registration_code=? WHERE registration_id=?")) {
            ps.setString(1, code);
            ps.setLong(2, registrationId);
            ps.executeUpdate();
        }
    }

    public boolean confirm(Connection c, long registrationId, String bib) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE registrations SET status='CONFIRMED', bib_number=? WHERE registration_id=? "
                + "AND status='PENDING_PAYMENT'")) {
            ps.setString(1, bib);
            ps.setLong(2, registrationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancel(Connection c, long registrationId, long userId) throws SQLException {
        String sql = "UPDATE registrations SET status='CANCELLED', cancelled_at=CURRENT_TIMESTAMP "
                + "WHERE registration_id=? AND user_id=? AND status IN ('CONFIRMED','PENDING_PAYMENT')";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, registrationId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean checkIn(Connection c, long registrationId, long organizerId) throws SQLException {
        String sql = "UPDATE registrations SET status='CHECKED_IN', checked_in_at=CURRENT_TIMESTAMP "
                + "WHERE registration_id=? AND status='CONFIRMED' AND category_id IN "
                + "(SELECT c.category_id FROM event_categories c JOIN events e ON c.event_id=e.event_id "
                + " WHERE e.organizer_id=?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, registrationId);
            ps.setLong(2, organizerId);
            return ps.executeUpdate() > 0;
        }
    }

    public void updateStatus(Connection c, long registrationId, RegistrationStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE registrations SET status=? WHERE registration_id=?")) {
            ps.setString(1, status.name());
            ps.setLong(2, registrationId);
            ps.executeUpdate();
        }
    }

    public String nextBib(Connection c) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(bib_number, 4) AS INT)), 10000) AS max_bib "
                + "FROM registrations WHERE bib_number LIKE 'BIB%'";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format("BIB%05d", rs.getInt("max_bib") + 1);
        }
    }

    public long nextSequence(Connection c, int year) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE registration_code LIKE ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "RE-" + year + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) + 1;
            }
        }
    }

    public boolean codeExists(Connection c, String code) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE registration_code=?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public List<Registration> recentForOrganizer(Connection c, long organizerId, int limit) throws SQLException {
        List<Registration> registrations = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(BASE
                + "WHERE e.organizer_id=? AND r.deleted_at IS NULL ORDER BY r.registered_at DESC LIMIT ?")) {
            ps.setLong(1, organizerId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        }
        return registrations;
    }

    public List<Registration> recent(Connection c, int limit) throws SQLException {
        List<Registration> registrations = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(BASE
                + "WHERE r.deleted_at IS NULL ORDER BY r.registered_at DESC LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        }
        return registrations;
    }

    public long countAll(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE deleted_at IS NULL");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public long countAllByStatus(Connection c, RegistrationStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE deleted_at IS NULL AND status=?")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public long countForOrganizerByStatus(Connection c, long organizerId, RegistrationStatus status)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations r JOIN event_categories c ON r.category_id=c.category_id "
                + "JOIN events e ON c.event_id=e.event_id WHERE e.organizer_id=? AND r.deleted_at IS NULL"
                + (status == null ? "" : " AND r.status=?");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, organizerId);
            if (status != null) {
                ps.setString(2, status.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public long countByUserAndStatus(Connection c, long userId, RegistrationStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE user_id=? AND deleted_at IS NULL AND status=?")) {
            ps.setLong(1, userId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public double totalDistanceForUser(Connection c, long userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(c.distance_km),0) FROM registrations r "
                + "JOIN event_categories c ON r.category_id=c.category_id "
                + "WHERE r.user_id=? AND r.deleted_at IS NULL AND r.status IN "
                + "('CONFIRMED','CHECKED_IN','FINISHED')";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    /**
     * Registrations for one runner whose event is still ahead of them, soonest
     * first. Used by the participant dashboard "next start lines" card so the
     * view can link straight into each registration.
     */
    public List<Registration> upcomingForUser(Connection c, long userId, int limit) throws SQLException {
        String sql = BASE + "WHERE r.user_id=? AND r.deleted_at IS NULL "
                + "AND r.status IN ('CONFIRMED','CHECKED_IN') AND e.event_date >= CURRENT_DATE "
                + "ORDER BY e.event_date ASC LIMIT ?";
        List<Registration> registrations = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        }
        return registrations;
    }

    public List<String> nextEventsForUser(Connection c, long userId, int limit) throws SQLException {
        List<String> titles = new ArrayList<>();
        String sql = "SELECT e.title FROM registrations r JOIN event_categories c ON r.category_id=c.category_id "
                + "JOIN events e ON c.event_id=e.event_id WHERE r.user_id=? AND r.deleted_at IS NULL "
                + "AND r.status IN ('CONFIRMED','CHECKED_IN') AND e.event_date >= CURRENT_DATE "
                + "ORDER BY e.event_date ASC LIMIT ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    titles.add(rs.getString(1));
                }
            }
        }
        return titles;
    }

    private String organizerWhere(long organizerId, Long eventId, String q, String status,
                                  List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE e.organizer_id=? AND r.deleted_at IS NULL");
        params.add(organizerId);
        if (eventId != null && eventId > 0) {
            where.append(" AND e.event_id=?");
            params.add(eventId);
        }
        if (q != null && !q.isBlank()) {
            where.append(" AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(r.bib_number) LIKE ? "
                    + "OR LOWER(r.registration_code) LIKE ?)");
            String term = like(q);
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND r.status=?");
            params.add(status);
        }
        return where.toString();
    }

    private String adminWhere(String q, String status, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE r.deleted_at IS NULL");
        if (q != null && !q.isBlank()) {
            where.append(" AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(e.title) LIKE ? "
                    + "OR LOWER(r.registration_code) LIKE ?)");
            String term = like(q);
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND r.status=?");
            params.add(status);
        }
        return where.toString();
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    public static Registration map(ResultSet rs) throws SQLException {
        Registration registration = new Registration();
        registration.setRegistrationId(rs.getLong("registration_id"));
        registration.setRegistrationCode(text(rs, "registration_code"));
        registration.setUserId(rs.getLong("user_id"));
        registration.setParticipantName(text(rs, "participant_name"));
        registration.setParticipantEmail(text(rs, "participant_email"));
        registration.setParticipantPhone(text(rs, "participant_phone"));
        registration.setCategoryId(rs.getLong("category_id"));
        registration.setCategoryName(text(rs, "category_name"));
        registration.setDistanceKm(rs.getDouble("distance_km"));
        registration.setEventId(rs.getLong("event_id"));
        registration.setEventTitle(text(rs, "event_title"));
        registration.setEventDate(date(rs, "event_date"));
        registration.setVenue(text(rs, "venue"));
        registration.setEventCity(text(rs, "event_city"));
        registration.setOrganizerId(rs.getLong("organizer_id"));
        registration.setBibNumber(text(rs, "bib_number"));
        registration.setJerseySize(text(rs, "jersey_size"));
        registration.setEmergencyName(text(rs, "emergency_name"));
        registration.setEmergencyPhone(text(rs, "emergency_phone"));
        registration.setMedicalNotes(text(rs, "medical_notes"));
        registration.setStatus(RegistrationStatus.parse(rs.getString("status")));
        registration.setTotalAmount(decimal(rs, "total_amount"));
        registration.setRegisteredAt(dateTime(rs, "registered_at"));
        registration.setCheckedInAt(dateTime(rs, "checked_in_at"));
        registration.setCancelledAt(dateTime(rs, "cancelled_at"));

        long paymentId = rs.getLong("payment_id");
        if (!rs.wasNull()) {
            Payment payment = new Payment();
            payment.setPaymentId(paymentId);
            payment.setRegistrationId(registration.getRegistrationId());
            payment.setReferenceNo(text(rs, "reference_no"));
            payment.setMethod(text(rs, "method"));
            payment.setAmount(decimal(rs, "payment_amount"));
            payment.setStatus(PaymentStatus.parse(rs.getString("payment_status")));
            payment.setPaidAt(dateTime(rs, "paid_at"));
            registration.setPayment(payment);
        }

        long resultId = rs.getLong("result_id");
        if (!rs.wasNull()) {
            Result result = new Result();
            result.setResultId(resultId);
            result.setRegistrationId(registration.getRegistrationId());
            result.setFinishTimeSeconds(rs.getInt("finish_time_seconds"));
            result.setOverallPosition(nullableInt(rs, "overall_position"));
            result.setCategoryPosition(nullableInt(rs, "category_position"));
            result.setPaceSeconds(nullableInt(rs, "pace_seconds"));
            registration.setResult(result);
        }
        return registration;
    }
}
