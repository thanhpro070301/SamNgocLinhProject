FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /build

# Copy Maven files first for better layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Đảm bảo mvnw có quyền thực thi
RUN chmod +x ./mvnw

# Cấu hình Maven để bỏ qua tests
RUN ./mvnw -DskipTests -Dmaven.test.skip=true process-resources

# Download dependencies first (leverages Docker cache)
RUN ./mvnw dependency:go-offline -B -DskipTests

# Then copy source code
COPY src src

# Build ứng dụng
RUN ./mvnw package -DskipTests && \
    rm -rf ~/.m2 # Xóa cache Maven để giảm kích thước image

# Giai đoạn thứ 2 - Runtime
FROM eclipse-temurin:21-jre-alpine

# Cài đặt các tiện ích cần thiết
RUN apk --no-cache add curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    echo "Asia/Ho_Chi_Minh" > /etc/timezone && \
    apk del tzdata

# Tạo user không phải root để chạy ứng dụng
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --ingroup appuser appuser

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
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1

