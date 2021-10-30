package de.samply.store.adapter.fhir.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.store.adapter.fhir.util.Either;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FhirDownloadServiceTest {

  private static final String LIBRARY_URI = "http://dktk.dkfz.de/fhir/Library/Query";
  private static final String MEASURE_URI = "http://dktk.dkfz.de/fhir/Measure/Query";
  private static final int PAGE_SIZE = 50;
  private static final String PAGE_URL = "url-185540";
  private static final int TOTAL = 212458;
  private static final String LIST_ID = "181102";

  @Mock
  private FhirService fhirService;

  @Mock
  private IGenericClient client;

  private FhirDownloadService service;

  @BeforeEach
  void setUp() {
    service = new FhirDownloadService(FhirContext.forR4(), fhirService, client, PAGE_SIZE);
  }

  @Test
  void runQuery_withTotal() {
    when(fhirService.resourceExists(Library.class, LIBRARY_URI)).thenReturn(Either.right(true));
    when(fhirService.resourceExists(Measure.class, MEASURE_URI)).thenReturn(Either.right(true));
    var report = new MeasureReport();
    report.getGroupFirstRep().getPopulationFirstRep().getSubjectResults()
        .setReference("List/" + LIST_ID);
    when(fhirService.evaluateMeasure(MEASURE_URI)).thenReturn(Either.right(report));
    var expectedBundle = new Bundle();
    expectedBundle.setTotal(TOTAL);
    when(fhirService.fetchFirstPage(LIST_ID, PAGE_SIZE)).thenReturn(Either.right(expectedBundle));

    var bundle = service.runQuery();

    assertEquals(Either.right(expectedBundle), bundle);
  }

  @Test
  void runQuery_withoutTotal() {
    when(fhirService.resourceExists(Library.class, LIBRARY_URI)).thenReturn(Either.right(true));
    when(fhirService.resourceExists(Measure.class, MEASURE_URI)).thenReturn(Either.right(true));
    var report = new MeasureReport();
    report.getGroupFirstRep().getPopulationFirstRep().getSubjectResults()
        .setReference("List/" + LIST_ID);
    when(fhirService.evaluateMeasure(MEASURE_URI)).thenReturn(Either.right(report));
    var normalBundle = new Bundle();
    when(fhirService.fetchFirstPage(LIST_ID, PAGE_SIZE)).thenReturn(Either.right(normalBundle));
    var totalBundle = new Bundle();
    totalBundle.setTotal(TOTAL);
    when(fhirService.fetchTotalBundle(LIST_ID)).thenReturn(Either.right(totalBundle));

    var total = service.runQuery().map(Bundle::getTotal);

    assertEquals(Either.right(TOTAL), total);
  }

  @Test
  void fetchPage() {
    var expectedBundle = new Bundle();
    when(client.fetchResourceFromUrl(Bundle.class, PAGE_URL)).thenReturn(expectedBundle);

    var bundle = service.fetchPage(PAGE_URL);

    assertSame(expectedBundle, bundle);
  }
}
