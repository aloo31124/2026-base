# ====================================
# AgentFlow Base - 後端 Dockerfile
# Multi-stage: Gradle bootJar 建置 + JRE 執行
# GCP Cloud Run 部署用
# ====================================

# Stage 1: 以 Gradle Wrapper 建置 Spring Boot 可執行 Jar
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# 先複製 Gradle Wrapper 與建置腳本，利用層快取加速重建
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle/ ./gradle/
RUN chmod +x gradlew

COPY src/ ./src/

# bootJar 不會觸發 frontendBuild 與 test，僅產出後端 Jar
RUN ./gradlew bootJar --no-daemon

# Stage 2: 僅帶 JRE 的輕量執行環境
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

# 容器記憶體上限的 75% 交給 JVM，避免 Cloud Run OOM
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Taipei"

# Cloud Run 以 PORT 環境變數指定監聽埠（預設 8080），對齊 application.yml 的 SERVER_PORT
EXPOSE 8080

CMD ["sh", "-c", "SERVER_PORT=${PORT:-8080} java -jar app.jar"]
