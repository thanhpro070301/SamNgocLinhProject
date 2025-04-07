services:
  - type: web
    name: sam-ngoc-linh-api
    env: docker
    buildCommand: docker build -t sam-ngoc-linh-api .
    startCommand: docker run -p 8080:8080 sam-ngoc-linh-api
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: SERVER_PORT
        value: 8080
      # Thêm các biến môi trường khác nếu cần
      - key: SPRING_DATASOURCE_URL
        sync: false
      - key: SPRING_DATASOURCE_USERNAME
        sync: false
      - key: SPRING_DATASOURCE_PASSWORD
        sync: false
