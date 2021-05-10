FROM gcr.io/distroless/java-debian10:11

WORKDIR /app

COPY --chown=nonroot:nonroot target/store-fhir-adapter.jar ./

USER nonroot

CMD ["store-fhir-adapter.jar"]
