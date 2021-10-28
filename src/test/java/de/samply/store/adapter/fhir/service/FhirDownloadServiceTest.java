package de.samply.store.adapter.fhir.service;

import static ca.uhn.fhir.rest.api.SummaryEnum.COUNT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FhirDownloadServiceTest {

  private static final int PAGE_SIZE = 50;
  private static final String PAGE_URL = "url-185540";
  private static final int TOTAL = 212458;

  @Mock
  private IGenericClient client;

  private FhirDownloadService service;

  @Mock
  private IUntypedQuery<IBaseBundle> untypedQuery;

  @Mock
  private IQuery<IBaseBundle> query1;

  @Mock
  private IQuery<IBaseBundle> query2;

  @Mock
  private IQuery<IBaseBundle> query3;

  @Mock
  private IQuery<IBaseBundle> query4;

  @Mock
  private IQuery<IBaseBundle> query5;

  @Mock
  private IQuery<IBaseBundle> query6;

  @Mock
  private IQuery<IBaseBundle> query7;

  @Mock
  private IQuery<IBaseBundle> query8;

  @Mock
  private IUntypedQuery<IBaseBundle> untypedTotalQuery;

  @Mock
  private IQuery<IBaseBundle> totalQuery1;

  @Mock
  private IQuery<IBaseBundle> totalQuery2;

  @BeforeEach
  void setUp() {
    service = new FhirDownloadService(client, PAGE_SIZE);
  }

  @Test
  void runQuery_withTotal() {
    when(client.search()).thenReturn(untypedQuery);
    when(untypedQuery.byUrl(FhirDownloadService.SEARCH_URL)).thenReturn(query1);
    when(query1.revInclude(new Include("Observation:patient"))).thenReturn(query2);
    when(query2.revInclude(new Include("Condition:patient"))).thenReturn(query3);
    when(query3.revInclude(new Include("Specimen:patient"))).thenReturn(query4);
    when(query4.revInclude(new Include("Procedure:patient"))).thenReturn(query5);
    when(query5.revInclude(new Include("MedicationStatement:patient"))).thenReturn(query6);
    when(query6.revInclude(new Include("ClinicalImpression:patient"))).thenReturn(query7);
    when(query7.count(PAGE_SIZE)).thenReturn(query8);
    var expectedBundle = new Bundle();
    expectedBundle.setTotal(211847);
    when(query8.execute()).thenReturn(expectedBundle);

    var bundle = service.runQuery();

    assertSame(expectedBundle, bundle);
  }

  @Test
  void runQuery_withoutTotal() {
    when(client.search()).thenReturn(untypedQuery, untypedTotalQuery);
    when(untypedQuery.byUrl(FhirDownloadService.SEARCH_URL)).thenReturn(query1);
    when(query1.revInclude(new Include("Observation:patient"))).thenReturn(query2);
    when(query2.revInclude(new Include("Condition:patient"))).thenReturn(query3);
    when(query3.revInclude(new Include("Specimen:patient"))).thenReturn(query4);
    when(query4.revInclude(new Include("Procedure:patient"))).thenReturn(query5);
    when(query5.revInclude(new Include("MedicationStatement:patient"))).thenReturn(query6);
    when(query6.revInclude(new Include("ClinicalImpression:patient"))).thenReturn(query7);
    when(query7.count(PAGE_SIZE)).thenReturn(query8);
    var normalBundle = new Bundle();
    when(query8.execute()).thenReturn(normalBundle);
    when(untypedTotalQuery.byUrl(FhirDownloadService.SEARCH_URL)).thenReturn(totalQuery1);
    when(totalQuery1.summaryMode(COUNT)).thenReturn(totalQuery2);
    var totalBundle = new Bundle();
    totalBundle.setTotal(TOTAL);
    when(totalQuery2.execute()).thenReturn(totalBundle);

    var total = service.runQuery().getTotal();

    assertEquals(TOTAL, total);
  }

  @Test
  void fetchPage() {
    var expectedBundle = new Bundle();
    when(client.fetchResourceFromUrl(Bundle.class, PAGE_URL)).thenReturn(expectedBundle);

    var bundle = service.fetchPage(PAGE_URL);

    assertSame(expectedBundle, bundle);
  }
}
