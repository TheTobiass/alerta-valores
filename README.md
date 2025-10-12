## Running the Project with Docker

This project provides a Docker setup for building and running the Java Spring Boot application using Eclipse Temurin JDK 21. The Docker configuration uses a multi-stage build for efficient image creation and runs the app as a non-root user for security.

### Requirements
- **Java Version:** Eclipse Temurin JDK 21 (as specified in the Dockerfile)
- **Build Tool:** Maven Wrapper (included in the project)

### Build and Run Instructions
1. **Ensure Docker and Docker Compose are installed on your system.**
2. **Build and start the application:**
   ```sh
   docker compose up --build
   ```
   This will build the image using the provided `Dockerfile` and start the service defined in `docker-compose.yml`.

### Service Details
- **Service Name:** `java-app`
- **Exposed Port:** `8080` (mapped to host port 8080)
- **Network:** `app-network` (custom bridge network for the service)

### Environment Variables
- No required environment variables are specified in the Dockerfile or Compose file by default.
- If you need to provide environment variables, you can create a `.env` file and uncomment the `env_file` line in `docker-compose.yml`.

### Special Configuration
- The application is built and run as a non-root user (`appuser`) for improved security.
- JVM is configured with `-XX:MaxRAMPercentage=80.0` for container-aware memory management.
- The build skips tests for faster image creation (`-DskipTests`).

### Additional Notes
- The `.dockerignore` file should be added to the project root to exclude unnecessary files and directories from the build context (see Dockerfile comments for recommended entries).
- No additional configuration is required unless your application needs custom environment variables or external dependencies.

---
*Update: Docker setup instructions have been added/updated to reflect the current project configuration.*