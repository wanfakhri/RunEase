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

import my.uitm.runease.model.AccountStatus;
import my.uitm.runease.model.Role;
import my.uitm.runease.model.User;

public class UserDao extends BaseDao {

    private static final String COLUMNS =
            "user_id, full_name, email, password_hash, phone, role, status, gender, date_of_birth, city, "
            + "last_login_at, created_at, updated_at, deleted_at";

    private static final Map<String, String> SORTS = new LinkedHashMap<>();

    static {
        SORTS.put("name", "u.full_name");
        SORTS.put("email", "u.email");
        SORTS.put("role", "u.role");
        SORTS.put("status", "u.status");
        SORTS.put("lastLogin", "u.last_login_at");
        SORTS.put("created", "u.created_at");
    }

    public User findByEmail(Connection c, String email) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE LOWER(email) = ? AND deleted_at IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public User findById(Connection c, long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE user_id = ? AND deleted_at IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean emailExists(Connection c, String email, Long excludeUserId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = ? AND deleted_at IS NULL"
                + (excludeUserId == null ? "" : " AND user_id <> ?");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            if (excludeUserId != null) {
                ps.setLong(2, excludeUserId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public long insert(Connection c, User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, phone, role, status, gender, "
                + "date_of_birth, city, created_at) VALUES (?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail().toLowerCase());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getStatus().name());
            ps.setString(7, user.getGender());
            if (user.getDateOfBirth() == null) {
                ps.setNull(8, java.sql.Types.DATE);
            } else {
                ps.setDate(8, java.sql.Date.valueOf(user.getDateOfBirth()));
            }
            ps.setString(9, user.getCity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void update(Connection c, User user) throws SQLException {
        String sql = "UPDATE users SET full_name=?, email=?, phone=?, role=?, status=?, gender=?, "
                + "date_of_birth=?, city=?, updated_at=CURRENT_TIMESTAMP WHERE user_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail().toLowerCase());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getStatus().name());
            ps.setString(6, user.getGender());
            if (user.getDateOfBirth() == null) {
                ps.setNull(7, java.sql.Types.DATE);
            } else {
                ps.setDate(7, java.sql.Date.valueOf(user.getDateOfBirth()));
            }
            ps.setString(8, user.getCity());
            ps.setLong(9, user.getUserId());
            ps.executeUpdate();
        }
    }

    public void updateProfile(Connection c, User user) throws SQLException {
        String sql = "UPDATE users SET full_name=?, phone=?, gender=?, date_of_birth=?, city=?, "
                + "updated_at=CURRENT_TIMESTAMP WHERE user_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getGender());
            if (user.getDateOfBirth() == null) {
                ps.setNull(4, java.sql.Types.DATE);
            } else {
                ps.setDate(4, java.sql.Date.valueOf(user.getDateOfBirth()));
            }
            ps.setString(5, user.getCity());
            ps.setLong(6, user.getUserId());
            ps.executeUpdate();
        }
    }

    public void updatePassword(Connection c, long userId, String hash) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE users SET password_hash=?, updated_at=CURRENT_TIMESTAMP WHERE user_id=?")) {
            ps.setString(1, hash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection c, long userId, AccountStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE users SET status=?, updated_at=CURRENT_TIMESTAMP WHERE user_id=?")) {
            ps.setString(1, status.name());
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public void touchLogin(Connection c, long userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE users SET last_login_at=CURRENT_TIMESTAMP WHERE user_id=?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    public void softDelete(Connection c, long userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE users SET deleted_at=CURRENT_TIMESTAMP, status='INACTIVE' WHERE user_id=?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    public List<User> list(Connection c, String q, String role, String status,
                           String sort, String dir, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, role, status, params);
        String order = SORTS.getOrDefault(sort, "u.created_at") + ("asc".equals(dir) ? " ASC" : " DESC");
        String sql = "SELECT " + COLUMNS + " FROM users u " + where
                + " ORDER BY " + order + ", u.user_id DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(map(rs));
                }
            }
        }
        return users;
    }

    public long count(Connection c, String q, String role, String status) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, role, status, params);
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users u " + where)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public long countByRole(Connection c, Role role) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role=? AND deleted_at IS NULL")) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public long countActive(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE deleted_at IS NULL AND status='ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private String buildWhere(String q, String role, String status, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE u.deleted_at IS NULL");
        if (q != null && !q.isBlank()) {
            where.append(" AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ? OR u.phone LIKE ?)");
            String term = like(q);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (role != null && !role.isBlank()) {
            where.append(" AND u.role = ?");
            params.add(role);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND u.status = ?");
            params.add(status);
        }
        return where.toString();
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setFullName(text(rs, "full_name"));
        user.setEmail(text(rs, "email"));
        user.setPasswordHash(text(rs, "password_hash"));
        user.setPhone(text(rs, "phone"));
        user.setRole(Role.parse(rs.getString("role")));
        user.setStatus(AccountStatus.parse(rs.getString("status")));
        user.setGender(text(rs, "gender"));
        user.setDateOfBirth(date(rs, "date_of_birth"));
        user.setCity(text(rs, "city"));
        user.setLastLoginAt(dateTime(rs, "last_login_at"));
        user.setCreatedAt(dateTime(rs, "created_at"));
        user.setUpdatedAt(dateTime(rs, "updated_at"));
        user.setDeletedAt(dateTime(rs, "deleted_at"));
        return user;
    }
}
