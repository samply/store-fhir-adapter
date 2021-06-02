package de.samply.store.adapter.fhir.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.samply.store.adapter.fhir.model.Result;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Alexander Kiel
 */
@ExtendWith(MockitoExtension.class)
class ResultStoreTest {

  public static final String RESULT_ID = "id-194129";
  public static final Result RESULT = Result.of(RESULT_ID, 0);

  private ResultStore store;

  @BeforeEach
  void setUp() {
    store = new ResultStore(() -> RESULT_ID);
  }

  @Test
  void get() {
    var result = store.get(RESULT_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  void get_withResult() throws Exception {
    var bundle = new Bundle();
    bundle.getLinkOrCreate("self").setUrl("url-150503");
    store.create(bundle);

    var result = store.get(RESULT_ID);

    assertTrue(result.isPresent());
    assertEquals(RESULT, result.get());
  }

  @Test
  void save() {
  }
}
