package my.uitm.runease.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.uitm.runease.model.Result;

public class ResultDao extends BaseDao {

    public Result findByRegistration(Connection c, long registrationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM results WHERE registration_id=?")) {
            ps.setLong(1, registrationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void save(Connection c, long registrationId, int finishSeconds, long recordedBy) throws SQLException {
        Result existing = findByRegistration(c, registrationId);
        if (existing == null) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO results (registration_id, finish_time_seconds, recorded_by, recorded_at) "
                    + "VALUES (?,?,?,CURRENT_TIMESTAMP)")) {
                ps.setLong(1, registrationId);
                ps.setInt(2, finishSeconds);
                ps.setLong(3, recordedBy);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE results SET finish_time_seconds=?, recorded_by=?, recorded_at=CURRENT_TIMESTAMP "
                    + "WHERE registration_id=?")) {
                ps.setInt(1, finishSeconds);
                ps.setLong(2, recordedBy);
                ps.setLong(3, registrationId);
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE registrations SET status='FINISHED' WHERE registration_id=? AND status IN "
                + "('CONFIRMED','CHECKED_IN','FINISHED')")) {
            ps.setLong(1, registrationId);
            ps.executeUpdate();
        }
        recalculatePositions(c, registrationId);
    }

    public void delete(Connection c, long registrationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM results WHERE registration_id=?")) {
            ps.setLong(1, registrationId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE registrations SET status='CHECKED_IN' WHERE registration_id=? AND status='FINISHED'")) {
            ps.setLong(1, registrationId);
            ps.executeUpdate();
        }
    }

    /**
     * Recomputes overall and category positions plus pace for every finisher of
     * the event that owns the given registration.
     */
    private void recalculatePositions(Connection c, long registrationId) throws SQLException {
        long eventId;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT c.event_id FROM registrations r JOIN event_categories c ON r.category_id=c.category_id "
                + "WHERE r.registration_id=?")) {
            ps.setLong(1, registrationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                eventId = rs.getLong(1);
            }
        }

        String sql = "SELECT res.result_id, res.finish_time_seconds, r.category_id, c.distance_km "
                + "FROM results res JOIN registrations r ON res.registration_id=r.registration_id "
                + "JOIN event_categories c ON r.category_id=c.category_id "
                + "WHERE c.event_id=? ORDER BY res.finish_time_seconds ASC, res.result_id ASC";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getLong("result_id"),
                            rs.getInt("finish_time_seconds"),
                            rs.getLong("category_id"),
                            rs.getDouble("distance_km")
                    });
                }
            }
        }

        Map<Long, Integer> categoryCounters = new HashMap<>();
        int overall = 0;
        for (Object[] row : rows) {
            overall++;
            long categoryId = (Long) row[2];
            int categoryPosition = categoryCounters.merge(categoryId, 1, Integer::sum);
            int seconds = (Integer) row[1];
            double distance = (Double) row[3];
            int pace = distance > 0 ? (int) Math.round(seconds / distance) : 0;
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE results SET overall_position=?, category_position=?, pace_seconds=? WHERE result_id=?")) {
                ps.setInt(1, overall);
                ps.setInt(2, categoryPosition);
                ps.setInt(3, pace);
                ps.setLong(4, (Long) row[0]);
                ps.executeUpdate();
            }
        }
    }

    private Result map(ResultSet rs) throws SQLException {
        Result result = new Result();
        result.setResultId(rs.getLong("result_id"));
        result.setRegistrationId(rs.getLong("registration_id"));
        result.setFinishTimeSeconds(rs.getInt("finish_time_seconds"));
        result.setOverallPosition(nullableInt(rs, "overall_position"));
        result.setCategoryPosition(nullableInt(rs, "category_position"));
        result.setPaceSeconds(nullableInt(rs, "pace_seconds"));
        result.setRecordedBy(nullableLong(rs, "recorded_by"));
        result.setRecordedAt(dateTime(rs, "recorded_at"));
        return result;
    }
}
