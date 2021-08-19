package de.samply.store.adapter.fhir.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.hl7.fhir.r4.model.Condition;

public class ConditionContainer {

  private Condition condition;

  private final Map<String, ClinicalImpressionContainer> clinicalImpressionContainers = new HashMap<>();

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    Objects.requireNonNull(condition);
    this.condition = condition;
  }

  public ClinicalImpressionContainer getClinicalImpressionContainer(String reference) {
    Objects.requireNonNull(reference);
    return clinicalImpressionContainers.computeIfAbsent(reference,
        k -> new ClinicalImpressionContainer());
  }

  public Collection<ClinicalImpressionContainer> getClinicalImpressionContainers() {
    return clinicalImpressionContainers.values();
  }
}
