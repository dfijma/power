
FROM  maven:3.9.6-amazoncorretto-21-debian AS builder
WORKDIR /app
COPY pom.xml ./
COPY ./src ./src
RUN mvn clean  package

FROM maven:3.9.6-amazoncorretto-21-debian
WORKDIR /app
COPY --from=builder /app/target/power-1.0-SNAPSHOT.jar .
CMD ["java", "-jar", "power-1.0-SNAPSHOT.jar"]
