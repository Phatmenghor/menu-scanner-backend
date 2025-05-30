# -------- Stage 1: Build --------
FROM eclipse-temurin:17-jdk AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml to download dependencies first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Ensure the mvnw script has execute permissions
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Package the application
RUN ./mvnw clean package -DskipTests

# -------- Stage 2: Production --------
FROM eclipse-temurin:17-jdk AS production

# Set working directory
WORKDIR /app

# Create logs directory with proper permissions
RUN mkdir -p /app/logs && chmod 755 /app/logs

# Copy the jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (adjust according to your application settings)
EXPOSE 8080

# Set environment variable for log path
ENV LOG_PATH=/app/logs

# Default command to run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]