package de.samply.store.adapter.fhir.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.samply.store.adapter.fhir.model.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Alexander Kiel
 */
@ExtendWith(MockitoExtension.class)
class ResultStoreTest {

  public static final String RESULT_ID = "id-194129";
  public static final Result RESULT = Result.of(RESULT_ID, "url-foo", 0);

  @InjectMocks
  private ResultStore store;

  @Test
  void get() {
    var result = store.get(RESULT_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  void get_withResult() {
    store.save(RESULT);

    var result = store.get(RESULT_ID);

    assertTrue(result.isPresent());
    assertEquals(RESULT, result.get());
  }

  @Test
  void save() {
  }
}
