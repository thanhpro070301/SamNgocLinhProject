# Sử dụng OpenJDK 21
FROM openjdk:21

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy file JAR vào container
COPY target/your-app.jar app.jar

# Lệnh chạy ứng dụng
CMD ["java", "-jar", "app.jar"]
