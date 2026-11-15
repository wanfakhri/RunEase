package my.uitm.runease.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import my.uitm.runease.model.User;

public final class Web {

    public static final String SESSION_USER = "user";
    public static final String SESSION_CSRF = "csrfToken";
    public static final String FLASH = "flashMessages";

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private Web() {
    }

    public static String param(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? null : value.trim();
    }

    public static String param(HttpServletRequest req, String name, String fallback) {
        String value = param(req, name);
        return value == null || value.isBlank() ? fallback : value;
    }

    public static int intParam(HttpServletRequest req, String name, int fallback) {
        try {
            return Integer.parseInt(param(req, name, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static long longParam(HttpServletRequest req, String name, long fallback) {
        try {
            return Long.parseLong(param(req, name, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static Long longParamOrNull(HttpServletRequest req, String name) {
        String value = param(req, name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static LocalDate dateParam(HttpServletRequest req, String name) {
        String value = param(req, name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalTime timeParam(HttpServletRequest req, String name) {
        String value = param(req, name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String normalised = value.length() == 5 ? value : value.substring(0, Math.min(5, value.length()));
            return LocalTime.parse(normalised);
        } catch (Exception e) {
            return null;
        }
    }

    public static BigDecimal decimalParam(HttpServletRequest req, String name) {
        String value = param(req, name);
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static User user(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute(SESSION_USER);
    }

    public static boolean isJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String requested = req.getHeader("X-Requested-With");
        return (accept != null && accept.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(requested);
    }

    public static String clientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    public static void flash(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession();
        @SuppressWarnings("unchecked")
        java.util.List<FlashMessage> messages =
                (java.util.List<FlashMessage>) session.getAttribute(FLASH);
        if (messages == null) {
            messages = new java.util.ArrayList<>();
            session.setAttribute(FLASH, messages);
        }
        messages.add(new FlashMessage(type, message));
    }

    public static Map<String, String> values(HttpServletRequest req, String... names) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String name : names) {
            values.put(name, param(req, name, ""));
        }
        return values;
    }

    public static void json(HttpServletResponse res, int status, Object body) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        res.getWriter().write(GSON.toJson(body));
    }

    public static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    public static String safeNext(String next) {
        if (next == null || next.isBlank()) {
            return null;
        }
        if (!next.startsWith("/") || next.startsWith("//") || next.contains("\\")
                || next.contains("://") || next.contains("\r") || next.contains("\n")) {
            return null;
        }
        return next;
    }

    public static String contextPath(HttpServletRequest req) {
        return req.getContextPath();
    }

    public static String currentPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        return uri.length() > context.length() ? uri.substring(context.length()) : "/";
    }
}
