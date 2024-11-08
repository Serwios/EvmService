FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/evmservice-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/src/main/resources/db/changelog/db.changelog-master.xml /app/db/changelog/db.changelog-master.xml
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]