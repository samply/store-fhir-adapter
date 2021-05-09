package de.samply.store.adapter.fhir.service;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Kiel
 */
@ExtendWith(MockitoExtension.class)
class FhirDownloadServiceTest {

    public static final String RESULT_ID = "id-142731";
    public static final int PAGE_SIZE = 50;
    public static final int TOTAL = 183953;
    public static final String PAGE_URL = "url-185540";

    @Mock
    private IGenericClient client;

    private FhirDownloadService service;

    @Mock
    private IUntypedQuery<IBaseBundle> untypedQuery;

    @Mock
    private IQuery<IBaseBundle> query;

    @Mock
    private IQuery<IBaseBundle> countQuery;

    @BeforeEach
    void setUp() {
        service = new FhirDownloadService(client, PAGE_SIZE, () -> RESULT_ID);
    }

    @Test
    void runQuery() {
        when(client.search()).thenReturn(untypedQuery);
        when(untypedQuery.forResource(Patient.class)).thenReturn(query);
        when(query.count(PAGE_SIZE)).thenReturn(countQuery);
        var bundle = new Bundle();
        bundle.getLinkOrCreate("self").setUrl(PAGE_URL);
        bundle.setTotal(TOTAL);
        when(countQuery.execute()).thenReturn(bundle);

        var result = service.runQuery();

        assertEquals(RESULT_ID, result.getId());
        assertTrue(result.getPageUrl(0).isPresent());
        assertEquals(PAGE_URL, result.getPageUrl(0).get());
        assertEquals(TOTAL, result.getTotal());
    }

    @Test
    void fetchPage() {
        var expectedBundle = new Bundle();
        when(client.fetchResourceFromUrl(Bundle.class, PAGE_URL)).thenReturn(expectedBundle);

        var bundle = service.fetchPage(PAGE_URL);

        assertEquals(expectedBundle, bundle);
    }
}
