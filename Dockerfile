FROM bellsoft/liberica-openjdk-alpine:17

CMD ["./gradlew", "clean", "build"]

VOLUME /tmp

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar
COPY ./src/main/resources/firebase/firebase_service_key.json /app/src/main/resources/firebase/firebase_service_key.json

EXPOSE 8080

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev","/app.jar"]