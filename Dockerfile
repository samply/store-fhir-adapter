FROM eclipse-temurin:17-focal

COPY target/store-fhir-adapter.jar /app/

WORKDIR /app

CMD ["java", "-jar", "store-fhir-adapter.jar"]
