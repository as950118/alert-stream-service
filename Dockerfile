# Java 21 기반 Spring Boot 애플리케이션 Docker 이미지
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew gradlew.bat ./
COPY gradle gradle/

# build.gradle 및 settings.gradle 복사
COPY build.gradle settings.gradle ./

# 소스 코드 복사
COPY src src/

# Gradle 빌드 실행
RUN ./gradlew build -x test

# 애플리케이션 JAR 파일 복사
COPY build/libs/alert-stream-service-*.jar app.jar

# 포트 노출
EXPOSE 8080

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
