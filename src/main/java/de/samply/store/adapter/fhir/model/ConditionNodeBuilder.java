package de.samply.store.adapter.fhir.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Condition;

class ConditionNodeBuilder {

  private final PatientNodeBuilder patientNodeBuilder;
  private Condition condition;
  private final Map<String, ClinicalImpressionNodeBuilder> clinicalImpressionContainers =
      new HashMap<>();

  ConditionNodeBuilder(PatientNodeBuilder patientNodeBuilder) {
    this.patientNodeBuilder = patientNodeBuilder;
  }

  void setCondition(Condition condition) {
    this.condition = Objects.requireNonNull(condition);
  }

  ClinicalImpressionNodeBuilder getClinicalImpressionNodeBuilder(String reference) {
    return clinicalImpressionContainers.computeIfAbsent(Objects.requireNonNull(reference),
        k -> new ClinicalImpressionNodeBuilder());
  }

  Stream<ConditionNode> build() {
    return patientNodeBuilder.getPatient().stream().flatMap(patient -> Stream.ofNullable(condition)
        .map(condition -> new ConditionNode(patient, condition,
            clinicalImpressionContainers.values().stream()
                .flatMap(ClinicalImpressionNodeBuilder::build)
                .toList())));
  }
}
