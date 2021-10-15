package de.samply.store.adapter.fhir.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;

class PatientNodeBuilder {

  private Patient patient;
  private Observation vitalState;
  private final Map<String, ConditionNodeBuilder> conditionNodeBuilders = new HashMap<>();
  private final List<Specimen> specimens = new ArrayList<>();

  PatientNodeBuilder() {
  }

  Optional<Patient> getPatient() {
    return Optional.ofNullable(patient);
  }

  void setPatient(Patient patient) {
    this.patient = Objects.requireNonNull(patient);
  }

  void setVitalState(Observation vitalState) {
    this.vitalState = Objects.requireNonNull(vitalState);
  }

  ConditionNodeBuilder getConditionNodeBuilder(String reference) {
    return conditionNodeBuilders.computeIfAbsent(Objects.requireNonNull(reference),
        k -> new ConditionNodeBuilder(this));
  }

  void addSpecimen(Specimen specimen) {
    specimens.add(Objects.requireNonNull(specimen));
  }

  Stream<PatientNode> build() {
    return Stream.ofNullable(patient).map(p -> new PatientNode(p, Optional.ofNullable(vitalState),
        conditionNodeBuilders.values().stream().flatMap(ConditionNodeBuilder::build).toList(),
        List.copyOf(specimens)));
  }
}
