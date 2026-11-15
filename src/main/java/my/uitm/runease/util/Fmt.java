package my.uitm.runease.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class Fmt {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_LONG = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private Fmt() {
    }

    public static String date(LocalDate value) {
        return value == null ? "-" : value.format(DATE);
    }

    public static String dateLong(LocalDate value) {
        return value == null ? "-" : value.format(DATE_LONG);
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME);
    }

    public static String time(LocalTime value) {
        return value == null ? "-" : value.format(TIME);
    }

    public static String month(LocalDate value) {
        return value == null ? "-" : value.format(MONTH).toUpperCase(Locale.ENGLISH);
    }

    public static String money(BigDecimal value) {
        return value == null ? "RM 0.00" : "RM " + MONEY.format(value);
    }

    public static String number(long value) {
        return String.format(Locale.ENGLISH, "%,d", value);
    }

    public static String duration(Integer seconds) {
        if (seconds == null || seconds <= 0) {
            return "-";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, secs)
                : String.format("%d:%02d", minutes, secs);
    }

    public static String pace(Integer secondsPerKm) {
        if (secondsPerKm == null || secondsPerKm <= 0) {
            return "-";
        }
        return String.format("%d:%02d /km", secondsPerKm / 60, secondsPerKm % 60);
    }

    public static long daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }
}
