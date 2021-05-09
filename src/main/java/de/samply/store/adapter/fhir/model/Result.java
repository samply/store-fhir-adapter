package de.samply.store.adapter.fhir.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A result of a FHIR query.
 *
 * <p>The result contains the URL's of all pages accessed starting with the first page and the
 * total number of patients.
 *
 * <p>Results are immutable.
 *
 * @author Alexander Kiel
 */
public final class Result {

  private final String id;
  private final Map<Integer, String> pageUrls;
  private final int total;

  private Result(String id, Map<Integer, String> pageUrls, int total) {
    this.id = id;
    this.pageUrls = pageUrls;
    this.total = total;
  }

  /**
   * Returns a result.
   *
   * @param id           the identifier of the result
   * @param firstPageUrl the URL of the first page
   * @param total        the total number of patients
   * @return a result
   */
  public static Result of(String id, String firstPageUrl, int total) {
    var pageUrls = Map.of(0, Objects.requireNonNull(firstPageUrl));
    return new Result(Objects.requireNonNull(id), pageUrls, total);
  }

  /**
   * Returns the identifier of this result.
   *
   * @return the identifier of this result.
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the URL of the page with {@code pageNum}.
   *
   * @param pageNum the number of the page
   * @return the URL or {@link Optional#empty()} iff the URl is not known
   */
  public Optional<String> getPageUrl(int pageNum) {
    return Optional.ofNullable(pageUrls.get(pageNum));
  }

  /**
   * Returns the total number of patients.
   *
   * @return the total number of patients
   */
  public int getTotal() {
    return total;
  }

  /**
   * Returns a new result with the {@code pageUrl} of page with {@code pageNum} added to the known
   * set of page URL's.
   *
   * @param pageNum the number of the page
   * @param pageUrl the URL of the page
   * @return a new result
   */
  public Result withPageUrl(int pageNum, String pageUrl) {
    var pageUrls = new HashMap<>(this.pageUrls);
    pageUrls.put(pageNum, pageUrl);
    return new Result(id, pageUrls, total);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Result result = (Result) o;
    return total == result.total && id.equals(result.id) && pageUrls.equals(result.pageUrls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pageUrls, total);
  }
}
