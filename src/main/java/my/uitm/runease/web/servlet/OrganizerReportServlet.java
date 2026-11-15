package my.uitm.runease.web.servlet;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.Registration;
import my.uitm.runease.model.RegistrationStatus;
import my.uitm.runease.model.User;
import my.uitm.runease.service.EventService;
import my.uitm.runease.service.RegistrationService;
import my.uitm.runease.service.ReportService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.util.Page;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/organizer/reports")
public class OrganizerReportServlet extends BaseServlet {

    private static final int PAGE_SIZE = 15;

    private final RegistrationService registrationService = new RegistrationService();
    private final EventService eventService = new EventService();
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        User user = currentUser(req);
        Long eventId = Web.longParamOrNull(req, "eventId");
        String q = Web.param(req, "q", "");
        String status = Web.param(req, "status", "");
        String sort = Web.param(req, "sort", "registered");
        String dir = Web.param(req, "dir", "desc");
        int page = Math.max(1, Web.intParam(req, "page", 1));

        try {
            if ("csv".equals(Web.param(req, "export", ""))) {
                exportCsv(req, res, user, eventId, q, status);
                return;
            }
            List<Registration> registrations = registrationService.organizerRegistrations(user.getUserId(),
                    eventId, q, status, sort, dir, (page - 1) * PAGE_SIZE, PAGE_SIZE);
            long total = registrationService.countOrganizerRegistrations(user.getUserId(), eventId, q, status);
            Map<String, String> filters = new LinkedHashMap<>();
            filters.put("eventId", eventId == null ? "" : String.valueOf(eventId));
            filters.put("q", q);
            filters.put("status", status);
            req.setAttribute("registrations", registrations);
            req.setAttribute("pager", new Page<>(registrations, page, PAGE_SIZE, total, filters, sort, dir));
            req.setAttribute("filters", filters);
            req.setAttribute("statuses", RegistrationStatus.values());
            List<Event> events = eventService.organizerEvents(user.getUserId(), "", "", "date", "desc", 0, 100);
            req.setAttribute("organizedEvents", events);
            req.setAttribute("selectedEventId", eventId);
            req.setAttribute("stats", reportService.organizerOverview(user.getUserId()));
            page(req, "Reports", Crumb.link("Dashboard", "/organizer"), Crumb.of("Reports"));
            render(req, res, "organizer/reports");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void exportCsv(HttpServletRequest req, HttpServletResponse res, User user, Long eventId,
                           String q, String status) throws Exception {
        List<Registration> rows = registrationService.organizerRegistrations(user.getUserId(), eventId, q,
                status, "registered", "desc", 0, 5000);
        res.setContentType("text/csv;charset=UTF-8");
        res.setHeader("Content-Disposition", "attachment; filename=\"runease-registrations.csv\"");
        res.setHeader("Cache-Control", "no-store");
        PrintWriter out = res.getWriter();
        out.write('\ufeff');
        out.println("Registration code,Participant,Email,Phone,Event,Category,Status,Amount,Payment status,"
                + "Bib,Registered at,Checked in at");
        for (Registration r : rows) {
            out.println(String.join(",",
                    csv(r.getRegistrationCode()),
                    csv(r.getParticipantName()),
                    csv(r.getParticipantEmail()),
                    csv(r.getParticipantPhone()),
                    csv(r.getEventTitle()),
                    csv(r.getCategoryName()),
                    csv(r.getStatusLabel()),
                    csv(r.getAmountFormatted()),
                    csv(r.getPayment() == null ? "-" : r.getPayment().getStatusLabel()),
                    csv(r.getBibDisplay()),
                    csv(r.getRegisteredAtFormatted()),
                    csv(r.getCheckedInAtFormatted())));
        }
        out.flush();
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
