package de.samply.store.adapter.fhir.model;

import java.util.Collection;
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
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;
import org.jetbrains.annotations.NotNull;

public class ResourceContainer {

  public Collection<PatientContainer> getPatientContainers() {
    return patientContainers.values();
  }

  private final Map<String, PatientContainer> patientContainers = new HashMap<>();

  public Map<String, Resource> getResources() {
    return resources;
  }

  private final Map<String, Resource> resources = new HashMap<>();

  private ResourceContainer() {

  }

  private static Optional<String> findFirstLonicCode(CodeableConcept concept) {
    return concept.getCoding().stream()
        .filter(c -> "http://loinc.org".equals(c.getSystem()))
        .map(Coding::getCode)
        .findFirst();
  }

  /**
   * Partitions the resources in {@code bundle} by patient.
   *
   * @param bundle the bundle to partition
   * @return a list of lists of all resources of one patient. The patient is the first element in
   * each inner lists.
   */
  public static ResourceContainer fromBundle(Bundle bundle) {
    var resourceContainer = new ResourceContainer();

    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      Resource resource = entry.getResource();
      switch (resource.getResourceType()) {
        case Patient -> {
          Patient patient = (Patient) resource;
          resourceContainer.getPatientContainer("Patient/" + patient.getIdElement().getIdPart())
              .setPatient(patient);
        }
        case Condition -> {
          Condition condition = (Condition) resource;
          resourceContainer.getPatientContainer(condition.getSubject().getReference())
              .getConditionContainer("Condition/" + condition.getId()).setCondition(condition);
        }
        case Observation -> {
          Observation observation = (Observation) resource;

          var code = findFirstLonicCode(observation.getCode());
            if (code.equals(Optional.of("75186-7"))) {
              resourceContainer.getPatientContainer(observation.getSubject().getReference())
                  .setVitalState(observation);
            }
        }
        case Specimen -> {
          Specimen specimen = (Specimen) resource;
          resourceContainer.getPatientContainer(specimen.getSubject().getReference())
              .addSpecimen(specimen);
        }
        case ClinicalImpression -> {
          var clinicalImpression = (ClinicalImpression) entry.getResource();
          resourceContainer.getPatientContainer(clinicalImpression.getSubject().getReference())
              .getConditionContainer(clinicalImpression.getProblemFirstRep().getReference())
              .getClinicalImpressionContainer("ClinicalImpression" + clinicalImpression.getId())
              .setClinicalImpression(clinicalImpression);
        }
      }
      resourceContainer.resources.put(
          resource.getResourceType() + "/" + resource.getIdElement().getIdPart(),
          resource);
    }
    return resourceContainer;
  }

  @NotNull
  private PatientContainer getPatientContainer(String reference) {
    return patientContainers.computeIfAbsent(reference, k -> new PatientContainer());
  }

}
