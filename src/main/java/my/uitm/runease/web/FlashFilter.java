package my.uitm.runease.web;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Moves one-shot flash messages from the session into the request so the layout
 * can render them exactly once.
 */
public class FlashFilter implements Filter {

    @Override
    @SuppressWarnings("unchecked")
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);
        if (session != null) {
            List<FlashMessage> messages = (List<FlashMessage>) session.getAttribute(Web.FLASH);
            if (messages != null) {
                req.setAttribute(Web.FLASH, messages);
                session.removeAttribute(Web.FLASH);
            }
        }
        chain.doFilter(request, response);
    }
}
