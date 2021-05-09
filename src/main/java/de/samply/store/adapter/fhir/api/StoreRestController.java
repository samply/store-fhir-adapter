package de.samply.store.adapter.fhir.api;

import de.samply.share.model.ccp.QueryResult;
import de.samply.share.model.osse.QueryResultStatistic;
import de.samply.store.adapter.fhir.model.Result;
import de.samply.store.adapter.fhir.service.FhirDownloadService;
import de.samply.store.adapter.fhir.service.MappingService;
import de.samply.store.adapter.fhir.service.ResultStore;
import java.net.URI;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The complete REST API of the Store-FHIR-Adapter.
 *
 * @author Alexander Kiel
 */
@RestController
public class StoreRestController {

  private static final Logger logger = LoggerFactory.getLogger(FhirDownloadService.class);

  private final FhirDownloadService downloadService;
  private final MappingService mappingService;
  private final ResultStore resultStore;
  private final int pageSize;

  /**
   * Creates a new {@code StoreRestController}.
   *
   * @param downloadService the FHIR backend
   * @param mappingService the mapping service between FHIR and {@link QueryResult}
   * @param resultStore the result store
   * @param pageSize the number of patients per page
   */
  public StoreRestController(FhirDownloadService downloadService, MappingService mappingService,
      ResultStore resultStore, @Value("${app.store.page-size}") int pageSize) {
    this.downloadService = Objects.requireNonNull(downloadService);
    this.mappingService = Objects.requireNonNull(mappingService);
    this.resultStore = Objects.requireNonNull(resultStore);
    this.pageSize = pageSize;
  }

  /**
   * Runs {@code query} against the FHIR server, saves the result and returns a 201 created response
   * with the Location header pointing to the request created.
   *
   * @param query the query to execute
   * @return a 201 created response
   */
  @PostMapping("/requests")
  public ResponseEntity<?> createRequest(@RequestBody String query) {
    logger.debug("create request");

    var result = downloadService.runQuery();
    resultStore.save(result);
    return ResponseEntity.created(URI.create("/requests/" + result.getId())).body(null);
  }

  /**
   * Tries to retrieve the result with {@code id} and returns the {@code QueryResultStatistic} with
   * {@code numberOfPages} and {@code totalSize}.
   *
   * @param id the identifier of the result
   * @return the {@code QueryResultStatistic} according of the found result
   * @throws RequestNotFoundException if the result was not found
   */
  @GetMapping("/requests/{id}/stats")
  public QueryResultStatistic getStats(@PathVariable("id") String id) {
    logger.debug("request stats id={}", id);

    var result = resultStore.get(id);
    if (result.isPresent()) {
      var stats = new QueryResultStatistic();
      stats.setNumberOfPages((int) Math.ceil((double) result.get().getTotal() / pageSize));
      stats.setTotalSize(result.get().getTotal());
      stats.setRequestId(id);
      return stats;
    } else {
      throw new RequestNotFoundException(id);
    }
  }

  /**
   * Tries to retrieve the result with {@code id} and {@code pageNum} and returns the corresponding
   * {@code QueryResult}.
   *
   * @param id the identifier of the result
   * @return the {@code QueryResult} according of the found result
   * @throws RequestNotFoundException if the result was not found
   * @throws MissingPageUrlException page with {@code pageNum} was not found
   */
  @GetMapping("/requests/{id}/result")
  public QueryResult getResult(@PathVariable("id") String id,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNum) {
    logger.debug("request result id={}, pageNum={}", id, pageNum);

    var result = resultStore.get(id);
    if (result.isPresent()) {
      var pageUrl = result.get().getPageUrl(pageNum);
      if (pageUrl.isPresent()) {
        var bundle = downloadService.fetchPage(pageUrl.get());
        appendResultPageUrl(result.get(), pageNum, bundle);
        var queryResult = mappingService.map(bundle);
        queryResult.setId(id);
        return queryResult;
      } else {
        throw new MissingPageUrlException(id, pageNum);
      }
    } else {
      throw new RequestNotFoundException(id);
    }
  }

  private void appendResultPageUrl(Result result, int pageNum, Bundle bundle) {
    var url = bundle.getLinkOrCreate("next").getUrl();
    if (url != null) {
      var newResult = result.withPageUrl(pageNum + 1, url);
      resultStore.save(newResult);
    }
  }
}
