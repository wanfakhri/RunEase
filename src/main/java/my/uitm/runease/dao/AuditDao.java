package my.uitm.runease.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import my.uitm.runease.model.AuditLog;

public class AuditDao extends BaseDao {

    public void log(Connection c, Long userId, String action, String entityType, String entityId,
                    String description, String ip) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, description, ip_address, created_at) "
                + "VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)")) {
            if (userId == null) {
                ps.setNull(1, java.sql.Types.BIGINT);
            } else {
                ps.setLong(1, userId);
            }
            ps.setString(2, action);
            ps.setString(3, entityType);
            ps.setString(4, entityId);
            ps.setString(5, description);
            ps.setString(6, ip);
            ps.executeUpdate();
        }
    }

    public List<AuditLog> list(Connection c, String q, String action, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, action, params);
        String sql = "SELECT a.*, u.full_name AS user_name FROM audit_logs a "
                + "LEFT JOIN users u ON a.user_id=u.user_id " + where
                + " ORDER BY a.created_at DESC, a.log_id DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<AuditLog> logs = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(map(rs));
                }
            }
        }
        return logs;
    }

    public long count(Connection c, String q, String action) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, action, params);
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM audit_logs a LEFT JOIN users u ON a.user_id=u.user_id " + where)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private String buildWhere(String q, String action, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1");
        if (q != null && !q.isBlank()) {
            where.append(" AND (LOWER(a.description) LIKE ? OR LOWER(u.full_name) LIKE ? "
                    + "OR LOWER(a.entity_type) LIKE ?)");
            String term = like(q);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (action != null && !action.isBlank()) {
            where.append(" AND a.action=?");
            params.add(action);
        }
        return where.toString();
    }

    private AuditLog map(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getLong("log_id"));
        log.setUserId(nullableLong(rs, "user_id"));
        log.setUserName(text(rs, "user_name"));
        log.setAction(text(rs, "action"));
        log.setEntityType(text(rs, "entity_type"));
        log.setEntityId(text(rs, "entity_id"));
        log.setDescription(text(rs, "description"));
        log.setIpAddress(text(rs, "ip_address"));
        log.setCreatedAt(dateTime(rs, "created_at"));
        return log;
    }
}
