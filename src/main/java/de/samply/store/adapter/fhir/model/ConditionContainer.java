package de.samply.store.adapter.fhir.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.jetbrains.annotations.NotNull;

public class ConditionContainer {

  private Condition condition;

  private final Map<String, ClinicalImpressionContainer> clinicalImpressionContainers = new HashMap<>();

  public Condition getCondition() {
    return condition;
  }

  @NotNull
  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  @NotNull
  public ClinicalImpressionContainer getClinicalImpressionContainer(String reference) {
    return clinicalImpressionContainers.computeIfAbsent(reference, k -> new ClinicalImpressionContainer());
  }

  public Collection<ClinicalImpressionContainer> getClinicalImpressionContainers() {
    return clinicalImpressionContainers.values();
  }
}
