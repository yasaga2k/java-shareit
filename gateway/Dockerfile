FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
COPY target/shareit-gateway-*.jar app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]