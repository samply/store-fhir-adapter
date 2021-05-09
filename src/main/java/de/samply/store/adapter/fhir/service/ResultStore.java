package de.samply.store.adapter.fhir.service;

import de.samply.store.adapter.fhir.model.Result;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * An in-memory store for results.
 *
 * <p>Currently, it is sufficient to store results in-memory because they are only needed during one
 * run of pages.
 *
 * @author Alexander Kiel
 */
@Component
public class ResultStore {

  private final Map<String, Result> results;

  public ResultStore() {
    results = new ConcurrentHashMap<>();
  }

  public Optional<Result> get(String id) {
    return Optional.ofNullable(results.get(id));
  }

  public void save(Result result) {
    results.put(result.getId(), result);
  }
}
