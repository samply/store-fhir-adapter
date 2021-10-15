package de.samply.store.adapter.fhir.service;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.Objects;
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
 */
@Service
public class FhirDownloadService {

  private static final Logger logger = LoggerFactory.getLogger(FhirDownloadService.class);

  private final IGenericClient client;
  private final int pageSize;

  /**
   * Creates a new {@code FhirDownloadService}.
   *
   * @param client   the HAPI FHIR client
   * @param pageSize the number of patients per page
   */
  public FhirDownloadService(IGenericClient client, @Value("${app.store.page-size}") int pageSize) {
    this.client = Objects.requireNonNull(client);
    this.pageSize = pageSize;
  }

  /**
   * Runs a query that selects all patients and returns the corresponding {@link Bundle}.
   *
   * @return the bundle
   */
  public Bundle runQuery() {
    logger.debug("Run query");
    return (Bundle) client.search().forResource(Patient.class)
        .revInclude(new Include("Observation:patient"))
        .revInclude(new Include("Condition:patient"))
        .revInclude(new Include("Specimen:patient"))
        .revInclude(new Include("Procedure:patient"))
        .revInclude(new Include("MedicationStatement:patient"))
        .revInclude(new Include("ClinicalImpression:patient"))
        .count(pageSize)
        .execute();
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
