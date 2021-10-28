package de.samply.store.adapter.fhir.service;

import static ca.uhn.fhir.rest.api.SummaryEnum.COUNT;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.List;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
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
  private static final List<String> CODES = List.of(
      "C00.0", "C00.1", "C00.3", "C00.4", "C00.6", "C00.9", "C01", "C01.9", "C02", "C02.1", "C02.2",
      "C02.3", "C02.4", "C02.8", "C02.9", "C03.0", "C03.1", "C03.9", "C04", "C04.0", "C04.1",
      "C04.8", "C04.9", "C05.0", "C05.1", "C05.2", "C05.8", "C05.9", "C06.0", "C06.1", "C06.2",
      "C06.8", "C06.9", "C07", "C07.9", "C08.0", "C08.9", "C09", "C09.0", "C09.1", "C09.8", "C09.9",
      "C10", "C10.0", "C10.2", "C10.3", "C10.8", "C10.9", "C11", "C11.0", "C11.2", "C11.8", "C11.9",
      "C12", "C13", "C13.0", "C13.1", "C13.2", "C13.8", "C13.9", "C14.0", "C15", "C15.0", "C15.1",
      "C15.2", "C15.3", "C15.4", "C15.5", "C15.8", "C15.9", "C16", "C16.0", "C16.1", "C16.2",
      "C16.3", "C16.4", "C16.5", "C16.6", "C16.8", "C16.9", "C17", "C17.0", "C17.1", "C17.2",
      "C17.8", "C17.9", "C18", "C18.0", "C18.1", "C18.2", "C18.3", "C18.4", "C18.5", "C18.6",
      "C18.7", "C18.9", "C19", "C19.9", "C20", "C20.9", "C21.0", "C21.1", "C21.2", "C21.8", "C22",
      "C22.0", "C22.1", "C22.2", "C22.3", "C22.4", "C22.7", "C22.9", "C23", "C24", "C24.0", "C24.1",
      "C24.8", "C25", "C25.0", "C25.1", "C25.2", "C25.8", "C25.9", "C26.9", "C30.0", "C30.1", "C31",
      "C31.0", "C31.1", "C31.2", "C31.8", "C31.9", "C32", "C32.0", "C32.1", "C32.2", "C32.8",
      "C32.9", "C34", "C34.0", "C34.1", "C34.2", "C34.3", "C34.8", "C34.9", "C37", "C37.9", "C38.1",
      "C38.2", "C38.3", "C38.4", "C40.0", "C40.2", "C41.0", "C41.1", "C41.2", "C41.3", "C41.4",
      "C41.9", "C42.2", "C43.0", "C43.1", "C43.2", "C43.3", "C43.4", "C43.5", "C43.6", "C43.7",
      "C43.9", "C44", "C44.0", "C44.1", "C44.2", "C44.3", "C44.4", "C44.5", "C44.6", "C44.7",
      "C44.8", "C44.9", "C45.0", "C45.1", "C45.7", "C46.0", "C46.1", "C46.2", "C46.7", "C46.8",
      "C47.0", "C47.1", "C47.2", "C47.6", "C47.9", "C48", "C48.0", "C48.1", "C48.2", "C48.8", "C49",
      "C49.0", "C49.1", "C49.2", "C49.3", "C49.4", "C49.5", "C49.8", "C49.9", "C50", "C50.0",
      "C50.1", "C50.2", "C50.4", "C50.8", "C50.9", "C51", "C51.0", "C51.1", "C51.2", "C51.8",
      "C51.9", "C52", "C52.9", "C53", "C53.0", "C53.1", "C53.8", "C53.9", "C54.1", "C54.2", "C54.8",
      "C54.9", "C55", "C55.9", "C56", "C56.9", "C57.0", "C57.4", "C57.9", "C60", "C60.0", "C60.1",
      "C60.2", "C60.8", "C60.9", "C61", "C61.9", "C62", "C62.1", "C62.9", "C63.1", "C63.2", "C63.9",
      "C64", "C64.9", "C65", "C65.9", "C66", "C67", "C67.0", "C67.1", "C67.2", "C67.3", "C67.4",
      "C67.5", "C67.6", "C67.8", "C67.9", "C68", "C68.0", "C68.8", "C68.9", "C69.0", "C69.3",
      "C69.4", "C69.6", "C69.9", "C70.0", "C71.0", "C71.1", "C71.2", "C71.3", "C71.4", "C71.6",
      "C71.7", "C71.8", "C71.9", "C72.0", "C73", "C73.9", "C74", "C74.0", "C74.1", "C74.9", "C75.0",
      "C75.1", "C75.3", "C75.5", "C75.8", "C76", "C76.0", "C76.1", "C76.2", "C76.3", "C76.4",
      "C76.5", "C76.7", "C76.8", "C77", "C77.0", "C77.1", "C77.2", "C77.3", "C77.4", "C77.5",
      "C77.9", "C79.5", "C80", "C80.0", "C81.0", "C81.1", "C81.2", "C81.4", "C81.9", "C82.0",
      "C82.1", "C82.2", "C82.3", "C82.4", "C82.5", "C82.6", "C82.9", "C83.0", "C83.1", "C83.3",
      "C83.5", "C83.7", "C83.8", "C84.0", "C84.1", "C84.4", "C84.5", "C84.6", "C84.7", "C84.8",
      "C85", "C85.1", "C85.2", "C85.7", "C85.9", "C86.0", "C86.1", "C86.4", "C86.5", "C88.0",
      "C88.4", "C88.7", "C90.0", "C90.2", "C91.0", "C91.1", "C91.3", "C91.4", "C91.5", "C91.6",
      "C91.8", "C92.0", "C92.1", "C92.2", "C92.3", "C92.4", "C92.5", "C92.6", "C92.8", "C93.0",
      "C93.1", "C93.3", "C94.0", "C94.2", "C94.8", "C95.0", "C96.2", "C96.5", "C96.6", "C96.9",
      "D00.0", "D00.1", "D01.0", "D01.2", "D01.3", "D01.5", "D02.0", "D03.0", "D03.1", "D03.2",
      "D03.3", "D03.4", "D03.5", "D03.6", "D03.7", "D04.2", "D04.3", "D04.4", "D04.6", "D05.1",
      "D06.0", "D06.1", "D06.7", "D06.9", "D07.0", "D07.1", "D07.2", "D07.3", "D07.4", "D07.6",
      "D09.0", "D09.1", "D18.18", "D32.0", "D32.1", "D32.9", "D33", "D33.0", "D33.1", "D33.3",
      "D33.9", "D35.2", "D37.1", "D37.2", "D37.5", "D37.7", "D39.0", "D39.1", "D40.1", "D41.0",
      "D41.1", "D41.4", "D42.0", "D42.1", "D43.0", "D43.1", "D43.2", "D43.3", "D43.4", "D44.3",
      "D44.4", "D45", "D46", "D46.0", "D46.1", "D46.2", "D46.5", "D46.6", "D46.7", "D46.9", "D47.0",
      "D47.1", "D47.2", "D47.3", "D47.4", "D47.5", "D48.0", "D48.1", "D48.5", "Q85.0"
  );
  static final String SEARCH_URL = "Patient?_has:Condition:patient:code=" + String.join(",", CODES);

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
    Bundle bundle = fetchFirstPage();
    if (!bundle.hasTotalElement()) {
      bundle.setTotal(fetchTotalBundle().getTotal());
    }
    return bundle;
  }

  private Bundle fetchFirstPage() {
    return (Bundle) client.search()
        .byUrl(SEARCH_URL)
        .revInclude(new Include("Observation:patient"))
        .revInclude(new Include("Condition:patient"))
        .revInclude(new Include("Specimen:patient"))
        .revInclude(new Include("Procedure:patient"))
        .revInclude(new Include("MedicationStatement:patient"))
        .revInclude(new Include("ClinicalImpression:patient"))
        .count(pageSize)
        .execute();
  }

  private Bundle fetchTotalBundle() {
    return (Bundle) client.search()
        .byUrl(SEARCH_URL)
        .summaryMode(COUNT)
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
