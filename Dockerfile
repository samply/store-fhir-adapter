FROM eclipse-temurin:17-focal

COPY target/store-fhir-adapter.jar /app/

WORKDIR /app
USER 1001

CMD ["java", "-jar", "store-fhir-adapter.jar"]
