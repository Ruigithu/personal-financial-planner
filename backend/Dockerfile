FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装健康检查工具
RUN apk add --no-cache curl

# 复制构建好的JAR文件
COPY target/*.jar app.jar

# 创建必要目录
RUN mkdir -p tokens uploads

# 启动应用
CMD ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]