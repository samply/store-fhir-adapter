package de.samply.store.adapter.fhir.api;

import de.samply.share.model.ccp.QueryResult;
import de.samply.store.adapter.fhir.model.Result;
import de.samply.store.adapter.fhir.service.FhirDownloadService;
import de.samply.store.adapter.fhir.service.MappingService;
import de.samply.store.adapter.fhir.service.ResultStore;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Alexander Kiel
 */
@ExtendWith(MockitoExtension.class)
class StoreRestControllerTest {

    public static final String RESULT_ID = "id-142731";
    public static final int TOTAL = 143513;
    public static final int PAGE_SIZE = 50;
    public static final String PAGE_URL = "url-143738";
    public static final String NEXT_PAGE_URL = "url-181450";

    @Mock
    private FhirDownloadService downloadService;

    @Mock
    private MappingService mappingService;

    @Mock
    private ResultStore resultStore;

    private StoreRestController controller;

    @BeforeEach
    void setUp() {
        controller = new StoreRestController(downloadService, mappingService, resultStore, PAGE_SIZE);
    }

    @Test
    void createRequest() {
        when(downloadService.runQuery()).thenReturn(Result.of(RESULT_ID, "url-foo", 0));

        var responseEntity = controller.createRequest("<foo></foo>");

        assertEquals("/requests/" + RESULT_ID, responseEntity.getHeaders().getFirst("location"));
    }

    @Test
    void getStats() {
        when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(Result.of(RESULT_ID, "url-foo", TOTAL)));

        var stats = controller.getStats(RESULT_ID);

        assertEquals(TOTAL, stats.getTotalSize());
        assertEquals((int) Math.ceil((double) TOTAL / PAGE_SIZE), stats.getNumberOfPages());
    }

    @Test
    void getStats_NotFound() {
        when(resultStore.get(RESULT_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(ResponseStatusException.class, () -> controller.getStats(RESULT_ID));

        assertEquals(NOT_FOUND, exception.getStatus());
    }

    @Test
    void getResult() {
        when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(Result.of(RESULT_ID, PAGE_URL, TOTAL)));
        var bundle = new Bundle();
        when(downloadService.fetchPage(PAGE_URL)).thenReturn(bundle);
        var expectedResult = new QueryResult();
        when(mappingService.map(bundle)).thenReturn(expectedResult);

        var result = controller.getResult(RESULT_ID, 0);

        assertEquals(expectedResult, result);
    }

    @Test
    void getResult_NextPageUrlIsSaved() {
        when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(Result.of(RESULT_ID, PAGE_URL, TOTAL)));
        var bundle = new Bundle();
        bundle.getLinkOrCreate("next").setUrl(NEXT_PAGE_URL);
        when(downloadService.fetchPage(PAGE_URL)).thenReturn(bundle);
        when(mappingService.map(bundle)).thenReturn(new QueryResult());

        controller.getResult(RESULT_ID, 0);

        verify(resultStore).save(Result.of(RESULT_ID, PAGE_URL, TOTAL).withPageUrl(1, NEXT_PAGE_URL));
    }

    @Test
    void getResult_NotFound() {
        when(resultStore.get(RESULT_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(RequestNotFoundException.class, () -> controller.getResult(RESULT_ID, 0));

        assertEquals(RESULT_ID, exception.getId());
    }

    @Test
    void getResult_MissingPageUrl() {
        when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(Result.of(RESULT_ID, PAGE_URL, TOTAL)));

        var exception = assertThrows(MissingPageUrlException.class, () -> controller.getResult(RESULT_ID, 1));

        assertEquals(RESULT_ID, exception.getId());
        assertEquals(1, exception.getPageNum());
    }
}
