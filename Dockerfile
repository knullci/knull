# ==============================================================================
# Knull CI/CD Dockerfile (Debian-based for better compatibility)
# Multi-stage build with separate test and production stages
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Dependencies (cached layer)
# Using Debian-based image for better glibc compatibility with protoc plugins
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS dependencies

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (this layer is cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# ------------------------------------------------------------------------------
# Stage 2: Compile
# ------------------------------------------------------------------------------
FROM dependencies AS compile

WORKDIR /app

# Copy source code
COPY src ./src

# Compile the application
RUN mvn compile -B -DskipTests

# ------------------------------------------------------------------------------
# Stage 3: Test
# ------------------------------------------------------------------------------
FROM compile AS test

WORKDIR /app

# Run tests
RUN mvn test -B

# ------------------------------------------------------------------------------
# Stage 4: Package
# ------------------------------------------------------------------------------
FROM compile AS package

WORKDIR /app

# Skip tests since they ran in the test stage
RUN mvn package -B -DskipTests

# Extract layers for better caching (Spring Boot layered JAR)
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ------------------------------------------------------------------------------
# Stage 5: Production (using slim Alpine for smaller image)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS production

# Add labels for container metadata
LABEL maintainer="Knull CI/CD Team"
LABEL version="0.0.1-SNAPSHOT"
LABEL description="Knull CI/CD - Lightweight CI/CD Server"

# Create non-root user for security
RUN addgroup -g 1001 -S knull && \
    adduser -u 1001 -S knull -G knull

WORKDIR /app

# Copy layered application from package stage
COPY --from=package /app/target/extracted/dependencies/ ./
COPY --from=package /app/target/extracted/spring-boot-loader/ ./
COPY --from=package /app/target/extracted/snapshot-dependencies/ ./
COPY --from=package /app/target/extracted/application/ ./

# Create directories for storage and workspace
RUN mkdir -p /app/storage /app/workspace && \
    chown -R knull:knull /app

# Switch to non-root user
USER knull

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
