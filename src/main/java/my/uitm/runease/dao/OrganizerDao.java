package my.uitm.runease.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import my.uitm.runease.model.Organizer;
import my.uitm.runease.model.VerificationStatus;

public class OrganizerDao extends BaseDao {

    private static final String BASE =
            "SELECT o.organizer_id, o.user_id, o.organization_name, o.registration_no, o.contact_person, "
            + "o.website, o.verification_status, o.verified_at, o.verified_by, o.created_at, "
            + "u.full_name AS user_name, u.email AS user_email, u.phone AS user_phone, "
            + "(SELECT COUNT(*) FROM events e WHERE e.organizer_id=o.organizer_id AND e.deleted_at IS NULL) AS event_count "
            + "FROM organizers o JOIN users u ON o.user_id=u.user_id ";

    public Organizer findByUserId(Connection c, long userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE o.user_id=?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Organizer findById(Connection c, long organizerId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE o.organizer_id=?")) {
            ps.setLong(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Organizer> list(Connection c, String status) throws SQLException {
        String sql = BASE + (status == null || status.isBlank() ? "WHERE 1=1 " : "WHERE o.verification_status=? ")
                + "ORDER BY CASE o.verification_status WHEN 'PENDING' THEN 0 WHEN 'VERIFIED' THEN 1 ELSE 2 END, "
                + "o.created_at DESC";
        List<Organizer> organizers = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (status != null && !status.isBlank()) {
                ps.setString(1, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    organizers.add(map(rs));
                }
            }
        }
        return organizers;
    }

    public long insert(Connection c, Organizer organizer) throws SQLException {
        String sql = "INSERT INTO organizers (user_id, organization_name, registration_no, contact_person, "
                + "website, verification_status, created_at) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, organizer.getUserId());
            ps.setString(2, organizer.getOrganizationName());
            ps.setString(3, organizer.getRegistrationNo());
            ps.setString(4, organizer.getContactPerson());
            ps.setString(5, organizer.getWebsite());
            ps.setString(6, organizer.getVerificationStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void update(Connection c, Organizer organizer) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE organizers SET organization_name=?, registration_no=?, contact_person=?, website=? "
                + "WHERE organizer_id=?")) {
            ps.setString(1, organizer.getOrganizationName());
            ps.setString(2, organizer.getRegistrationNo());
            ps.setString(3, organizer.getContactPerson());
            ps.setString(4, organizer.getWebsite());
            ps.setLong(5, organizer.getOrganizerId());
            ps.executeUpdate();
        }
    }

    public void updateVerification(Connection c, long organizerId, VerificationStatus status, long adminId)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE organizers SET verification_status=?, verified_at=CURRENT_TIMESTAMP, verified_by=? "
                + "WHERE organizer_id=?")) {
            ps.setString(1, status.name());
            ps.setLong(2, adminId);
            ps.setLong(3, organizerId);
            ps.executeUpdate();
        }
    }

    public long countPending(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM organizers WHERE verification_status='PENDING'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private Organizer map(ResultSet rs) throws SQLException {
        Organizer organizer = new Organizer();
        organizer.setOrganizerId(rs.getLong("organizer_id"));
        organizer.setUserId(rs.getLong("user_id"));
        organizer.setOrganizationName(text(rs, "organization_name"));
        organizer.setRegistrationNo(text(rs, "registration_no"));
        organizer.setContactPerson(text(rs, "contact_person"));
        organizer.setWebsite(text(rs, "website"));
        organizer.setVerificationStatus(VerificationStatus.parse(rs.getString("verification_status")));
        organizer.setVerifiedAt(dateTime(rs, "verified_at"));
        organizer.setVerifiedBy(nullableLong(rs, "verified_by"));
        organizer.setCreatedAt(dateTime(rs, "created_at"));
        organizer.setUserName(text(rs, "user_name"));
        organizer.setUserEmail(text(rs, "user_email"));
        organizer.setUserPhone(text(rs, "user_phone"));
        organizer.setEventCount(rs.getInt("event_count"));
        return organizer;
    }
}
