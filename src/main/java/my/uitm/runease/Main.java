package my.uitm.runease;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;

import my.uitm.runease.config.Db;

/**
 * Local development runner: starts the application on an embedded Tomcat 10.1
 * (Jakarta EE 10) instance. Run with: mvn compile exec:java
 *
 * For server deployment, build a WAR with "mvn package" and drop
 * target/runease.war into Tomcat's webapps folder.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Path webapp = resolveWebapp();
        Path baseDir = Files.createTempDirectory("runease-tomcat-");

        Db.initialize();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector().setURIEncoding("UTF-8");

        StandardContext context = (StandardContext) tomcat.addWebapp("", webapp.toAbsolutePath().toString());
        context.setParentClassLoader(Main.class.getClassLoader());
        context.setSessionTimeout(30);
        context.setUseHttpOnly(true);

        Rfc6265CookieProcessor cookies = new Rfc6265CookieProcessor();
        cookies.setSameSiteCookies("lax");
        context.setCookieProcessor(cookies);

        // In dev mode, servlet classes live in target/classes, not WEB-INF/classes.
        Path classes = Paths.get("target/classes");
        if (Files.isDirectory(classes)) {
            WebResourceRoot resources = new StandardRoot(context);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    classes.toAbsolutePath().toString(), "/"));
            context.setResources(resources);
        }

        tomcat.start();

        String line = "=".repeat(66);
        System.out.println();
        System.out.println(line);
        System.out.println("  RunEase - Running Event Management System");
        System.out.println("  Local URL : http://localhost:" + port + "/");
        System.out.println("  Admin     : admin@runease.my / Password123!");
        System.out.println("  Organizer : ahmad@paceevents.my / Password123!");
        System.out.println("  Runner    : alia@student.uitm.edu.my / Password123!");
        System.out.println(line);
        System.out.println();

        tomcat.getServer().await();
    }

    private static Path resolveWebapp() {
        String override = System.getenv("RUNEASE_WEBAPP");
        if (override != null && !override.isBlank()) {
            return Paths.get(override);
        }
        Path source = Paths.get("src/main/webapp");
        if (Files.isDirectory(source)) {
            return source;
        }
        throw new IllegalStateException("Cannot locate the web application folder. "
                + "Run the command from the project root or set RUNEASE_WEBAPP.");
    }
}
