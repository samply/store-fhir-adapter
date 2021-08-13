package de.samply.store.adapter.fhir.api;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import de.samply.share.model.ccp.QueryResult;
import de.samply.share.model.common.QueryResultStatistic;
import de.samply.store.adapter.fhir.service.BundleWithoutSelfUrlException;
import de.samply.store.adapter.fhir.service.FhirDownloadService;
import de.samply.store.adapter.fhir.service.MappingService;
import de.samply.store.adapter.fhir.service.ResultStore;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The complete REST API of the Store-FHIR-Adapter.
 *
 * @author Alexander Kiel
 */
@RestController
@RequestMapping("/rest")
public class StoreRestController {

  private static final Logger logger = LoggerFactory.getLogger(StoreRestController.class);

  private final FhirDownloadService downloadService;
  private final MappingService mappingService;
  private final ResultStore resultStore;
  private final int pageSize;
  private final String version;
  private final String baseUrl;

  /**
   * Creates a new {@code StoreRestController}.
   *
   * @param downloadService the FHIR backend
   * @param mappingService  the mapping service between FHIR and {@link QueryResult}
   * @param resultStore     the result store
   * @param pageSize        the number of patients per page
   * @param version         the application version
   */
  public StoreRestController(FhirDownloadService downloadService,  MappingService mappingService,
      ResultStore resultStore, @Value("${app.store.page-size}") int pageSize,
      @Value("${app.version}") String version,
      @Value("${app.base-url}") String baseUrl) {
    this.downloadService = Objects.requireNonNull(downloadService);
    this.mappingService = Objects.requireNonNull(mappingService);
    this.resultStore = Objects.requireNonNull(resultStore);
    this.pageSize = pageSize;
    this.version = version;
    this.baseUrl = baseUrl;
  }

  /**
   * Returns application information like the version.
   *
   * @return application information
   */
  @GetMapping(value = "/info", produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getInfo() {
    return ResponseEntity.ok()
        .header("version", version)
        .body("{}");
  }

  /**
   * Runs {@code query} against the FHIR server, saves the result and returns a 201 created response
   * with the Location header pointing to the request created.
   *
   * @param query the query to execute
   * @return a 201 created response
   */
  @PostMapping("/teiler/requests")
  public ResponseEntity<?> createRequest(
      @RequestParam(name = "statisticsOnly", required = false, defaultValue = "false")
          boolean statisticsOnly,
      @RequestBody String query) {
    logger.debug("create request statisticsOnly = {}", statisticsOnly);

    var bundle = downloadService.runQuery();
    try {
      var result = resultStore.create(bundle);
      return ResponseEntity.created(createRequestUrl(result)).body(null);
    } catch (BundleWithoutSelfUrlException e) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR)
          .body("The bundle returned by the FHIR Server has no self link URL.");
    }
  }

  private URI createRequestUrl(de.samply.store.adapter.fhir.model.Result result) {
    return URI.create(baseUrl + "/rest/teiler/requests/" + result.getId());
  }

  /**
   * Tries to retrieve the result with {@code id} and returns the {@code QueryResultStatistic} with
   * {@code numberOfPages} and {@code totalSize}.
   *
   * @param id the identifier of the result
   * @return the {@code QueryResultStatistic} according of the found result
   * @throws RequestNotFoundException if the result was not found
   */
  @GetMapping(value = "/teiler/requests/{id}/stats", produces = APPLICATION_XML_VALUE)
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
   * @throws MissingPageUrlException  page with {@code pageNum} was not found
   */
  @GetMapping(value = "/teiler/requests/{id}/result", produces = APPLICATION_XML_VALUE)
  public QueryResult getResult(@PathVariable("id") String id,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNum) {
    logger.debug("request result id={}, pageNum={}", id, pageNum);

    if (resultStore.get(id).isPresent()) {
      var queryResult = tryLoadPage(id, pageNum)
          .map(mappingService::map)
          .orElseThrow(() -> new MissingPageUrlException(id, pageNum));
      queryResult.setId(id);
      return queryResult;
    } else {
      throw new RequestNotFoundException(id);
    }
  }

  private Optional<Bundle> tryLoadPage(String resultId, int pageNum) {
    return fetchPage(resultId, pageNum)
        .or(() -> resultStore.getMaxPageNum(resultId).flatMap(maxPageNum -> {
          Optional<Bundle> bundle = Optional.empty();
          while (maxPageNum <= pageNum) {
            bundle = fetchPage(resultId, maxPageNum);
            assert bundle.isPresent();
            var url = bundle.get().getLinkOrCreate("next").getUrl();
            if (url != null) {
              maxPageNum++;
            } else {
              break;
            }
          }
          return bundle;
        }));
  }

  private Optional<Bundle> fetchPage(String resultId, int pageNum) {
    return resultStore.getPageUrl(resultId, pageNum)
        .map(downloadService::fetchPage)
        .map(bundle -> {
          var url = bundle.getLinkOrCreate("next").getUrl();
          if (url != null) {
            resultStore.savePageUrl(resultId, pageNum + 1, url);
          }
          return bundle;
        });
  }
}
