# Zeabur 部署用 Dockerfile
FROM openjdk:8-jdk-alpine

WORKDIR /app

# 复制 jar 文件
COPY target/notespace-serve-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置生产环境 profile
ENV SPRING_PROFILES_ACTIVE=prod

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
