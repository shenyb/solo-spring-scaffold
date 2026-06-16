FROM eclipse-temurin:{{jdkVersion}}-jre-alpine AS runtime

# 元数据
LABEL maintainer="solo-spring-scaffold"
LABEL description="{{projectName}} - Spring Boot Application"

# 时区 + 语言
RUN apk add --no-cache tzdata curl \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

WORKDIR /app

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 分层构建：先拷贝依赖层（变化少）
COPY {{projectName}}-web/target/{{projectName}}-web-1.0.0.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

USER appuser

# JVM 参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
