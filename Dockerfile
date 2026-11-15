# ---------------------------------------------------------------------------
# RunEase - production-style container image
#
# Build:  docker build -t runease .
# Run:    docker run -p 8080:8080 runease
#
# The application is packaged as a WAR and deployed to Tomcat 10.1 as the root
# context. The PORT environment variable (set automatically by Render, Railway,
# Koyeb, Fly.io, etc.) is mapped onto Tomcat's HTTP connector.
# ---------------------------------------------------------------------------

# ---------- stage 1: build the WAR -----------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- stage 2: run on Tomcat ------------------------------------------
FROM tomcat:10.1-jdk21-temurin

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

# Tomcat reads the connector port from the http.port system property, which
# setenv.sh fills from the platform's PORT variable (default 8080).
RUN sed -i 's/port="8080"/port="${http.port}"/' /usr/local/tomcat/conf/server.xml \
 && printf '#!/bin/sh\nexport CATALINA_OPTS="$CATALINA_OPTS -Dhttp.port=${PORT:-8080} -XX:MaxRAMPercentage=75"\n' \
        > /usr/local/tomcat/bin/setenv.sh \
 && chmod +x /usr/local/tomcat/bin/setenv.sh

# Serve RunEase at "/" and drop Tomcat's welcome application.
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/target/runease.war /usr/local/tomcat/webapps/ROOT.war

# The embedded H2 database is created in /usr/local/tomcat/data on first start.
# Attach a persistent volume here if you need data to survive redeployments.
VOLUME ["/usr/local/tomcat/data"]

EXPOSE 8080
CMD ["catalina.sh", "run"]
