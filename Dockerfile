# Сборка WAR на Maven
FROM maven:3.9.11-ibm-semeru-21-noble AS build
WORKDIR /build
COPY . .
COPY ./env/settings.xml /root/.m2/settings.xml
RUN mvn clean package -DskipTests=true -Dmaven.test.skip=true

# Деплой WAR в Tomcat
FROM tomcat:jdk21-openjdk-slim
COPY --from=build /build/api/target/api-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
COPY ./env/tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
EXPOSE 8080