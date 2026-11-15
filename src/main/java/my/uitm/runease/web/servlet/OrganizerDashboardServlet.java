package my.uitm.runease.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.User;
import my.uitm.runease.service.ReportService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/organizer")
public class OrganizerDashboardServlet extends BaseServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        User user = Web.user(req);
        try {
            req.setAttribute("stats", reportService.organizerOverview(user.getUserId()));
            page(req, "Organizer dashboard", Crumb.of("Dashboard"));
            render(req, res, "organizer/dashboard");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
