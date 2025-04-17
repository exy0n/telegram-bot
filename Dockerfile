FROM alpine:3.19.1

RUN apk add openjdk21
COPY build/libs/TelegramBot-1.0.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]