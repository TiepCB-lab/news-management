# ==========================================
# STAGE 1: Build source code bằng Maven
# ==========================================
FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /build

COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp package -Dmaven.test.skip=true

# ==========================================
# STAGE 2: Chạy ứng dụng với JRE 25
# ==========================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Tạo thư mục uploads cho ứng dụng
RUN mkdir -p /app/uploads

# Copy file jar đã build từ Stage 1
COPY --from=builder /build/target/news-management-*.jar app.jar

# Cổng ứng dụng
EXPOSE 8080

# Chạy ứng dụng Spring Boot với cấu hình JVM tối ưu
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
