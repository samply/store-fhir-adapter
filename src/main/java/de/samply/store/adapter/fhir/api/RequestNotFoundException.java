package de.samply.store.adapter.fhir.api;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.Objects;
import org.springframework.web.server.ResponseStatusException;

/**
 * An exception indicating that a request was not found.
 *
 * <p>Will produce a 404 Not Found response.
 */
public final class RequestNotFoundException extends ResponseStatusException {

  private final String id;

  /**
   * Creates a new {@code RequestNotFoundException}.
   *
   * @param id the identifier of the request that was not found
   */
  public RequestNotFoundException(String id) {
    super(NOT_FOUND,
        String.format("Unable to find the request with id `%s`.", Objects.requireNonNull(id)));
    this.id = id;
  }

  /**
   * Returns the identifier of the request that was not found.
   *
   * @return the identifier of the request that was not found
   */
  public String getId() {
    return id;
  }
}
