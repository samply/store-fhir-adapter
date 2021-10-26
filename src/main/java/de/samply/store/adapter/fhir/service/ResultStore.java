package de.samply.store.adapter.fhir.service;

import static java.util.Comparator.naturalOrder;

import de.samply.store.adapter.fhir.model.Result;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * An in-memory store for results.
 *
 * <p>Currently, it is sufficient to store results in-memory because they are only needed during
 * one run of pages.
 */
@Component
public class ResultStore {

  private static final Logger logger = LoggerFactory.getLogger(ResultStore.class);

  private final Supplier<String> resultIdSupplier;
  private final Map<String, InternalResult> results;

  public ResultStore(Supplier<String> resultIdSupplier) {
    this.resultIdSupplier = Objects.requireNonNull(resultIdSupplier);
    results = new ConcurrentHashMap<>();
  }

  public Optional<Result> get(String id) {
    return getInternal(id).map(r -> r.result);
  }

  private Optional<InternalResult> getInternal(String id) {
    return Optional.ofNullable(results.get(id));
  }

  /**
   * Creates a {@code Result}.
   *
   * @param bundle the bundle to extract the total, self and optional next link URL's.
   * @return the result
   * @throws BundleWithoutSelfUrlException if the bundle has no self link URL
   */
  public Result create(Bundle bundle) throws BundleWithoutSelfUrlException {
    var id = resultIdSupplier.get();
    logger.debug("create result id={}", id);
    var result = new Result(id, bundle.getTotal());
    results.put(id, new InternalResult(result, bundle));
    return result;
  }

  public Optional<String> getPageUrl(String resultId, int pageNum) {
    return getInternal(resultId).flatMap(r -> Optional.ofNullable(r.pageUrls.get(pageNum)));
  }

  public Optional<Integer> getMaxPageNum(String resultId) {
    return getInternal(resultId).flatMap(r -> r.pageUrls.keySet().stream().min(naturalOrder()));
  }

  public void savePageUrl(String resultId, int pageNum, String pageUrl) {
    getInternal(resultId).ifPresent(r -> r.pageUrls.putIfAbsent(pageNum, pageUrl));
  }

  private static final class InternalResult {

    private final Result result;
    private final Map<Integer, String> pageUrls;

    private InternalResult(Result result, Bundle bundle) throws BundleWithoutSelfUrlException {
      this.result = result;
      pageUrls = new ConcurrentHashMap<>();
      var selfUrl = bundle.getLinkOrCreate("self").getUrl();
      if (selfUrl == null) {
        throw new BundleWithoutSelfUrlException();
      }
      pageUrls.put(0, selfUrl);
      var nextUrl = bundle.getLinkOrCreate("next").getUrl();
      if (nextUrl != null) {
        pageUrls.put(1, nextUrl);
      }
    }
  }
}
