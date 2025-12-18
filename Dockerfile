# ----------------------------
# 1️⃣ BUILD STAGE (Maven + JDK)
# ----------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom first (better cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source
COPY src src

# Build fat jar
RUN mvn clean package -DskipTests


# ----------------------------
# 2️⃣ RUNTIME STAGE (JRE only)
# ----------------------------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create uploads directory with 777 permissions
RUN mkdir -p /app/uploads && chmod 777 /app/uploads

# Install fonts including DejaVu and Liberation
RUN apt-get update && \
    apt-get install -y fonts-liberation fonts-dejavu fonts-liberation2 && \
    rm -rf /var/lib/apt/lists/* && \
    fc-cache -f -v

# Copy custom fonts (FMAbhaya and Iskoola Pota)
COPY fonts/calibri.ttf /usr/share/fonts/truetype/
COPY fonts/fm_abhay.TTF /usr/share/fonts/truetype/
COPY fonts/iskoola-pota.ttf /usr/share/fonts/truetype/

# Rebuild font cache to recognize all fonts
RUN fc-cache -f -v

# Copy JAR from builder
COPY --from=builder /build/target/*.jar app.jar

# Copy application.yml
COPY src/main/resources/application.yml /app/

# Expose port
EXPOSE 8080

# Run as root user to avoid permission issues
USER root

# Start application with JasperReports font configuration
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-Dnet.sf.jasperreports.awt.ignore.missing.font=true", "-Dnet.sf.jasperreports.default.font.name=DejaVuSans", "-jar", "/app/app.jar"]
