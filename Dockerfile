# syntax=docker/dockerfile:1

# --- Build Stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Maven wrapper and config first for dependency caching
COPY --link pom.xml mvnw ./
COPY --link .mvn .mvn/
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy source code
COPY --link src ./src/

# Build the application (skip tests for faster build)
RUN ./mvnw package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user and group
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Copy built jar from build stage
COPY --link --from=build /app/target/*.jar /app/app.jar

# Set permissions and switch to non-root user
RUN chown appuser:appgroup /app/app.jar
USER appuser

# JVM container-aware flags for memory/resource management
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0"

# Expose default Spring Boot port
EXPOSE 8080

# Use exec form for proper signal handling
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=80.0", "-jar", "/app/app.jar"]

# --- .dockerignore (add this file in your project root) ---
# .git
# .gitignore
# .gitattributes
# .mvn/wrapper/
# .vscode
# target/
# *.env
# *.lock
# *.iml
# *.idea
