package de.samply.store.adapter.fhir.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import de.samply.store.adapter.fhir.util.Either;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FhirServiceTest {

  private static final String LIST_ID = "181715";
  private static final int PAGE_SIZE = 50;

  @Mock
  private IGenericClient client;

  @InjectMocks
  private FhirService service;

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
  private IQuery<Bundle> query9;

  @Test
  void fetchFirstPage() {
    when(client.search()).thenReturn(untypedQuery);
    when(untypedQuery.byUrl("Patient?_list=" + LIST_ID)).thenReturn(query1);
    when(query1.revInclude(new Include("Observation:patient"))).thenReturn(query2);
    when(query2.revInclude(new Include("Condition:patient"))).thenReturn(query3);
    when(query3.revInclude(new Include("Specimen:patient"))).thenReturn(query4);
    when(query4.revInclude(new Include("Procedure:patient"))).thenReturn(query5);
    when(query5.revInclude(new Include("MedicationStatement:patient"))).thenReturn(query6);
    when(query6.revInclude(new Include("ClinicalImpression:patient"))).thenReturn(query7);
    when(query7.count(PAGE_SIZE)).thenReturn(query8);
    when(query8.returnBundle(Bundle.class)).thenReturn(query9);
    var expectedBundle = new Bundle();
    expectedBundle.setTotal(211847);
    when(query9.execute()).thenReturn(expectedBundle);

    var bundle = service.fetchFirstPage(LIST_ID, PAGE_SIZE);

    assertEquals(Either.right(expectedBundle), bundle);
  }
}
