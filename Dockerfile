FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B package -DskipTests && \
    cp target/*.jar app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --create-home appuser
USER appuser

COPY --from=build /workspace/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
