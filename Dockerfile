FROM gradle:9.1.0-jdk21 AS build
USER root
RUN apt-get update && apt-get install -y ca-certificates openssl && update-ca-certificates
WORKDIR /app
COPY . .
RUN ./gradlew clean :service:bootJar --no-daemon --stacktrace

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y ca-certificates && update-ca-certificates && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/service/build/libs/service-*.jar /app/app.jar
COPY ./env/application.yaml /app/application.yaml
COPY ./env/.env /app/.env
ENV SPRING_CONFIG_LOCATION=/app/application.yaml
ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]