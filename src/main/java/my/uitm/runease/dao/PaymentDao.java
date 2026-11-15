package my.uitm.runease.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import my.uitm.runease.model.Payment;
import my.uitm.runease.model.PaymentStatus;

public class PaymentDao extends BaseDao {

    public long insert(Connection c, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (registration_id, reference_no, method, amount, status, paid_at, created_at) "
                + "VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, payment.getRegistrationId());
            ps.setString(2, payment.getReferenceNo());
            ps.setString(3, payment.getMethod());
            ps.setBigDecimal(4, payment.getAmount());
            ps.setString(5, payment.getStatus().name());
            if (payment.getPaidAt() == null) {
                ps.setNull(6, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(6, java.sql.Timestamp.valueOf(payment.getPaidAt()));
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public Payment findByRegistration(Connection c, long registrationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM payments WHERE registration_id=?")) {
            ps.setLong(1, registrationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Payment payment = new Payment();
                payment.setPaymentId(rs.getLong("payment_id"));
                payment.setRegistrationId(rs.getLong("registration_id"));
                payment.setReferenceNo(text(rs, "reference_no"));
                payment.setMethod(text(rs, "method"));
                payment.setAmount(decimal(rs, "amount"));
                payment.setStatus(PaymentStatus.parse(rs.getString("status")));
                payment.setPaidAt(dateTime(rs, "paid_at"));
                payment.setCreatedAt(dateTime(rs, "created_at"));
                return payment;
            }
        }
    }

    public void markPaid(Connection c, long registrationId, String method) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE payments SET status='PAID', method=?, paid_at=CURRENT_TIMESTAMP WHERE registration_id=?")) {
            ps.setString(1, method);
            ps.setLong(2, registrationId);
            ps.executeUpdate();
        }
    }

    public void refund(Connection c, long registrationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE payments SET status='REFUNDED' WHERE registration_id=?")) {
            ps.setLong(1, registrationId);
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection c, long registrationId, PaymentStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE payments SET status=? WHERE registration_id=?")) {
            ps.setString(1, status.name());
            ps.setLong(2, registrationId);
            ps.executeUpdate();
        }
    }

    public BigDecimal totalRevenue(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='PAID'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    public BigDecimal revenueForOrganizer(Connection c, long organizerId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(p.amount),0) FROM payments p "
                + "JOIN registrations r ON p.registration_id=r.registration_id "
                + "JOIN event_categories c ON r.category_id=c.category_id "
                + "JOIN events e ON c.event_id=e.event_id "
                + "WHERE e.organizer_id=? AND p.status='PAID' AND r.deleted_at IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    public BigDecimal revenueForEvent(Connection c, long eventId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(p.amount),0) FROM payments p "
                + "JOIN registrations r ON p.registration_id=r.registration_id "
                + "JOIN event_categories c ON r.category_id=c.category_id "
                + "WHERE c.event_id=? AND p.status='PAID' AND r.deleted_at IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    public BigDecimal revenueThisMonth(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='PAID' "
                + "AND EXTRACT(MONTH FROM paid_at) = EXTRACT(MONTH FROM CURRENT_DATE) "
                + "AND EXTRACT(YEAR FROM paid_at) = EXTRACT(YEAR FROM CURRENT_DATE)");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
