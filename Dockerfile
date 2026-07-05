FROM maven:3.9.10-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
