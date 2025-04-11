FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /build

# Sao chép các file Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Cài đặt Maven và build ứng dụng
RUN ./mvnw package -DskipTests

# Giai đoạn thứ 2 - Runtime
FROM eclipse-temurin:21-jre-alpine

# Tạo user không phải root để chạy ứng dụng
RUN addgroup --system --gid 1001 appuser \
    && adduser --system --uid 1001 --ingroup appuser appuser

WORKDIR /app

# Sao chép JAR từ giai đoạn builder
COPY --from=builder /build/target/*.jar app.jar

# Cấu hình môi trường JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=prod"

# Cổng ứng dụng
EXPOSE 8080

# Chuyển quyền sở hữu và chạy với user không phải root
RUN chown -R appuser:appuser /app
USER appuser

# Lệnh khởi động
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
