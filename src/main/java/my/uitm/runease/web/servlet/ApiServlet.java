package my.uitm.runease.web.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.AccountStatus;
import my.uitm.runease.model.Role;
import my.uitm.runease.model.User;
import my.uitm.runease.model.VerificationStatus;
import my.uitm.runease.service.AppException;
import my.uitm.runease.service.AuthService;
import my.uitm.runease.util.Validation;
import my.uitm.runease.web.ApiResponse;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

/**
 * JSON API for the administrator's user-management screen. Demonstrates the
 * asynchronous create / read / update / delete path required by the project
 * rubric while keeping every rule validated again on the server.
 */
@WebServlet("/api/admin/*")
public class ApiServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String[] segments = segments(req);
        if (segments.length == 0) {
            throw new AppException("API endpoint not found.", 404);
        }
        if (segments[0].equals("users")) {
            if (segments.length == 1) {
                listUsers(req, res);
                return;
            }
            long id = parseId(segments[1]);
            User user;
            try {
                user = authService.findById(id);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            if (user == null) {
                throw new AppException("User not found.", 404);
            }
            Web.json(res, 200, ApiResponse.ok("User loaded.", userMap(user)));
            return;
        }
        throw new AppException("API endpoint not found.", 404);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String[] segments = segments(req);
        if (segments.length >= 3 && segments[0].equals("organizers") && segments[2].equals("verify")) {
            verifyOrganizer(req, res, parseId(segments[1]));
            return;
        }
        if (segments.length == 1 && segments[0].equals("users")) {
            createUser(req, res);
            return;
        }
        throw new AppException("API endpoint not found.", 404);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String[] segments = segments(req);
        if (segments.length >= 2 && segments[0].equals("users")) {
            updateUser(req, res, parseId(segments[1]));
            return;
        }
        throw new AppException("API endpoint not found.", 404);
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String[] segments = segments(req);
        if (segments.length >= 3 && segments[0].equals("users") && segments[2].equals("status")) {
            updateStatus(req, res, parseId(segments[1]));
            return;
        }
        throw new AppException("API endpoint not found.", 404);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String[] segments = segments(req);
        if (segments.length >= 2 && segments[0].equals("users")) {
            deleteUser(req, res, parseId(segments[1]));
            return;
        }
        throw new AppException("API endpoint not found.", 404);
    }

    private void listUsers(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String q = Web.param(req, "q", "");
        String role = Web.param(req, "role", "");
        String status = Web.param(req, "status", "");
        String sort = Web.param(req, "sort", "created");
        String dir = Web.param(req, "dir", "desc");
        int page = Math.max(1, Web.intParam(req, "page", 1));
        int size = Math.min(100, Math.max(5, Web.intParam(req, "size", 10)));
        try {
            List<User> users = authService.listUsers(q, role, status, sort, dir, (page - 1) * size, size);
            long total = authService.countUsers(q, role, status);
            List<Map<String, Object>> items = new ArrayList<>();
            for (User user : users) {
                items.add(userMap(user));
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("items", items);
            data.put("page", page);
            data.put("size", size);
            data.put("total", total);
            data.put("totalPages", Math.max(1, (int) Math.ceil(total / (double) size)));
            Web.json(res, 200, ApiResponse.ok("Users loaded.", data));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void createUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonObject body = body(req);
        String fullName = text(body, "fullName");
        String email = text(body, "email");
        String phone = text(body, "phone");
        String city = text(body, "city");
        String password = text(body, "password");
        String roleRaw = text(body, "role");
        String statusRaw = text(body, "status");
        String organizationName = text(body, "organizationName");

        Validation validation = new Validation();
        validation.name("fullName", "Full name", fullName);
        validation.email("email", email);
        validation.phone("phone", "Phone number", phone, false);
        validation.password("password", password);
        validation.length("city", "City", city, 80);
        validation.oneOf("role", "Role", roleRaw, "ADMIN", "ORGANIZER", "PARTICIPANT");
        if (!statusRaw.isBlank()) {
            validation.oneOf("status", "Status", statusRaw, "ACTIVE", "INACTIVE", "SUSPENDED");
        }
        if (validation.hasErrors()) {
            Web.json(res, 422, ApiResponse.validation("Please review the highlighted fields.",
                    validation.getErrors()));
            return;
        }
        try {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setCity(city);
            user.setRole(Role.parse(roleRaw));
            user.setStatus(statusRaw.isBlank() ? AccountStatus.ACTIVE : AccountStatus.parse(statusRaw));
            User admin = currentUser(req);
            authService.createUser(user, password, organizationName, admin.getUserId(), Web.clientIp(req));
            Web.json(res, 201, ApiResponse.ok("User " + email + " created.", null));
        } catch (AppException e) {
            Web.json(res, 422, ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void updateUser(HttpServletRequest req, HttpServletResponse res, long id) throws IOException {
        JsonObject body = body(req);
        String fullName = text(body, "fullName");
        String email = text(body, "email");
        String phone = text(body, "phone");
        String city = text(body, "city");
        String password = text(body, "password");
        String roleRaw = text(body, "role");
        String statusRaw = text(body, "status");

        Validation validation = new Validation();
        validation.name("fullName", "Full name", fullName);
        validation.email("email", email);
        validation.phone("phone", "Phone number", phone, false);
        validation.length("city", "City", city, 80);
        validation.oneOf("role", "Role", roleRaw, "ADMIN", "ORGANIZER", "PARTICIPANT");
        validation.oneOf("status", "Status", statusRaw, "ACTIVE", "INACTIVE", "SUSPENDED");
        if (password != null && !password.isBlank()) {
            validation.password("password", password);
        }
        if (validation.hasErrors()) {
            Web.json(res, 422, ApiResponse.validation("Please review the highlighted fields.",
                    validation.getErrors()));
            return;
        }
        try {
            User existing = authService.findById(id);
            if (existing == null) {
                throw new AppException("User not found.", 404);
            }
            User admin = currentUser(req);
            Role role = Role.parse(roleRaw);
            AccountStatus status = AccountStatus.parse(statusRaw);
            if (id == admin.getUserId() && role != Role.ADMIN) {
                throw new AppException("You cannot remove your own administrator role.");
            }
            if (id == admin.getUserId() && status != AccountStatus.ACTIVE) {
                throw new AppException("You cannot deactivate your own administrator account.");
            }
            existing.setFullName(fullName);
            existing.setEmail(email);
            existing.setPhone(phone);
            existing.setCity(city);
            existing.setRole(role);
            existing.setStatus(status);
            authService.updateUser(existing, password, admin.getUserId(), Web.clientIp(req));
            User refreshed = authService.findById(id);
            Web.json(res, 200, ApiResponse.ok("User updated.", userMap(refreshed)));
        } catch (AppException e) {
            Web.json(res, 422, ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void updateStatus(HttpServletRequest req, HttpServletResponse res, long id) throws IOException {
        JsonObject body = body(req);
        String statusRaw = text(body, "status");
        Validation validation = new Validation();
        validation.oneOf("status", "Status", statusRaw, "ACTIVE", "INACTIVE", "SUSPENDED");
        if (validation.hasErrors()) {
            Web.json(res, 422, ApiResponse.validation("Select a valid status.", validation.getErrors()));
            return;
        }
        try {
            User admin = currentUser(req);
            authService.setUserStatus(id, AccountStatus.parse(statusRaw), admin.getUserId(), Web.clientIp(req));
            Web.json(res, 200, ApiResponse.ok("Account status changed to "
                    + AccountStatus.parse(statusRaw).getLabel() + ".", null));
        } catch (AppException e) {
            Web.json(res, 422, ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse res, long id) throws IOException {
        try {
            User admin = currentUser(req);
            authService.deleteUser(id, admin.getUserId(), Web.clientIp(req));
            Web.json(res, 200, ApiResponse.ok("User deleted.", null));
        } catch (AppException e) {
            Web.json(res, 422, ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void verifyOrganizer(HttpServletRequest req, HttpServletResponse res, long organizerId)
            throws IOException {
        JsonObject body = body(req);
        String statusRaw = text(body, "status");
        Validation validation = new Validation();
        validation.oneOf("status", "Status", statusRaw, "VERIFIED", "REJECTED");
        if (validation.hasErrors()) {
            Web.json(res, 422, ApiResponse.validation("Select a valid verification status.",
                    validation.getErrors()));
            return;
        }
        try {
            User admin = currentUser(req);
            VerificationStatus status = VerificationStatus.parse(statusRaw);
            authService.verifyOrganizer(organizerId, status, admin.getUserId(), Web.clientIp(req));
            Web.json(res, 200, ApiResponse.ok("Organizer " + status.getLabel().toLowerCase() + ".", null));
        } catch (AppException e) {
            Web.json(res, 422, ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Object> userMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", user.getUserId());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole() == null ? "" : user.getRole().name());
        map.put("roleLabel", user.getRoleLabel());
        map.put("status", user.getStatus() == null ? "" : user.getStatus().name());
        map.put("statusLabel", user.getStatusLabel());
        map.put("statusBadge", user.getStatusBadge());
        map.put("gender", user.getGender());
        map.put("dateOfBirth", user.getDateOfBirth() == null ? "" : user.getDateOfBirth().toString());
        map.put("city", user.getCity());
        map.put("initials", user.getInitials());
        map.put("lastLogin", user.getLastLoginFormatted());
        map.put("createdAt", user.getCreatedAtFormatted());
        return map;
    }

    private String[] segments(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            return new String[0];
        }
        return path.substring(1).split("/");
    }

    private long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new AppException("Record not found.", 404);
        }
    }

    private JsonObject body(HttpServletRequest req) throws IOException {
        String raw = req.getReader().lines().collect(Collectors.joining("\n"));
        if (raw == null || raw.isBlank()) {
            return new JsonObject();
        }
        try {
            return JsonParser.parseString(raw).getAsJsonObject();
        } catch (Exception e) {
            throw new AppException("The request body is not valid JSON.", 400);
        }
    }

    private String text(JsonObject body, String key) {
        if (body == null || !body.has(key) || body.get(key).isJsonNull()) {
            return "";
        }
        try {
            return body.get(key).getAsString().trim();
        } catch (Exception e) {
            return "";
        }
    }
}
