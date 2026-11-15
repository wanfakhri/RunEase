package my.uitm.runease.web.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.service.EventService;
import my.uitm.runease.service.ReportService;
import my.uitm.runease.web.BaseServlet;

@WebServlet("")
public class HomeServlet extends BaseServlet {

    private final EventService eventService = new EventService();
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            req.setAttribute("featured", eventService.featuredEvents(6));
            req.setAttribute("stats", reportService.publicOverview());
            page(req, "Run further, together");
            render(req, res, "home");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
