FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY shareit-gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]