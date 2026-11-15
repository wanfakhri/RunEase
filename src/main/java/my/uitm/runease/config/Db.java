package my.uitm.runease.config;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.RunScript;

/**
 * Central JDBC configuration. By default RunEase uses an embedded H2 database
 * (MySQL compatibility mode) so the application runs with zero setup.
 * Set RUNEASE_DB_URL / RUNEASE_DB_USER / RUNEASE_DB_PASSWORD to point at MySQL.
 */
public final class Db {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private static final boolean EMBEDDED;

    static {
        String url = env("RUNEASE_DB_URL",
                "jdbc:h2:file:./data/runease;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE");
        URL = url;
        USER = env("RUNEASE_DB_USER", "sa");
        PASSWORD = env("RUNEASE_DB_PASSWORD", "");
        EMBEDDED = url.startsWith("jdbc:h2:");
        try {
            Class.forName(EMBEDDED ? "org.h2.Driver" : "com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBC driver not found for " + url, e);
        }
    }

    private Db() {
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @FunctionalInterface
    public interface TxFunction<T> {
        T apply(Connection connection) throws Exception;
    }

    /** Runs work in a committed transaction and rolls back on any failure. */
    public static <T> T transaction(TxFunction<T> work) throws Exception {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = work.apply(connection);
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    /** Runs read-only work on a dedicated connection. */
    public static <T> T read(TxFunction<T> work) throws Exception {
        try (Connection connection = getConnection()) {
            return work.apply(connection);
        }
    }

    /**
     * Creates the schema and demo data on first run (embedded database only).
     */
    public static void initialize() {
        if (!EMBEDDED) {
            return;
        }
        try (Connection connection = getConnection()) {
            if (tableExists(connection, "USERS")) {
                return;
            }
            System.out.println("[RunEase] First run detected. Creating schema ...");
            runScript(connection, "/db/schema.sql");
            System.out.println("[RunEase] Loading demonstration data ...");
            runScript(connection, "/db/seed.sql");
            System.out.println("[RunEase] Database ready.");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialise the database", e);
        }
    }

    private static boolean tableExists(Connection connection, String table) {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, table, new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException ignored) {
            // fall through to the query check
        }
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private static void runScript(Connection connection, String resource) throws Exception {
        try (Reader reader = new InputStreamReader(
                Db.class.getResourceAsStream(resource), StandardCharsets.UTF_8)) {
            if (reader == null) {
                throw new IllegalStateException("Missing SQL resource " + resource);
            }
            RunScript.execute(connection, reader);
        }
    }
}
