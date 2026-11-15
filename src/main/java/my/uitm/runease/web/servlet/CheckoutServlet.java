package my.uitm.runease.web.servlet;

import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.uitm.runease.model.Registration;
import my.uitm.runease.model.User;
import my.uitm.runease.service.AppException;
import my.uitm.runease.service.CheckoutSelection;
import my.uitm.runease.service.RegistrationService;
import my.uitm.runease.util.Crumb;
import my.uitm.runease.util.Validation;
import my.uitm.runease.web.BaseServlet;
import my.uitm.runease.web.Web;

@WebServlet("/checkout")
public class CheckoutServlet extends BaseServlet {

    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        Long categoryId = Web.longParamOrNull(req, "category");
        if (categoryId == null) {
            Web.flash(req, "warning", "Choose a race category before continuing to checkout.");
            redirect(req, res, "/events");
            return;
        }
        try {
            User user = currentUser(req);
            CheckoutSelection selection = registrationService.checkout(categoryId, user.getUserId());
            if (selection.isAlreadyRegistered()) {
                Web.flash(req, "info", "You already hold an entry for this event.");
                redirect(req, res, "/events/" + selection.getEvent().getEventId());
                return;
            }
            if (selection.isFull()) {
                Web.flash(req, "warning", "The " + selection.getCategory().getName() + " category is full.");
                redirect(req, res, "/events/" + selection.getEvent().getEventId());
                return;
            }
            req.setAttribute("selection", selection);
            req.setAttribute("jerseySizes", new String[]{"XS", "S", "M", "L", "XL", "XXL"});
            req.setAttribute("form", Map.of("jerseySize", "M", "method", "FPX"));
            page(req, "Checkout", Crumb.link("Events", "/events"),
                    Crumb.link(selection.getEvent().getTitle(), "/events/" + selection.getEvent().getEventId()),
                    Crumb.of("Checkout"));
            render(req, res, "participant/checkout");
        } catch (AppException e) {
            Web.flash(req, "danger", e.getMessage());
            redirect(req, res, "/events");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {
        Long categoryId = Web.longParamOrNull(req, "categoryId");
        if (categoryId == null) {
            Web.flash(req, "danger", "Your checkout session expired. Please choose a category again.");
            redirect(req, res, "/events");
            return;
        }
        User user = currentUser(req);
        String jerseySize = Web.param(req, "jerseySize", "M");
        String emergencyName = Web.param(req, "emergencyName", "");
        String emergencyPhone = Web.param(req, "emergencyPhone", "");
        String medicalNotes = Web.param(req, "medicalNotes", "");
        String method = Web.param(req, "method", "FPX");
        boolean payNow = "1".equals(Web.param(req, "payNow")) || req.getParameter("payNow") != null;

        Validation validation = new Validation();
        validation.oneOf("jerseySize", "Jersey size", jerseySize, "XS", "S", "M", "L", "XL", "XXL");
        validation.name("emergencyName", "Emergency contact name", emergencyName);
        validation.phone("emergencyPhone", "Emergency contact number", emergencyPhone, true);
        validation.length("medicalNotes", "Medical notes", medicalNotes, 255);
        validation.oneOf("method", "Payment method", method, "FPX", "CARD", "EWALLET");

        if (validation.hasErrors()) {
            showForm(req, res, categoryId, validation);
            return;
        }

        try {
            Registration registration = registrationService.register(user.getUserId(), categoryId, jerseySize,
                    emergencyName, emergencyPhone, medicalNotes, payNow, method, Web.clientIp(req));
            if (payNow) {
                Web.flash(req, "success", "You are in! Registration " + registration.getRegistrationCode()
                        + " is confirmed. Your bib number is " + registration.getBibDisplay() + ".");
            } else {
                Web.flash(req, "info", "Registration " + registration.getRegistrationCode()
                        + " has been reserved. Complete payment to secure your slot.");
            }
            redirect(req, res, "/registrations/" + registration.getRegistrationId());
        } catch (AppException e) {
            Validation server = new Validation();
            server.error("form", e.getMessage());
            showForm(req, res, categoryId, server);
        } catch (Exception e) {
            log("Registration checkout failed", e);
            Validation server = new Validation();
            server.error("form", "We could not complete your registration right now. Please try again.");
            showForm(req, res, categoryId, server);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse res, long categoryId, Validation validation)
            throws ServletException, java.io.IOException {
        try {
            User user = currentUser(req);
            CheckoutSelection selection = registrationService.checkout(categoryId, user.getUserId());
            req.setAttribute("selection", selection);
            req.setAttribute("jerseySizes", new String[]{"XS", "S", "M", "L", "XL", "XXL"});
            req.setAttribute("errors", validation.getErrors());
            req.setAttribute("form", Web.values(req,
                    "jerseySize", "emergencyName", "emergencyPhone", "medicalNotes", "method"));
            page(req, "Checkout", Crumb.link("Events", "/events"),
                    Crumb.link(selection.getEvent().getTitle(), "/events/" + selection.getEvent().getEventId()),
                    Crumb.of("Checkout"));
            render(req, res, "participant/checkout");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
