# Project Chimera — production-style runtime image (Spring Boot / Java 21)
# Build: docker build -t chimera:latest .
# Run:   docker run --rm -p 8080:8080 chimera:latest
#
# Packaging uses -DskipTests so the image builds even while contract tests are red (TDD).
# Run the full suite locally or in CI: make test

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY mvnw pom.xml checkstyle.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src ./src
COPY tests ./tests

RUN ./mvnw -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime

RUN groupadd --system chimera && useradd --system --gid chimera --home /app chimera
WORKDIR /app

COPY --from=build /app/target/project-chimera-*.jar /app/app.jar

USER chimera
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
