package my.uitm.runease.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import my.uitm.runease.model.AccountStatus;
import my.uitm.runease.model.Role;
import my.uitm.runease.model.User;

/**
 * Route guard: protects every private area, enforces role-based access control
 * and destroys sessions that belong to users who were disabled by an admin.
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = Web.currentPath(req);
        Role required = requiredRole(path);
        boolean authenticatedOnly = path.equals("/profile");
        if (required == null && !authenticatedOnly) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(Web.SESSION_USER);

        if (user == null) {
            if (path.startsWith("/api/")) {
                Web.json(res, 401, ApiResponse.error("Your session has expired. Please sign in again."));
            } else {
                String next = req.getRequestURI();
                if (req.getQueryString() != null) {
                    next += "?" + req.getQueryString();
                }
                res.sendRedirect(req.getContextPath() + "/login?next="
                        + URLEncoder.encode(next, StandardCharsets.UTF_8));
            }
            return;
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            session.invalidate();
            if (path.startsWith("/api/")) {
                Web.json(res, 401, ApiResponse.error("This account is no longer active."));
            } else {
                res.sendRedirect(req.getContextPath() + "/login?expired=1");
            }
            return;
        }

        if (required != null && user.getRole() != required) {
            if (path.startsWith("/api/")) {
                Web.json(res, 403, ApiResponse.error("You are not authorised to perform this action."));
            } else {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private Role requiredRole(String path) {
        if (path.startsWith("/admin") || path.startsWith("/api/admin")) {
            return Role.ADMIN;
        }
        if (path.startsWith("/organizer") || path.startsWith("/api/organizer")) {
            return Role.ORGANIZER;
        }
        if (path.startsWith("/my") || path.startsWith("/api/my")
                || path.equals("/checkout") || path.equals("/registrations")
                || path.startsWith("/registrations/")) {
            return Role.PARTICIPANT;
        }
        return null;
    }
}
