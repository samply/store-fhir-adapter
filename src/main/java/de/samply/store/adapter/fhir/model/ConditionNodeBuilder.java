package de.samply.store.adapter.fhir.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Procedure;

class ConditionNodeBuilder {

  private final PatientNodeBuilder patientNodeBuilder;
  private Condition condition;
  private final List<Observation> histologies = new ArrayList<>();
  private final List<Procedure> procedures = new ArrayList<>();
  private final Map<String, ClinicalImpressionNodeBuilder> clinicalImpressionContainers =
      new HashMap<>();

  ConditionNodeBuilder(PatientNodeBuilder patientNodeBuilder) {
    this.patientNodeBuilder = patientNodeBuilder;
  }

  void setCondition(Condition condition) {
    this.condition = Objects.requireNonNull(condition);
  }

  void addHistology(Observation histology) {
    histologies.add(histology);
  }

  void addProcedure(Procedure procedure) {
    procedures.add(Objects.requireNonNull(procedure));
  }

  ClinicalImpressionNodeBuilder getClinicalImpressionNodeBuilder(String reference) {
    return clinicalImpressionContainers.computeIfAbsent(Objects.requireNonNull(reference),
        k -> new ClinicalImpressionNodeBuilder());
  }

  Stream<ConditionNode> build() {
    return patientNodeBuilder.getPatient().stream().flatMap(patient -> Stream.ofNullable(condition)
        .map(c -> new ConditionNode(patient, c,
            List.copyOf(histologies),
            List.copyOf(procedures),
            clinicalImpressionContainers.values().stream()
                .flatMap(ClinicalImpressionNodeBuilder::build)
                .toList())));
  }
}
