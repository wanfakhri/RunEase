package my.uitm.runease.web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import my.uitm.runease.config.Db;

/**
 * Initialises the database when the application starts inside a servlet
 * container. The embedded development runner calls {@link Db#initialize()}
 * itself; this listener makes a plain WAR deployment behave the same way, so
 * the schema and demonstration data are created on the first request.
 */
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Db.initialize();
    }
}
