# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Resolve dependencies in their own layer so source edits don't refetch them.
COPY pom.xml .
RUN mvn -B -ntp -DskipTests dependency:go-offline

COPY src src
RUN mvn -B -ntp -DskipTests package && mv target/*.jar target/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system --gid 1001 app \
 && useradd --system --uid 1001 --gid app --no-create-home app
COPY --from=build --chown=app:app /workspace/target/app.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
