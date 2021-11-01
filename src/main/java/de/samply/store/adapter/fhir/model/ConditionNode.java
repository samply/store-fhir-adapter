package de.samply.store.adapter.fhir.model;

import java.util.List;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;

/**
 * The condition node of the tree of FHIR Resources modeled after the hierarchy of the MDS data
 * set.
 */
public record ConditionNode(Patient patient, Condition condition,
                            List<Observation> histologies,
                            List<ClinicalImpressionNode> clinicalImpressions) {

  public ConditionNode(Patient patient, Condition condition) {
    this(patient, condition, List.of(), List.of());
  }
}
