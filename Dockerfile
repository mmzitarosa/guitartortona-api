# ============================================================================
# Dockerfile for Spring Boot Application
# Supports: linux/amd64
# ============================================================================

# NOTE: il JAR NON viene ricompilato qui. Il Dockerfile impacchetta il JAR gia'
# costruito e testato dalla pipeline (stesso binario della GitHub Release).
# Build locale: ./mvnw clean package -DskipTests && docker build -t guitartortona-api .

# ============================================================================
# Runtime image
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

# Copy the pre-built JAR (target/ contains a single executable JAR)
COPY --chown=appuser:appuser target/*.jar app.jar

# Expose ports
EXPOSE ${SERVER_PORT} ${MANAGEMENT_PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${MANAGEMENT_PORT}/actuator/health/liveness || exit 1

# Volume for temporary files
VOLUME ["/tmp/app"]

# Start application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Djava.io.tmpdir=/tmp/app -jar app.jar"]