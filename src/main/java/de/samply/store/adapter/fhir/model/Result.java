package de.samply.store.adapter.fhir.model;

import java.util.Objects;

/**
 * A result of a FHIR query.
 *
 * <p>Results are immutable.
 *
 * @author Alexander Kiel
 */
public final class Result {

  private final String id;
  private final int total;

  private Result(String id, int total) {
    this.id = id;
    this.total = total;
  }

  /**
   * Returns a result.
   *
   * @param id    the identifier of the result
   * @param total the total number of patients
   * @return a result
   */
  public static Result of(String id, int total) {
    return new Result(Objects.requireNonNull(id), total);
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
   * Returns the total number of patients.
   *
   * @return the total number of patients
   */
  public int getTotal() {
    return total;
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
    return total == result.total && id.equals(result.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, total);
  }

  @Override
  public String toString() {
    return "Result{id='" + id + '\'' + ", total=" + total + '}';
  }
}
