package my.uitm.runease.service;

import java.math.BigDecimal;
import java.sql.Connection;import java.time.LocalDate;
import java.util.List;

import my.uitm.runease.config.Db;
import my.uitm.runease.dao.AuditDao;
import my.uitm.runease.dao.CategoryDao;
import my.uitm.runease.dao.EventDao;
import my.uitm.runease.dao.OrganizerDao;
import my.uitm.runease.dao.PaymentDao;
import my.uitm.runease.dao.RegistrationDao;
import my.uitm.runease.dao.ResultDao;
import my.uitm.runease.model.Event;
import my.uitm.runease.model.EventCategory;
import my.uitm.runease.model.EventStatus;
import my.uitm.runease.model.Payment;
import my.uitm.runease.model.PaymentStatus;
import my.uitm.runease.model.Registration;
import my.uitm.runease.model.RegistrationStatus;

public class RegistrationService {

    private final RegistrationDao registrationDao = new RegistrationDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final EventDao eventDao = new EventDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final ResultDao resultDao = new ResultDao();
    private final OrganizerDao organizerDao = new OrganizerDao();
    private final AuditDao auditDao = new AuditDao();

    public Registration register(long userId, long categoryId, String jerseySize, String emergencyName,
                                 String emergencyPhone, String medicalNotes, boolean payNow,
                                 String method, String ip) throws Exception {
        return Db.transaction(c -> {
            EventCategory category = categoryDao.lockForUpdate(c, categoryId);
            if (category == null) {
                throw new AppException("The selected race category no longer exists.", 404);
            }
            Event event = eventDao.findById(c, category.getEventId());
            if (event == null || event.getStatus() != EventStatus.APPROVED) {
                throw new AppException("This event is not open for registration.");
            }
            LocalDate today = LocalDate.now();
            if (today.isBefore(event.getRegistrationOpen())) {
                throw new AppException("Registration for this event has not opened yet.");
            }
            if (today.isAfter(event.getRegistrationClose())) {
                throw new AppException("Registration for this event has closed.");
            }
            if (registrationDao.existsActiveInEvent(c, userId, event.getEventId())) {
                throw new AppException("You are already registered for this event. "
                        + "Each participant may only hold one entry per event.");
            }
            long active = categoryDao.activeCount(c, categoryId);
            if (active >= category.getQuota()) {
                throw new AppException("Sorry, the " + category.getName() + " category is full.");
            }

            Registration registration = new Registration();
            registration.setUserId(userId);
            registration.setCategoryId(categoryId);
            registration.setJerseySize(jerseySize);
            registration.setEmergencyName(emergencyName);
            registration.setEmergencyPhone(emergencyPhone);
            registration.setMedicalNotes(medicalNotes);
            registration.setStatus(RegistrationStatus.PENDING_PAYMENT);
            registration.setTotalAmount(category.getPrice());
            // Temporary unique placeholder; replaced with the formatted RE-YYYY-NNNN code below.
            registration.setRegistrationCode("TMP-" + Long.toString(System.nanoTime(), 36));
            long registrationId = registrationDao.insert(c, registration);

            int year = today.getYear();
            long sequence = registrationDao.nextSequence(c, year);
            String code = String.format("RE-%d-%04d", year, sequence);
            while (registrationDao.codeExists(c, code)) {
                sequence++;
                code = String.format("RE-%d-%04d", year, sequence);
            }
            registrationDao.updateCode(c, registrationId, code);

            Payment payment = new Payment();
            payment.setRegistrationId(registrationId);
            payment.setReferenceNo(String.format("PAY-%d-%05d", year, registrationId));
            payment.setMethod(method == null || method.isBlank() ? "FPX" : method);
            payment.setAmount(category.getPrice());
            payment.setStatus(PaymentStatus.PENDING);
            paymentDao.insert(c, payment);

            if (payNow) {
                paymentDao.markPaid(c, registrationId, payment.getMethod());
                registrationDao.confirm(c, registrationId, registrationDao.nextBib(c));
            }

            auditDao.log(c, userId, "CREATE", "REGISTRATION", String.valueOf(registrationId),
                    "Registered for " + event.getTitle() + " - " + category.getName()
                            + " (" + category.getPriceFormatted() + (payNow ? ", paid)" : ", pending payment)"),
                    ip);
            return registrationDao.findById(c, registrationId);
        });
    }

    public Registration pay(long registrationId, long userId, String method, String ip) throws Exception {
        return Db.transaction(c -> {
            Registration registration = registrationDao.findByIdForUser(c, registrationId, userId);
            if (registration == null) {
                throw new AppException("Registration not found.", 404);
            }
            if (registration.getStatus() != RegistrationStatus.PENDING_PAYMENT) {
                throw new AppException("This registration does not require payment.");
            }
            Payment payment = paymentDao.findByRegistration(c, registrationId);
            if (payment == null) {
                throw new AppException("Payment record not found.", 404);
            }
            String chosen = method == null || method.isBlank() ? payment.getMethod() : method;
            paymentDao.markPaid(c, registrationId, chosen);
            String bib = registrationDao.nextBib(c);
            if (!registrationDao.confirm(c, registrationId, bib)) {
                throw new AppException("Unable to confirm the registration. Please contact support.");
            }
            auditDao.log(c, userId, "PAYMENT", "REGISTRATION", String.valueOf(registrationId),
                    "Paid " + registration.getAmountFormatted() + " for " + registration.getEventTitle()
                            + " (bib " + bib + ")", ip);
            return registrationDao.findByIdForUser(c, registrationId, userId);
        });
    }

    public void cancel(long registrationId, long userId, String ip) throws Exception {
        Db.transaction(c -> {
            Registration registration = registrationDao.findByIdForUser(c, registrationId, userId);
            if (registration == null) {
                throw new AppException("Registration not found.", 404);
            }
            if (registration.getStatus() != RegistrationStatus.CONFIRMED
                    && registration.getStatus() != RegistrationStatus.PENDING_PAYMENT) {
                throw new AppException("Only confirmed or unpaid registrations can be cancelled.");
            }
            if (registration.getEventDate() != null && LocalDate.now().isAfter(registration.getEventDate())) {
                throw new AppException("The event has already taken place; this registration cannot be cancelled.");
            }
            boolean cancelled = registrationDao.cancel(c, registrationId, userId);
            if (!cancelled) {
                throw new AppException("The registration could not be cancelled.");
            }
            Payment payment = paymentDao.findByRegistration(c, registrationId);
            if (payment != null && payment.getStatus() == PaymentStatus.PAID) {
                paymentDao.refund(c, registrationId);
            } else if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                paymentDao.updateStatus(c, registrationId, PaymentStatus.FAILED);
            }
            auditDao.log(c, userId, "UPDATE", "REGISTRATION", String.valueOf(registrationId),
                    "Cancelled registration " + registration.getRegistrationCode()
                            + " for " + registration.getEventTitle(), ip);
            return null;
        });
    }

    public Registration checkIn(long registrationId, long organizerUserId, String ip) throws Exception {
        return Db.transaction(c -> {
            Registration registration = registrationDao.findById(c, registrationId);
            if (registration == null) {
                throw new AppException("Registration not found.", 404);
            }
            var organizer = organizerDao.findByUserId(c, organizerUserId);
            if (organizer == null || registration.getOrganizerId() != organizer.getOrganizerId()) {
                throw new AppException("You do not have access to this registration.", 403);
            }
            if (registration.getStatus() == RegistrationStatus.CHECKED_IN) {
                throw new AppException("This participant has already checked in.");
            }
            if (registration.getStatus() != RegistrationStatus.CONFIRMED) {
                throw new AppException("Only confirmed participants can check in.");
            }
            if (!registrationDao.checkIn(c, registrationId, organizer.getOrganizerId())) {
                throw new AppException("Check-in failed. Please refresh and try again.");
            }
            auditDao.log(c, organizerUserId, "CHECK_IN", "REGISTRATION", String.valueOf(registrationId),
                    "Checked in " + registration.getParticipantName() + " ("
                            + registration.getEventTitle() + ")", ip);
            return registrationDao.findById(c, registrationId);
        });
    }

    public void saveResult(long registrationId, String durationText, long organizerUserId, String ip)
            throws Exception {
        int seconds = parseDuration(durationText);
        Db.transaction(c -> {
            Registration registration = registrationDao.findById(c, registrationId);
            if (registration == null) {
                throw new AppException("Registration not found.", 404);
            }
            var organizer = organizerDao.findByUserId(c, organizerUserId);
            if (organizer == null || registration.getOrganizerId() != organizer.getOrganizerId()) {
                throw new AppException("You do not have access to this registration.", 403);
            }
            if (!registration.getStatus().isPaid()) {
                throw new AppException("Only confirmed or checked-in participants can receive a result.");
            }
            resultDao.save(c, registrationId, seconds, organizerUserId);
            auditDao.log(c, organizerUserId, "CREATE", "RESULT", String.valueOf(registrationId),
                    "Recorded finish time " + durationText + " for " + registration.getParticipantName(), ip);
            return null;
        });
    }

    public void deleteResult(long registrationId, long organizerUserId, String ip) throws Exception {
        Db.transaction(c -> {
            Registration registration = registrationDao.findById(c, registrationId);
            if (registration == null) {
                throw new AppException("Registration not found.", 404);
            }
            var organizer = organizerDao.findByUserId(c, organizerUserId);
            if (organizer == null || registration.getOrganizerId() != organizer.getOrganizerId()) {
                throw new AppException("You do not have access to this registration.", 403);
            }
            resultDao.delete(c, registrationId);
            auditDao.log(c, organizerUserId, "DELETE", "RESULT", String.valueOf(registrationId),
                    "Removed result for " + registration.getParticipantName(), ip);
            return null;
        });
    }

    public List<Registration> allRegistrations(String q, String status, String sort, String dir,
                                               int offset, int limit) throws Exception {
        return Db.read(c -> registrationDao.listAll(c, q, status, sort, dir, offset, limit));
    }

    public long countAllRegistrations(String q, String status) throws Exception {
        return Db.read(c -> registrationDao.countAllFiltered(c, q, status));
    }

    public List<Registration> myRegistrations(long userId, String status, String sort, String dir,
                                              int offset, int limit) throws Exception {        return Db.read(c -> registrationDao.listByUser(c, userId, status, sort, dir, offset, limit));
    }

    public long countMyRegistrations(long userId, String status) throws Exception {
        return Db.read(c -> registrationDao.countByUser(c, userId, status));
    }

    public CheckoutSelection checkout(long categoryId, long userId) throws Exception {
        return Db.read(c -> {
            EventCategory category = categoryDao.findById(c, categoryId);
            if (category == null) {
                throw new AppException("The selected race category was not found.", 404);
            }
            Event event = eventDao.findById(c, category.getEventId());
            if (event == null || !event.getStatus().isPubliclyVisible()) {
                throw new AppException("This event is not available.", 404);
            }
            boolean registered = registrationDao.existsActiveInEvent(c, userId, event.getEventId());
            return new CheckoutSelection(event, category, registered, category.isFull());
        });
    }

    public boolean isRegisteredInEvent(long userId, long eventId) throws Exception {
        return Db.read(c -> registrationDao.existsActiveInEvent(c, userId, eventId));
    }

    public boolean hasResult(long registrationId) throws Exception {
        return Db.read(c -> resultDao.findByRegistration(c, registrationId) != null);
    }

    public Registration myRegistration(long userId, long registrationId) throws Exception {
        return Db.read(c -> {
            Registration registration = registrationDao.findByIdForUser(c, registrationId, userId);
            if (registration == null) {
                throw new AppException("Registration not found.", 404);
            }
            return registration;
        });
    }

    public List<Registration> organizerRegistrations(long userId, Long eventId, String q, String status,
                                                     String sort, String dir, int offset, int limit)
            throws Exception {
        return Db.read(c -> {
            var organizer = organizerDao.findByUserId(c, userId);
            if (organizer == null) {
                throw new AppException("Organizer profile not found.", 403);
            }
            return registrationDao.listForOrganizer(c, organizer.getOrganizerId(), eventId, q, status,
                    sort, dir, offset, limit);
        });
    }

    public long countOrganizerRegistrations(long userId, Long eventId, String q, String status) throws Exception {
        return Db.read(c -> {
            var organizer = organizerDao.findByUserId(c, userId);
            if (organizer == null) {
                throw new AppException("Organizer profile not found.", 403);
            }
            return registrationDao.countForOrganizer(c, organizer.getOrganizerId(), eventId, q, status);
        });
    }

    public List<Registration> eventRegistrations(long userId, long eventId) throws Exception {
        return Db.read(c -> {
            var organizer = organizerDao.findByUserId(c, userId);
            if (organizer == null) {
                throw new AppException("Organizer profile not found.", 403);
            }
            Event event = eventDao.findByIdForOrganizer(c, eventId, organizer.getOrganizerId());
            if (event == null) {
                throw new AppException("Event not found.", 404);
            }
            return registrationDao.listByEvent(c, eventId);
        });
    }

    private int parseDuration(String value) throws AppException {
        if (value == null || value.isBlank()) {
            throw new AppException("Finish time is required (hh:mm:ss or mm:ss).");
        }
        String[] parts = value.trim().split(":");
        try {
            int hours = 0;
            int minutes;
            int seconds;
            if (parts.length == 3) {
                hours = Integer.parseInt(parts[0]);
                minutes = Integer.parseInt(parts[1]);
                seconds = Integer.parseInt(parts[2]);
            } else if (parts.length == 2) {
                minutes = Integer.parseInt(parts[0]);
                seconds = Integer.parseInt(parts[1]);
            } else {
                throw new NumberFormatException();
            }
            if (minutes > 59 || seconds > 59 || hours > 47) {
                throw new NumberFormatException();
            }
            int total = hours * 3600 + minutes * 60 + seconds;
            if (total < 300) {
                throw new AppException("Finish time looks too fast. Enter a time of at least 5 minutes.");
            }
            return total;
        } catch (NumberFormatException e) {
            throw new AppException("Enter a valid finish time, e.g. 1:45:30 or 45:30.");
        }
    }
}
