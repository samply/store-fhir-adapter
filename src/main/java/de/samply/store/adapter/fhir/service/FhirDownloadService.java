package de.samply.store.adapter.fhir.service;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.store.adapter.fhir.util.Anomaly;
import de.samply.store.adapter.fhir.util.Anomaly.Fault;
import de.samply.store.adapter.fhir.util.Either;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The FHIR backend.
 *
 * <p>It can {@link #runQuery() run queries} and {@link #fetchPage(String) fetch individual pages}.
 */
@Service
public class FhirDownloadService {

  private static final Logger logger = LoggerFactory.getLogger(FhirDownloadService.class);
  private static final String LIBRARY_URI = "http://dktk.dkfz.de/fhir/Library/Query";
  private static final String MEASURE_URI = "http://dktk.dkfz.de/fhir/Measure/Query";

  private final FhirContext fhirContext;
  private final FhirService fhirService;
  private final IGenericClient client;
  private final int pageSize;

  /**
   * Creates a new {@code FhirDownloadService}.
   *
   * @param fhirContext the HAPI FHIR context
   * @param fhirService the FHIR service
   * @param client      the HAPI FHIR client
   * @param pageSize    the number of patients per page
   */
  public FhirDownloadService(FhirContext fhirContext, FhirService fhirService,
      IGenericClient client, @Value("${app.store.page-size}") int pageSize) {
    this.fhirContext = fhirContext;
    this.fhirService = fhirService;
    this.client = Objects.requireNonNull(client);
    this.pageSize = pageSize;
  }

  /**
   * Runs a query that selects all patients and returns the corresponding {@link Bundle}.
   *
   * @return a Right with the bundle or a Left in case of an error
   */
  public Either<String, Bundle> runQuery() {
    logger.debug("Run query");
    return initResources()
        .flatMap(foo -> fhirService.evaluateMeasure(MEASURE_URI))
        .map(measureReport -> measureReport.getGroupFirstRep().getPopulationFirstRep())
        .map(population -> population.getSubjectResults().getReferenceElement().getIdPart())
        .flatMap(listId -> fhirService.fetchFirstPage(listId, pageSize)
            .flatMap(bundle -> bundle.hasTotalElement()
                ? Either.right(bundle)
                : fhirService.fetchTotalBundle(listId).map(b -> bundle.setTotal(b.getTotal())))
        );
  }

  /**
   * Tries to create Library and Measure resources if not present on the FHIR server.
   *
   * @return either an error or nothing
   */
  private Either<String, Void> initResources() {
    return initLibrary().flatMap(foo -> initMeasure());
  }

  private Either<String, Void> initLibrary() {
    return fhirService.resourceExists(Library.class, LIBRARY_URI)
        .flatMap(exists -> exists
            ? Either.right()
            : slurp("Library.json")
                .flatMap(s -> parseResource(Library.class, s))
                .flatMap(this::appendCql)
                .flatMap(this::createResource));
  }

  private Either<String, Void> initMeasure() {
    return fhirService.resourceExists(Measure.class, MEASURE_URI)
        .flatMap(exists -> exists
            ? Either.right()
            : slurp("Measure.json")
                .flatMap(s -> parseResource(Measure.class, s))
                .flatMap(this::createResource));
  }

  private static Either<String, String> slurp(String name) {
    try (InputStream in = FhirDownloadService.class.getResourceAsStream(name)) {
      if (in == null) {
        logger.error("file `{}` not found in classpath", name);
        return Either.left(format("file `%s` not found in classpath", name));
      } else {
        logger.info("read file `{}` from classpath", name);
        return Either.right(new String(in.readAllBytes(), UTF_8));
      }
    } catch (IOException e) {
      logger.error("error while reading the file `{}` from classpath", name, e);
      return Either.left(format("error while reading the file `%s` from classpath", name));
    }
  }

  private <T extends IBaseResource> Either<String, T> parseResource(Class<T> type, String s) {
    var parser = fhirContext.newJsonParser();
    return Either.tryGet(() -> type.cast(parser.parseResource(s))).mapLeft(Exception::getMessage);
  }

  private Either<String, Library> appendCql(Library library) {
    return slurp("query.cql").map(cql -> {
      library.getContentFirstRep().setContentType("text/cql");
      library.getContentFirstRep().setData(cql.getBytes(UTF_8));
      return library;
    });
  }

  private Either<String, Void> createResource(IBaseResource resource) {
    return Either.tryGet(() -> client.create().resource(resource).encodedJson().execute())
        .mapLeft(Exception::getMessage)
        .flatMap(outcome -> outcome.getCreated()
            ? Either.right()
            : Either.left("error while creating a resource"));
  }

  /**
   * Returns the bundle of a page with {@code pageUrl}.
   *
   * @param pageUrl the URL of the page to fetch
   * @return a Right with the bundle or a Left in case of an error
   */
  public Either<Anomaly, Bundle> fetchPage(String pageUrl) {
    logger.debug("fetch page pageUrl={}", pageUrl);
    return Either.tryGet(() -> client.fetchResourceFromUrl(Bundle.class, pageUrl))
        .mapLeft(e -> new Fault(e.getMessage()));
  }
}
