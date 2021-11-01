package de.samply.store.adapter.fhir.service;

import de.samply.share.model.ccp.Container;
import java.util.Objects;
import java.util.Optional;

public class TestUtil {

  public static Optional<String> findAttrValue(Container container, String urnSuffix) {
    Objects.requireNonNull(container);
    Objects.requireNonNull(urnSuffix);
    return container.getAttribute().stream()
        .filter(a -> ("urn:dktk:dataelement:" + urnSuffix).equals(a.getMdrKey()))
        .map(a -> a.getValue().getValue())
        .findFirst();
  }
}
