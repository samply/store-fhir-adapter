package de.samply.store.adapter.fhir.model;

import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;

/**
 * The patient node of the tree of FHIR Resources modeled after the hierarchy of the MDS data set.
 */
public record PatientNode(Patient patient, Optional<Observation> vitalState,
                          List<ConditionNode> conditions, List<Specimen> specimens) {

  public PatientNode(Patient patient) {
    this(patient, Optional.empty(), List.of(), List.of());
  }

  public PatientNode(Patient patient, Observation vitalState) {
    this(patient, Optional.of(vitalState), List.of(), List.of());
  }

  public PatientNode(Patient patient, Condition condition) {
    this(patient, Optional.empty(), List.of(new ConditionNode(patient, condition)), List.of());
  }
}
