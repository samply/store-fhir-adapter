package de.samply.store.adapter.fhir.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import de.samply.share.model.ccp.QueryResult;
import de.samply.store.adapter.fhir.model.Result;
import de.samply.store.adapter.fhir.service.FhirDownloadService;
import de.samply.store.adapter.fhir.service.MappingService;
import de.samply.store.adapter.fhir.service.ResultStore;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StoreRestControllerTest {

  public static final String RESULT_ID = "id-142731";
  public static final int TOTAL = 143513;
  public static final int PAGE_SIZE = 50;
  public static final String VERSION = "0.1.0";
  public static final String BASE_URL = "http://localhost:8080";
  public static final String PAGE_0_URL = "url-143738";
  public static final String PAGE_1_URL = "url-181450";
  public static final String PAGE_2_URL = "url-085531";

  @Mock
  private FhirDownloadService downloadService;

  @Mock
  private MappingService mappingService;

  @Mock
  private ResultStore resultStore;

  private StoreRestController controller;

  @BeforeEach
  void setUp() {
    controller = new StoreRestController(downloadService, mappingService, resultStore, PAGE_SIZE,
        VERSION, BASE_URL);
  }

  @Test
  void getInfo() {
    var responseEntity = controller.getInfo();

    assertEquals(VERSION, responseEntity.getHeaders().getFirst("version"));
  }

  @Test
  void createRequest() throws Exception {
    var page0 = new Bundle();
    when(downloadService.runQuery()).thenReturn(page0);
    when(resultStore.create(page0)).thenReturn(new Result(RESULT_ID, TOTAL));

    var responseEntity = controller.createRequest(true, "<foo></foo>");

    assertEquals(BASE_URL + "/rest/teiler/requests/" + RESULT_ID,
        responseEntity.getHeaders().getFirst("location"));
  }

  @Test
  void getStats() {
    when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(new Result(RESULT_ID, TOTAL)));

    var stats = controller.getStats(RESULT_ID);

    assertEquals(TOTAL, stats.getTotalSize());
    assertEquals((int) Math.ceil((double) TOTAL / PAGE_SIZE), stats.getNumberOfPages());
  }

  @Test
  void getStats_NotFound() {
    when(resultStore.get(RESULT_ID)).thenReturn(Optional.empty());

    var exception = assertThrows(ResponseStatusException.class,
        () -> controller.getStats(RESULT_ID));

    assertEquals(NOT_FOUND, exception.getStatus());
  }

  @Test
  void getResult_Page0IsReturned() {
    when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(new Result(RESULT_ID, TOTAL)));
    when(resultStore.getPageUrl(RESULT_ID, 0)).thenReturn(Optional.of(PAGE_0_URL));
    var page0 = new Bundle();
    when(downloadService.fetchPage(PAGE_0_URL)).thenReturn(page0);
    var expectedResult = new QueryResult();
    when(mappingService.map(page0)).thenReturn(expectedResult);

    var result = controller.getResult(RESULT_ID, 0);

    assertSame(expectedResult, result);
  }

  @Test
  void getResult_Page1UrlIsSaved() {
    when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(new Result(RESULT_ID, TOTAL)));
    when(resultStore.getPageUrl(RESULT_ID, 0)).thenReturn(Optional.of(PAGE_0_URL));
    var page0 = new Bundle();
    page0.getLinkOrCreate("next").setUrl(PAGE_1_URL);
    when(downloadService.fetchPage(PAGE_0_URL)).thenReturn(page0);
    when(mappingService.map(page0)).thenReturn(new QueryResult());

    controller.getResult(RESULT_ID, 0);

    verify(resultStore).savePageUrl(RESULT_ID, 1, PAGE_1_URL);
  }

  @Test
  void getResult_Page1IsReturned() {
    when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(new Result(RESULT_ID, TOTAL)));
    when(resultStore.getPageUrl(RESULT_ID, 1)).thenReturn(Optional.of(PAGE_1_URL));
    var page1 = new Bundle();
    when(downloadService.fetchPage(PAGE_1_URL)).thenReturn(page1);
    var expectedResult = new QueryResult();
    when(mappingService.map(page1)).thenReturn(expectedResult);

    var result = controller.getResult(RESULT_ID, 1);

    assertSame(expectedResult, result);
  }

  @Test
  void getResult_Page2IsReturned() {
    when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(new Result(RESULT_ID, TOTAL)));
    //noinspection unchecked
    when(resultStore.getPageUrl(RESULT_ID, 2)).thenReturn(Optional.empty(),
        Optional.of(PAGE_2_URL));
    when(resultStore.getMaxPageNum(RESULT_ID)).thenReturn(Optional.of(1));
    when(resultStore.getPageUrl(RESULT_ID, 1)).thenReturn(Optional.of(PAGE_1_URL));
    var page1 = new Bundle();
    page1.getLinkOrCreate("next").setUrl(PAGE_2_URL);
    when(downloadService.fetchPage(PAGE_1_URL)).thenReturn(page1);
    var page2 = new Bundle();
    when(downloadService.fetchPage(PAGE_2_URL)).thenReturn(page2);
    var expectedResult = new QueryResult();
    when(mappingService.map(page2)).thenReturn(expectedResult);

    var result = controller.getResult(RESULT_ID, 2);

    assertSame(expectedResult, result);
  }

  @Test
  void getResult_NotFound() {
    when(resultStore.get(RESULT_ID)).thenReturn(Optional.empty());

    var exception = assertThrows(RequestNotFoundException.class,
        () -> controller.getResult(RESULT_ID, 0));

    assertEquals(RESULT_ID, exception.getId());
  }

  @Test
  void getResult_MissingPageUrl() {
    when(resultStore.get(RESULT_ID)).thenReturn(Optional.of(new Result(RESULT_ID, TOTAL)));
    when(resultStore.getPageUrl(RESULT_ID, 1)).thenReturn(Optional.empty());
    when(resultStore.getMaxPageNum(RESULT_ID)).thenReturn(Optional.of(0));
    when(resultStore.getPageUrl(RESULT_ID, 0)).thenReturn(Optional.of(PAGE_0_URL));
    when(downloadService.fetchPage(PAGE_0_URL)).thenReturn(new Bundle());

    var exception = assertThrows(MissingPageUrlException.class,
        () -> controller.getResult(RESULT_ID, 1));

    assertEquals(RESULT_ID, exception.getId());
    assertEquals(1, exception.getPageNum());
  }
}
