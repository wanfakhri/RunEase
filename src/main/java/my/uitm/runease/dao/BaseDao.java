package my.uitm.runease.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

abstract class BaseDao {

    protected static LocalDateTime dateTime(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    protected static LocalDate date(ResultSet rs, String column) throws SQLException {
        java.sql.Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    protected static LocalTime time(ResultSet rs, String column) throws SQLException {
        java.sql.Time value = rs.getTime(column);
        return value == null ? null : value.toLocalTime();
    }

    protected static BigDecimal decimal(ResultSet rs, String column) throws SQLException {
        return rs.getBigDecimal(column);
    }

    protected static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    protected static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    protected static String text(ResultSet rs, String column) throws SQLException {
        return rs.getString(column);
    }

    protected static String like(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
