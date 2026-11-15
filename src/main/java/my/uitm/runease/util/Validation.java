package my.uitm.runease.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Server-side validation. Every form is validated again on the server even
 * though the browser performs HTML5 validation first (defence in depth).
 */
public class Validation {

    public static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    public static final Pattern PHONE =
            Pattern.compile("^[0-9+()\\-\\s]{8,20}$");
    public static final Pattern NAME =
            Pattern.compile("^[\\p{L}][\\p{L}\\s.'\\-]{2,119}$");
    public static final Pattern EVENT_TITLE =
            Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N}\\s.,'&()\\-:!/]{2,149}$");
    public static final Pattern BIB =
            Pattern.compile("^[A-Z0-9\\-]{3,15}$");

    private final Map<String, String> errors = new LinkedHashMap<>();

    public void required(String field, String label, String value) {
        if (value == null || value.isBlank()) {
            error(field, label + " is required.");
        }
    }

    public void email(String field, String value) {
        required(field, "Email address", value);
        if (value != null && !value.isBlank() && (value.length() > 160 || !EMAIL.matcher(value).matches())) {
            error(field, "Enter a valid email address, e.g. runner@example.com.");
        }
    }

    public void phone(String field, String label, String value, boolean required) {
        if (required) {
            required(field, label, value);
        }
        if (value != null && !value.isBlank() && !PHONE.matcher(value).matches()) {
            error(field, label + " must contain 8-20 digits and may include +, - or spaces.");
        }
    }

    public void name(String field, String label, String value) {
        required(field, label, value);
        if (value != null && !value.isBlank() && !NAME.matcher(value).matches()) {
            error(field, label + " must be 3-120 characters and contain letters only.");
        }
    }

    public void password(String field, String value) {
        required(field, "Password", value);
        if (value == null || value.isBlank()) {
            return;
        }
        if (value.length() < 8 || value.length() > 72) {
            error(field, "Password must be between 8 and 72 characters.");
        }
        if (!value.matches(".*[A-Z].*")) {
            error(field, "Password must contain at least one uppercase letter.");
        }
        if (!value.matches(".*[a-z].*")) {
            error(field, "Password must contain at least one lowercase letter.");
        }
        if (!value.matches(".*[0-9].*")) {
            error(field, "Password must contain at least one number.");
        }
        if (!value.matches(".*[^A-Za-z0-9].*")) {
            error(field, "Password must contain at least one special character.");
        }
    }

    public void matches(String field, String label, String value, String other) {
        if (value == null || !value.equals(other)) {
            error(field, label + " does not match.");
        }
    }

    public void length(String field, String label, String value, int max) {
        if (value != null && value.length() > max) {
            error(field, label + " cannot exceed " + max + " characters.");
        }
    }

    public void integer(String field, String label, String value, int min, int max) {
        required(field, label, value);
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            int number = Integer.parseInt(value.trim());
            if (number < min || number > max) {
                error(field, label + " must be between " + min + " and " + max + ".");
            }
        } catch (NumberFormatException e) {
            error(field, label + " must be a whole number.");
        }
    }

    public void decimal(String field, String label, String value, double min, double max) {
        required(field, label, value);
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            double number = Double.parseDouble(value.trim());
            if (number < min || number > max) {
                error(field, label + " must be between " + min + " and " + max + ".");
            }
        } catch (NumberFormatException e) {
            error(field, label + " must be a number.");
        }
    }

    public void date(String field, String label, String value, boolean required) {
        if (required || (value != null && !value.isBlank())) {
            required(field, label, value);
        }
        if (value != null && !value.isBlank()) {
            try {
                LocalDate.parse(value.trim());
            } catch (Exception e) {
                error(field, label + " is not a valid date.");
            }
        }
    }

    public void time(String field, String label, String value, boolean required) {
        if (required || (value != null && !value.isBlank())) {
            required(field, label, value);
        }
        if (value != null && !value.isBlank()) {
            try {
                LocalTime.parse(value.trim());
            } catch (Exception e) {
                error(field, label + " is not a valid time.");
            }
        }
    }

    public void dateNotBefore(String field, String label, String value, String otherValue) {
        if (value == null || otherValue == null || value.isBlank() || otherValue.isBlank()) {
            return;
        }
        try {
            LocalDate date = LocalDate.parse(value.trim());
            LocalDate other = LocalDate.parse(otherValue.trim());
            if (date.isBefore(other)) {
                error(field, label + " cannot be earlier than the registration opening date.");
            }
        } catch (Exception ignored) {
            // handled by date()
        }
    }

    public void money(String field, String label, String value, double max) {
        required(field, label, value);
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(value.trim());
            if (amount.signum() < 0 || amount.compareTo(BigDecimal.valueOf(max)) > 0) {
                error(field, label + " must be between 0.00 and " + max + ".");
            }
        } catch (NumberFormatException e) {
            error(field, label + " must be a valid amount.");
        }
    }

    public void oneOf(String field, String label, String value, String... allowed) {
        required(field, label, value);
        if (value == null) {
            return;
        }
        for (String option : allowed) {
            if (option.equals(value)) {
                return;
            }
        }
        error(field, "Select a valid " + label.toLowerCase() + ".");
    }

    public void error(String field, String message) {
        errors.putIfAbsent(field, message);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public String getFirstError() {
        return errors.values().stream().findFirst().orElse("Please review the highlighted fields.");
    }
}
