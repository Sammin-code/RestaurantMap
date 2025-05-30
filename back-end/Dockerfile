FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven

RUN mvn clean package -DskipTests

EXPOSE 8080

ENV PORT=8080

CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"] 