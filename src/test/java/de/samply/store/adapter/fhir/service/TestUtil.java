package de.samply.store.adapter.fhir.service;

import de.samply.share.model.ccp.Container;
import java.util.Objects;
import java.util.Optional;


public class TestUtil {

  public static Optional<String> findAttributeValue(Container container, String urn) {
    Objects.requireNonNull(container);
    Objects.requireNonNull(urn);
    return container.getAttribute().stream().filter(a -> urn.equals(a.getMdrKey())).findFirst()
        .map(a -> a.getValue().getValue());
  }

  public static Optional<String> convertCsvValue(String value) {
    return Objects.isNull(value) ? Optional.empty() : Optional.of(value);
  }
}
