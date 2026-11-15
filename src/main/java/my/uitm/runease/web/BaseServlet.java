package my.uitm.runease.web;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.User;
import my.uitm.runease.service.AppException;
import my.uitm.runease.util.Crumb;

public abstract class BaseServlet extends HttpServlet {

    protected void render(HttpServletRequest req, HttpServletResponse res, String view)
            throws ServletException, IOException {
        req.setAttribute("ctx", req.getContextPath());
        exposeLayout(req);
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, res);
    }

    /**
     * Publishes the current path and the layout the shared JSP fragments should
     * paint. Public marketing pages, the auth screens and the signed-in
     * application shell each share one header/footer pair.
     */
    private void exposeLayout(HttpServletRequest req) {
        String path = Web.currentPath(req);
        User user = Web.user(req);
        String layout = "public";
        if (user != null) {
            layout = "app";
        }
        if (path.equals("/login") || path.equals("/register")) {
            layout = "auth";
        }
        req.setAttribute("currentPath", path);
        req.setAttribute("layout", layout);
    }

    protected void page(HttpServletRequest req, String title, Crumb... crumbs) {
        req.setAttribute("pageTitle", title);
        req.setAttribute("crumbs", List.of(crumbs));
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse res, String location) throws IOException {
        res.sendRedirect(req.getContextPath() + location);
    }

    protected User currentUser(HttpServletRequest req) {
        User user = Web.user(req);
        if (user == null) {
            throw new IllegalStateException("No authenticated user in the request");
        }
        return user;
    }

    protected long pathId(HttpServletRequest req) throws AppException {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            throw new AppException("The requested record was not specified.", 404);
        }
        String[] segments = path.substring(1).split("/");
        try {
            return Long.parseLong(segments[0]);
        } catch (NumberFormatException e) {
            throw new AppException("The requested record was not found.", 404);
        }
    }

    @Override
    protected final void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            // HttpServlet has no PATCH hook, so dispatch it explicitly.
            if ("PATCH".equalsIgnoreCase(req.getMethod())) {
                doPatch(req, res);
                return;
            }
            super.service(req, res);
        } catch (Exception e) {
            // Servlet handlers declare checked service exceptions only, so unwrap
            // the application's own AppException here.
            if (e instanceof AppException app) {
                handleError(req, res, app.getStatus(), app.getMessage());
            } else if (e instanceof IllegalStateException) {
                log("Request failed: " + req.getRequestURI(), e);
                handleError(req, res, 500, "Something went wrong while processing your request. Please try again.");
            } else {
                log("Unhandled error for " + req.getRequestURI(), e);
                handleError(req, res, 500,
                        "An unexpected error occurred. The RunEase team has been notified. Please try again.");
            }
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse res, int status, String message)
            throws ServletException, IOException {
        if (res.isCommitted()) {
            return;
        }
        if (Web.currentPath(req).startsWith("/api/")) {
            Web.json(res, status, ApiResponse.error(message));
            return;
        }
        res.setStatus(status);
        req.setAttribute("status", status);
        req.setAttribute("message", message);
        req.setAttribute("pageTitle", status == 404 ? "Page not found" : "Something went wrong");
        exposeLayout(req);
        req.getRequestDispatcher("/WEB-INF/views/errors/error.jsp").forward(req, res);
    }
}
