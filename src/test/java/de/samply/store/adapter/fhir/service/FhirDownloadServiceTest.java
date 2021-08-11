package de.samply.store.adapter.fhir.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Alexander Kiel
 */
@ExtendWith(MockitoExtension.class)
class FhirDownloadServiceTest {

  public static final int PAGE_SIZE = 50;
  public static final String PAGE_URL = "url-185540";

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
  private IQuery<IBaseBundle> countQuery;

  @BeforeEach
  void setUp() {
    service = new FhirDownloadService(client, PAGE_SIZE);
  }

  @Test
  void runQuery() {
    when(client.search()).thenReturn(untypedQuery);
    when(untypedQuery.forResource(Patient.class)).thenReturn(query1);
    when(query1.revInclude(new Include("Observation:patient"))).thenReturn(query2);
    when(query2.revInclude(new Include("Condition:patient"))).thenReturn(query3);
    when(query3.revInclude(new Include("Specimen:patient"))).thenReturn(query4);
    when(query4.revInclude(new Include("Procedure:patient"))).thenReturn(query5);
    when(query5.revInclude(new Include("MedicationStatement:patient"))).thenReturn(query6);
    when(query6.revInclude(new Include("ClinicalImpression:patient"))).thenReturn(query7);
    when(query7.count(PAGE_SIZE)).thenReturn(countQuery);
    var expectedBundle = new Bundle();
    when(countQuery.execute()).thenReturn(expectedBundle);

    var bundle = service.runQuery();

    assertSame(expectedBundle, bundle);
  }

  @Test
  void fetchPage() {
    var expectedBundle = new Bundle();
    when(client.fetchResourceFromUrl(Bundle.class, PAGE_URL)).thenReturn(expectedBundle);

    var bundle = service.fetchPage(PAGE_URL);

    assertSame(expectedBundle, bundle);
  }
}
