package my.uitm.runease.service;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import my.uitm.runease.config.Db;
import my.uitm.runease.dao.AuditDao;
import my.uitm.runease.dao.OrganizerDao;
import my.uitm.runease.dao.UserDao;
import my.uitm.runease.model.AccountStatus;
import my.uitm.runease.model.Organizer;
import my.uitm.runease.model.Role;
import my.uitm.runease.model.User;
import my.uitm.runease.model.VerificationStatus;

import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserDao userDao = new UserDao();
    private final OrganizerDao organizerDao = new OrganizerDao();
    private final AuditDao auditDao = new AuditDao();

    public User login(String email, String password, String ip) throws Exception {
        return Db.transaction(c -> {
            User user = userDao.findByEmail(c, email);
            if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
                throw new AppException("Incorrect email or password. Please try again.", 401);
            }
            if (user.getStatus() == AccountStatus.SUSPENDED) {
                throw new AppException("This account has been suspended. Please contact the RunEase administrator.", 403);
            }
            if (user.getStatus() == AccountStatus.INACTIVE) {
                throw new AppException("This account is inactive. Please contact the RunEase administrator.", 403);
            }
            if (user.getRole() == Role.ORGANIZER) {
                Organizer organizer = organizerDao.findByUserId(c, user.getUserId());
                if (organizer == null || organizer.getVerificationStatus() != VerificationStatus.VERIFIED) {
                    throw new AppException("Your organizer account is still awaiting administrator verification.", 403);
                }
            }
            userDao.touchLogin(c, user.getUserId());
            auditDao.log(c, user.getUserId(), "LOGIN", "USER", String.valueOf(user.getUserId()),
                    user.getFullName() + " signed in", ip);
            return user;
        });
    }

    public User registerParticipant(String fullName, String email, String password, String phone,
                                    String city, String ip) throws Exception {
        return Db.transaction(c -> {
            if (userDao.emailExists(c, email, null)) {
                throw new AppException("An account with this email address already exists.");
            }
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
            user.setPhone(phone);
            user.setCity(city);
            user.setRole(Role.PARTICIPANT);
            user.setStatus(AccountStatus.ACTIVE);
            long id = userDao.insert(c, user);
            auditDao.log(c, id, "CREATE", "USER", String.valueOf(id),
                    "New participant account registered: " + email, ip);
            user.setUserId(id);
            return user;
        });
    }

    public void changePassword(long userId, String currentPassword, String newPassword, String ip) throws Exception {
        Db.transaction(c -> {
            User user = userDao.findById(c, userId);
            if (user == null || !BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
                throw new AppException("Your current password is incorrect.");
            }
            userDao.updatePassword(c, userId, BCrypt.hashpw(newPassword, BCrypt.gensalt(10)));
            auditDao.log(c, userId, "UPDATE", "USER", String.valueOf(userId),
                    "Password changed", ip);
            return null;
        });
    }

    public User updateProfile(long userId, String fullName, String phone, String gender,
                              LocalDate dateOfBirth, String city, String ip) throws Exception {
        return Db.transaction(c -> {
            User user = userDao.findById(c, userId);
            if (user == null) {
                throw new AppException("Account not found.", 404);
            }
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setGender(gender);
            user.setDateOfBirth(dateOfBirth);
            user.setCity(city);
            userDao.updateProfile(c, user);
            auditDao.log(c, userId, "UPDATE", "USER", String.valueOf(userId),
                    "Profile details updated", ip);
            return user;
        });
    }

    public void registerOrganizer(String fullName, String email, String password, String phone,
                                  String organizationName, String registrationNo, String website,
                                  String ip) throws Exception {
        Db.transaction(c -> {
            if (userDao.emailExists(c, email, null)) {
                throw new AppException("An account with this email address already exists.");
            }
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
            user.setPhone(phone);
            user.setRole(Role.ORGANIZER);
            user.setStatus(AccountStatus.ACTIVE);
            long userId = userDao.insert(c, user);

            Organizer organizer = new Organizer();
            organizer.setUserId(userId);
            organizer.setOrganizationName(organizationName);
            organizer.setRegistrationNo(registrationNo);
            organizer.setContactPerson(fullName);
            organizer.setWebsite(website);
            organizer.setVerificationStatus(VerificationStatus.PENDING);
            organizerDao.insert(c, organizer);

            auditDao.log(c, userId, "CREATE", "ORGANIZER", String.valueOf(userId),
                    "Organizer registration submitted: " + organizationName, ip);
            return null;
        });
    }

    public List<User> listUsers(String q, String role, String status, String sort, String dir,
                                int offset, int limit) throws Exception {
        return Db.read(c -> userDao.list(c, q, role, status, sort, dir, offset, limit));
    }

    public long countUsers(String q, String role, String status) throws Exception {
        return Db.read(c -> userDao.count(c, q, role, status));
    }

    public User findById(long id) throws Exception {
        return Db.read(c -> userDao.findById(c, id));
    }

    public void createUser(User user, String rawPassword, long adminId, String ip) throws Exception {
        createUser(user, rawPassword, null, adminId, ip);
    }

    public void createUser(User user, String rawPassword, String organizationName, long adminId, String ip)
            throws Exception {
        Db.transaction(c -> {
            if (userDao.emailExists(c, user.getEmail(), null)) {
                throw new AppException("An account with this email address already exists.");
            }
            user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt(10)));
            long id = userDao.insert(c, user);
            if (user.getRole() == Role.ORGANIZER) {
                Organizer organizer = new Organizer();
                organizer.setUserId(id);
                String orgName = organizationName == null || organizationName.isBlank()
                        ? user.getFullName() + " Events" : organizationName.trim();
                organizer.setOrganizationName(orgName);
                organizer.setContactPerson(user.getFullName());
                organizer.setVerificationStatus(VerificationStatus.VERIFIED);
                long organizerId = organizerDao.insert(c, organizer);
                organizerDao.updateVerification(c, organizerId, VerificationStatus.VERIFIED, adminId);
            }
            auditDao.log(c, adminId, "CREATE", "USER", String.valueOf(id),
                    "Created " + user.getRole().name().toLowerCase() + " account for " + user.getEmail(), ip);
            return null;
        });
    }

    public void updateUser(User user, String rawPassword, long adminId, String ip) throws Exception {
        Db.transaction(c -> {
            if (userDao.emailExists(c, user.getEmail(), user.getUserId())) {
                throw new AppException("Another account already uses this email address.");
            }
            if (rawPassword != null && !rawPassword.isBlank()) {
                userDao.updatePassword(c, user.getUserId(), BCrypt.hashpw(rawPassword, BCrypt.gensalt(10)));
            }
            userDao.update(c, user);
            if (user.getRole() == Role.ORGANIZER && organizerDao.findByUserId(c, user.getUserId()) == null) {
                Organizer organizer = new Organizer();
                organizer.setUserId(user.getUserId());
                organizer.setOrganizationName(user.getFullName() + " Events");
                organizer.setContactPerson(user.getFullName());
                organizer.setVerificationStatus(VerificationStatus.VERIFIED);
                long organizerId = organizerDao.insert(c, organizer);
                organizerDao.updateVerification(c, organizerId, VerificationStatus.VERIFIED, adminId);
            }
            auditDao.log(c, adminId, "UPDATE", "USER", String.valueOf(user.getUserId()),
                    "Updated account " + user.getEmail(), ip);
            return null;
        });
    }

    public void setUserStatus(long userId, AccountStatus status, long adminId, String ip) throws Exception {
        Db.transaction(c -> {
            if (userId == adminId && status != AccountStatus.ACTIVE) {
                throw new AppException("You cannot deactivate your own administrator account.");
            }
            userDao.updateStatus(c, userId, status);
            auditDao.log(c, adminId, "UPDATE", "USER", String.valueOf(userId),
                    "Changed account status to " + status.name(), ip);
            return null;
        });
    }

    public void deleteUser(long userId, long adminId, String ip) throws Exception {
        Db.transaction(c -> {
            if (userId == adminId) {
                throw new AppException("You cannot delete your own administrator account.");
            }
            User user = userDao.findById(c, userId);
            if (user == null) {
                throw new AppException("Account not found.", 404);
            }
            userDao.softDelete(c, userId);
            auditDao.log(c, adminId, "DELETE", "USER", String.valueOf(userId),
                    "Soft-deleted account " + user.getEmail(), ip);
            return null;
        });
    }

    public Organizer organizerProfile(long userId) throws Exception {
        return Db.read(c -> organizerDao.findByUserId(c, userId));
    }

    public List<Organizer> listOrganizers(String status) throws Exception {
        return Db.read(c -> organizerDao.list(c, status));
    }

    public void verifyOrganizer(long organizerId, VerificationStatus status, long adminId, String ip)
            throws Exception {
        Db.transaction(c -> {
            Organizer organizer = organizerDao.findById(c, organizerId);
            if (organizer == null) {
                throw new AppException("Organizer not found.", 404);
            }
            organizerDao.updateVerification(c, organizerId, status, adminId);
            auditDao.log(c, adminId, status == VerificationStatus.VERIFIED ? "APPROVE" : "REJECT",
                    "ORGANIZER", String.valueOf(organizerId),
                    status.getLabel() + " organizer " + organizer.getOrganizationName(), ip);
            return null;
        });
    }

    public void log(Long userId, String action, String entityType, String entityId,
                    String description, String ip) {
        try (Connection c = Db.getConnection()) {
            auditDao.log(c, userId, action, entityType, entityId, description, ip);
        } catch (Exception e) {
            System.err.println("[RunEase] Audit log failed: " + e.getMessage());
        }
    }
}
