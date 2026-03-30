FROM ubuntu:latest
LABEL authors="User"

ENTRYPOINT ["top", "-b"]


# 1. Use lightweight Java image
FROM eclipse-temurin:17-jdk-alpine

# 2. Set working directory
WORKDIR /app

# 3. Copy jar file
COPY target/NUM_Payroll-0.0.1-SNAPSHOT.jar NUM_Payroll-0.0.1-SNAPSHOT.jar

# 4. Expose port (same as server.port)
EXPOSE 8080

# 5. Run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
