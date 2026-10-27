# syntax=docker/dockerfile:1

# ---------- Stage 1 : build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Dependencies first: this layer stays cached as long as pom.xml does not change
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests

# ---------- Stage 2 : development (bind mount, Maven + JDK) ----------
FROM maven:3.9-eclipse-temurin-17 AS development
WORKDIR /app
CMD ["mvn", "spring-boot:run"]

# ---------- Stage 3 : production ----------
FROM eclipse-temurin:17-jre AS production
WORKDIR /app

# curl is needed by the Compose healthcheck (installed as root, before USER)
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

# Non-root user and group with a fixed UID/GID
RUN groupadd --system --gid 1001 spring \
 && useradd  --system --uid 1001 --gid spring --no-create-home --shell /usr/sbin/nologin spring

# The jar belongs to the non-root user and is read-only for everyone else
COPY --from=build --chown=spring:spring /app/target/secureship.jar app.jar

USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]