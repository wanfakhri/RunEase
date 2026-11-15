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

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        User user = Web.user(req);
        if (user == null) {
            redirect(req, res, "/login?next=/dashboard");
            return;
        }
        if (!user.isParticipant()) {
            redirect(req, res, user.getRole().getHome());
            return;
        }
        try {
            req.setAttribute("stats", reportService.participantOverview(user.getUserId()));
            page(req, "My dashboard", Crumb.of("Dashboard"));
            render(req, res, "participant/dashboard");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
