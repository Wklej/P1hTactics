FROM eclipse-temurin:17

WORKDIR /app

COPY build/libs/P1hTactics-0.2.1.jar P1hTactics.jar

EXPOSE 8080

CMD ["java", "-jar", "P1hTactics.jar"]