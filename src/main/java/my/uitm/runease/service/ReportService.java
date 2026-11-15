package my.uitm.runease.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import my.uitm.runease.config.Db;
import my.uitm.runease.dao.AnnouncementDao;
import my.uitm.runease.dao.EventDao;
import my.uitm.runease.dao.OrganizerDao;
import my.uitm.runease.dao.PaymentDao;
import my.uitm.runease.dao.RegistrationDao;
import my.uitm.runease.dao.StatsDao;
import my.uitm.runease.dao.UserDao;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.EventStatus;
import my.uitm.runease.model.Organizer;
import my.uitm.runease.model.Registration;
import my.uitm.runease.model.RegistrationStatus;
import my.uitm.runease.model.Role;
import my.uitm.runease.model.User;

/**
 * Read-only reporting facade used by the three dashboards, the organizer
 * reports and the administrator analytics page. Aggregation is pushed to SQL
 * so the JSP layer only renders numbers it is given.
 */
public class ReportService {

    private final UserDao userDao = new UserDao();
    private final EventDao eventDao = new EventDao();
    private final RegistrationDao registrationDao = new RegistrationDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final OrganizerDao organizerDao = new OrganizerDao();
    private final AnnouncementDao announcementDao = new AnnouncementDao();
    private final StatsDao statsDao = new StatsDao();

    /** Figures shown on the public landing page. */
    public Map<String, Object> publicOverview() throws Exception {
        return Db.read(c -> {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("events", eventDao.countPublic(c, null, null, null, false));
            stats.put("runners", registrationDao.countAll(c));
            stats.put("cities", eventDao.listStates(c).size());
            stats.put("revenue", paymentDao.totalRevenue(c));
            return stats;
        });
    }

    public Map<String, Object> adminOverview() throws Exception {
        return Db.read(c -> {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("usersByRole", statsDao.usersByRole(c));
            stats.put("eventsByStatus", statsDao.eventStatuses(c, 0));
            stats.put("registrationsByStatus", statsDao.registrationStatuses(c, 0));
            stats.put("monthlyRegistrations", statsDao.monthlyRegistrations(c, 0, 6));
            stats.put("monthlyRevenue", statsDao.monthlyRevenue(c, 0, 6));
            stats.put("topEvents", statsDao.topEvents(c, 0, 5));
            stats.put("recentRegistrations", registrationDao.recent(c, 6));
            stats.put("totalUsers", userDao.countActive(c));
            stats.put("totalEvents", eventDao.countAll(c));
            stats.put("pendingEvents", eventDao.countByStatus(c, EventStatus.PENDING));
            stats.put("approvedEvents", eventDao.countByStatus(c, EventStatus.APPROVED));
            stats.put("pendingOrganizers", organizerDao.countPending(c));
            stats.put("totalRegistrations", registrationDao.countAll(c));
            stats.put("confirmedRegistrations", registrationDao.countAllByStatus(c, RegistrationStatus.CONFIRMED));
            stats.put("totalRevenue", paymentDao.totalRevenue(c));
            stats.put("revenueThisMonth", paymentDao.revenueThisMonth(c));
            stats.put("registrationsThisMonth", statsDao.registrationsThisMonth(c, 0));
            stats.put("checkInsToday", statsDao.checkInsToday(c, 0));
            return stats;
        });
    }

    public Map<String, Object> organizerOverview(long userId) throws Exception {
        return Db.read(c -> {
            Organizer organizer = organizerDao.findByUserId(c, userId);
            if (organizer == null) {
                throw new AppException("Organizer profile not found.", 403);
            }
            long organizerId = organizer.getOrganizerId();
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("organizer", organizer);
            stats.put("eventsByStatus", statsDao.eventStatuses(c, organizerId));
            stats.put("registrationsByStatus", statsDao.registrationStatuses(c, organizerId));
            stats.put("monthlyRegistrations", statsDao.monthlyRegistrations(c, organizerId, 6));
            stats.put("monthlyRevenue", statsDao.monthlyRevenue(c, organizerId, 6));
            stats.put("topEvents", statsDao.topEvents(c, organizerId, 5));
            stats.put("recentRegistrations", registrationDao.recentForOrganizer(c, organizerId, 6));
            stats.put("upcomingEvents", eventDao.upcomingByOrganizer(c, organizerId, 4));
            stats.put("totalEvents", eventDao.countByOrganizer(c, organizerId, null, null));
            stats.put("approvedEvents", eventDao.countByOrganizer(c, organizerId, null, "APPROVED"));
            stats.put("pendingEvents", eventDao.countByOrganizer(c, organizerId, null, "PENDING"));
            stats.put("draftEvents", eventDao.countByOrganizer(c, organizerId, null, "DRAFT"));
            stats.put("totalRegistrations", registrationDao.countForOrganizerByStatus(c, organizerId, null));
            stats.put("confirmedRegistrations",
                    registrationDao.countForOrganizerByStatus(c, organizerId, RegistrationStatus.CONFIRMED));
            stats.put("checkedInRegistrations",
                    registrationDao.countForOrganizerByStatus(c, organizerId, RegistrationStatus.CHECKED_IN));
            stats.put("pendingPayments",
                    registrationDao.countForOrganizerByStatus(c, organizerId, RegistrationStatus.PENDING_PAYMENT));
            stats.put("revenue", paymentDao.revenueForOrganizer(c, organizerId));
            stats.put("revenueThisMonth", paymentDao.revenueThisMonth(c));
            stats.put("registrationsThisMonth", statsDao.registrationsThisMonth(c, organizerId));
            stats.put("checkInsToday", statsDao.checkInsToday(c, organizerId));
            return stats;
        });
    }

    public Map<String, Object> participantOverview(long userId) throws Exception {
        return Db.read(c -> {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalRegistrations", registrationDao.countByUser(c, userId, null));
            stats.put("confirmed", registrationDao.countByUser(c, userId, "CONFIRMED"));
            stats.put("pendingPayments", registrationDao.countByUser(c, userId, "PENDING_PAYMENT"));
            stats.put("checkedIn", registrationDao.countByUser(c, userId, "CHECKED_IN"));
            stats.put("finished", registrationDao.countByUser(c, userId, "FINISHED"));
            stats.put("totalDistance", registrationDao.totalDistanceForUser(c, userId));
            stats.put("nextEvents", registrationDao.upcomingForUser(c, userId, 3));
            stats.put("registrations", registrationDao.listByUser(c, userId, null, "event", "asc", 0, 5));
            stats.put("announcements", announcementDao.listForParticipant(c, userId));
            return stats;
        });
    }

    public List<User> participants(String q, String status, int offset, int limit) throws Exception {
        return Db.read(c -> userDao.list(c, q, Role.PARTICIPANT.name(), status, "created", "desc", offset, limit));
    }

    public long countParticipants(String q, String status) throws Exception {
        return Db.read(c -> userDao.count(c, q, Role.PARTICIPANT.name(), status));
    }

    public BigDecimal revenueForOrganizer(long userId) throws Exception {
        return Db.read(c -> {
            Organizer organizer = organizerDao.findByUserId(c, userId);
            return organizer == null ? BigDecimal.ZERO : paymentDao.revenueForOrganizer(c, organizer.getOrganizerId());
        });
    }

    public BigDecimal totalRevenue() throws Exception {
        return Db.read(paymentDao::totalRevenue);
    }

    public List<Registration> recentRegistrations(int limit) throws Exception {
        return Db.read(c -> registrationDao.recent(c, limit));
    }

    public List<Event> upcomingEvents() throws Exception {
        return Db.read(c -> eventDao.featured(c, 6));
    }
}
