FROM adoptopenjdk:15-jre-hotspot


COPY target/store-fhir-adapter.jar /app/

WORKDIR /app



CMD ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005", "-jar", "store-fhir-adapter.jar"]