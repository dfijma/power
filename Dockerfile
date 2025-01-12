

FROM  amazoncorretto:21.0.4-alpine3.18
WORKDIR /app
COPY target/power-1.0-SNAPSHOT.jar .
CMD ["java", "-jar", "power-1.0-SNAPSHOT.jar"]
