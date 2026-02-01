# Zeabur 部署用 Dockerfile - 使用已打包的 jar
FROM openjdk:8-jdk-alpine

WORKDIR /app

# 复制本地打包好的 jar 文件
COPY target/notespace-serve-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
