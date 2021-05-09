package de.samply.store.adapter.fhir.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.store.adapter.fhir.model.Result;
import java.util.Objects;
import java.util.function.Supplier;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The FHIR backend.
 *
 * <p>It can {@link #runQuery() run queries} and {@link #fetchPage(String) fetch individual pages}.
 *
 * @author Alexander Kiel
 */
@Service
public class FhirDownloadService {

  private static final Logger logger = LoggerFactory.getLogger(FhirDownloadService.class);

  private final IGenericClient client;
  private final int pageSize;
  private final Supplier<String> resultIdSupplier;

  /**
   * Creates a new {@code FhirDownloadService}.
   *
   * @param client the HAPI FHIR client
   * @param pageSize the number of patients per page
   * @param resultIdSupplier the supplier of result identifiers
   */
  public FhirDownloadService(IGenericClient client, @Value("${app.store.page-size}") int pageSize,
      Supplier<String> resultIdSupplier) {
    this.client = Objects.requireNonNull(client);
    this.pageSize = pageSize;
    this.resultIdSupplier = Objects.requireNonNull(resultIdSupplier);
  }

  /**
   * Runs a query that selects all patients and returns a result with the first page URL and the
   * total number of found patients.
   *
   * <p>Please use {@link #fetchPage(String)} in order to fetch the contents of the first page.
   *
   * @return the result
   */
  public Result runQuery() {
    var bundle = internRunQuery();
    return Result.of(resultIdSupplier.get(), bundle.getLink("self").getUrl(), bundle.getTotal());
  }

  private Bundle internRunQuery() {
    logger.debug("Run query");
    return (Bundle) client.search().forResource(Patient.class).count(pageSize).execute();
  }

  /**
   * Returns the bundle of a page with {@code pageUrl}.
   *
   * @param pageUrl the URL of the page to fetch
   * @return the bundle of the page with {@code pageUrl}
   */
  public Bundle fetchPage(String pageUrl) {
    logger.debug("fetch page pageUrl={}", pageUrl);
    return client.fetchResourceFromUrl(Bundle.class, pageUrl);
  }
}
