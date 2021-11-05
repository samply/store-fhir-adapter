package de.samply.store.adapter.fhir.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;

/**
 * A builder for {@link RootNode}.
 */
public class RootNodeBuilder {

  private final Map<String, PatientNodeBuilder> patientNodeBuilders = new HashMap<>();
  private final Map<String, Resource> resources = new HashMap<>();

  private RootNodeBuilder() {
  }

  /**
   * Partitions the resources in {@code bundle} by patient.
   *
   * @param bundle the bundle to partition
   * @return a list of lists of all resources of one patient. The patient is the first element in
   *     each inner lists.
   */
  public static RootNode fromBundle(Bundle bundle) {
    var builder = new RootNodeBuilder();

    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      Resource resource = entry.getResource();
      switch (resource.getResourceType()) {
        case Patient -> {
          Patient patient = (Patient) resource;
          builder.getPatientNodeBuilder("Patient/" + patient.getIdElement().getIdPart())
              .setPatient(patient);
        }
        case Condition -> {
          Condition condition = (Condition) resource;
          builder.getPatientNodeBuilder(condition.getSubject().getReference())
              .getConditionNodeBuilder("Condition/" + condition.getIdElement().getIdPart())
              .setCondition(condition);
        }
        case Observation -> builder.addObservation((Observation) resource);
        case Procedure -> {
          Procedure procedure = (Procedure) resource;
          if (procedure.hasReasonReference()) {
            builder.getPatientNodeBuilder(procedure.getSubject().getReference())
                .getConditionNodeBuilder(procedure.getReasonReferenceFirstRep().getReference())
                .addProcedure(procedure);
          }
        }
        case Specimen -> {
          Specimen specimen = (Specimen) resource;
          builder.getPatientNodeBuilder(specimen.getSubject().getReference())
              .addSpecimen(specimen);
        }
        case ClinicalImpression -> {
          var clinicalImpression = (ClinicalImpression) entry.getResource();
          if (clinicalImpression.hasProblem()) {
            builder.getPatientNodeBuilder(clinicalImpression.getSubject().getReference())
                .getConditionNodeBuilder(clinicalImpression.getProblemFirstRep().getReference())
                .getClinicalImpressionNodeBuilder(
                    "ClinicalImpression/" + clinicalImpression.getIdElement().getIdPart())
                .setClinicalImpression(clinicalImpression);
          }
        }
        default -> {
        }
      }
      builder.resources.put(
          resource.getResourceType() + "/" + resource.getIdElement().getIdPart(),
          resource);
    }
    return builder.build();
  }

  private void addObservation(Observation observation) {
    findFirstLonicCode(observation.getCode())
        .ifPresent(code -> {
          switch (code) {
            case "75186-7":
              getPatientNodeBuilder(observation.getSubject().getReference())
                  .setVitalState(observation);
              break;
            case "59847-4":
              if (observation.hasFocus()) {
                getPatientNodeBuilder(observation.getSubject().getReference())
                    .getConditionNodeBuilder(observation.getFocusFirstRep().getReference())
                    .addHistology(observation);
              }
              break;
            default:
          }
        });
  }

  private RootNode build() {
    return new RootNode(
        patientNodeBuilders.values().stream().flatMap(PatientNodeBuilder::build).toList(),
        Map.copyOf(resources));
  }

  private PatientNodeBuilder getPatientNodeBuilder(String reference) {
    return patientNodeBuilders.computeIfAbsent(reference, k -> new PatientNodeBuilder());
  }

  private static Optional<String> findFirstLonicCode(CodeableConcept concept) {
    return concept.getCoding().stream()
        .filter(c -> "http://loinc.org".equals(c.getSystem()))
        .map(Coding::getCode)
        .findFirst();
  }
}
