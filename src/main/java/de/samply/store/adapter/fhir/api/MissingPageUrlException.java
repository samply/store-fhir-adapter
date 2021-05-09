package de.samply.store.adapter.fhir.api;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.server.ResponseStatusException;

/**
 * An exception indicating that a URL of a page was not found in a result.
 *
 * <p>Will produce a 404 Not Found response.
 *
 * @author Alexander Kiel
 */
public final class MissingPageUrlException extends ResponseStatusException {

  private final String id;
  private final int pageNum;

  /**
   * Creates a new {@code MissingPageUrlException}.
   *
   * @param id the identifier of the request
   * @param pageNum the number of the page that was not found
   */
  public MissingPageUrlException(String id, int pageNum) {
    super(NOT_FOUND, String
        .format("Unable to find the result page of the request with id `%s` and page number `%d`.",
            id, pageNum));
    this.id = id;
    this.pageNum = pageNum;
  }

  /**
   * Returns the identifier of the request.
   *
   * @return the identifier of the request
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the number of the page that was not found.
   *
   * @return the number of the page that was not found
   */
  public int getPageNum() {
    return pageNum;
  }
}
