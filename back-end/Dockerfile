FROM eclipse-temurin:17-jdk-alpine

LABEL maintainer="RestaurantMap Backend"
LABEL description="RestaurantMap Backend Service"

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven

RUN mvn clean package -DskipTests

EXPOSE 8080

ENV PORT=8080
ENV JAVA_OPTS="-Xmx768m -Xms512m -Dserver.tomcat.startup-timeout=300000 -Dspring.profiles.active=prod -Dserver.port=8080"

CMD java $JAVA_OPTS -jar target/demo-0.0.1-SNAPSHOT.jar 