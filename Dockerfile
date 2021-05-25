FROM gcr.io/distroless/java-debian10:11


COPY --chown=nonroot:nonroot target/store-fhir-adapter.jar /app/

USER nonroot
WORKDIR /app

CMD ["store-fhir-adapter.jar"]
