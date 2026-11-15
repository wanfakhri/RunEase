package my.uitm.runease.web;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Ensures every session owns a CSRF token and validates it on state-changing
 * requests (POST/PUT/PATCH/DELETE). Tokens are submitted as the _csrf parameter
 * or the X-CSRF-Token header.
 */
public class CsrfFilter implements Filter {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();

        String token = (String) session.getAttribute(Web.SESSION_CSRF);
        if (token == null || token.isBlank()) {
            token = newToken();
            session.setAttribute(Web.SESSION_CSRF, token);
        }

        String method = req.getMethod();
        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")
                || method.equals("DELETE")) {
            String submitted = req.getHeader("X-CSRF-Token");
            if (submitted == null || submitted.isBlank()) {
                submitted = req.getParameter("_csrf");
            }
            if (submitted == null || !constantTimeEquals(token, submitted)) {
                if (Web.isJson(req)) {
                    Web.json(res, 403, ApiResponse.error(
                            "Your session has expired or the security token is invalid. Please refresh the page."));
                } else {
                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                return;
            }
        }
        chain.doFilter(request, response);
    }

    public static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
