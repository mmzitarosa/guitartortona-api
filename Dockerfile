# ============================================================================
# Multi-Stage Dockerfile for Spring Boot Application
# Supports: linux/amd64, linux/arm64
# ============================================================================

# ============================================================================
# Stage 1: Build Stage
# ============================================================================
FROM maven:3.9-amazoncorretto-21 AS builder

WORKDIR /build

# Copy Maven configuration first (better layer caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (cached layer if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests --batch-mode --no-transfer-progress && \
    # Find the built JAR (excluding original)
    mv target/*.jar target/app.jar 2>/dev/null || \
    find target -name "*.jar" ! -name "*-original.jar" -exec mv {} target/app.jar \;

# ============================================================================
# Stage 2: Runtime Stage
# ============================================================================
FROM amazoncorretto:21-alpine

# Metadata
LABEL maintainer="mmzitarosa"
LABEL org.opencontainers.image.source="https://github.com/mmzitarosa/guitartortona-api"
LABEL org.opencontainers.image.description="GuitarTortona API - Spring Boot Application"

# Build arguments
ARG VERSION=dev
ARG BUILD_DATE
ARG VCS_REF

# Environment variables
ENV VERSION=${VERSION} \
    BUILD_DATE=${BUILD_DATE} \
    VCS_REF=${VCS_REF} \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \
    SERVER_PORT=8080 \
    MANAGEMENT_PORT=8081

# Install required packages
RUN apk add --no-cache \
    curl \
    wget \
    ca-certificates \
    tzdata && \
    cp /usr/share/zoneinfo/Europe/Rome /etc/localtime && \
    echo "Europe/Rome" > /etc/timezone

# Create non-root user
RUN addgroup -g 1000 appuser && \
    adduser -u 1000 -G appuser -s /bin/sh -D appuser && \
    mkdir -p /app /tmp/app && \
    chown -R appuser:appuser /app /tmp/app

# Switch to non-root user
USER appuser

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder --chown=appuser:appuser /build/target/app.jar app.jar

# Expose ports
EXPOSE ${SERVER_PORT} ${MANAGEMENT_PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${MANAGEMENT_PORT}/actuator/health/liveness || exit 1

# Volume for temporary files
VOLUME ["/tmp/app"]

# Start application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Djava.io.tmpdir=/tmp/app -jar app.jar"]