package de.samply.store.adapter.fhir.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
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
              .addCondition(new ConditionContainer(condition));
        }
        case Observation -> {
          Observation observation = (Observation) resource;

          var code = findFirstLonicCode(observation.getCode());
          if (code.isPresent()) {
            switch (code.get()) {
              case "75186-7" -> {
                resourceContainer.getPatientContainer(observation.getSubject().getReference()).setVitalState(observation);
              }
              // case "21907-1" -> resourceCondition.addMetastasis(observation);
              // case "59847-4" -> resourceCondition.addHistology(observation);
              // case "21908-9", "21902-2" -> resourceCondition.addTNM(observation);
            }
          }
        }

      }
      resourceContainer.resources.put(
          resource.getResourceType() + "/" + resource.getIdElement().getIdPart(),
          resource);
    }

    return resourceContainer;

/*
      (entry.getResource().getResourceType().equals(ResourceType.Patient)) {
      }



        for (Bundle.BundleEntryComponent entry2 : bundle.getEntry()) {
          if (entry2.getResource().getResourceType().equals(ResourceType.Specimen)) {
            var specimen = (Specimen) entry.getResource();
            if (specimen.getSubject().getId().equals(patient.getId())) {
              paR.addSpecimen(specimen);
            }
          } else if (entry2.getResource().getResourceType().equals(ResourceType.Condition)) {

            if (condition.getSubject().getId().equals(patient.getId())) {
                List<String> evidenceIds = condition.getEvidence().stream().map(conditionEvidenceComponent -> conditionEvidenceComponent.getId());
                List<>
              for (Bundle.BundleEntryComponent entry3 : bundle.getEntry()) {
                if (entry3.getResource().equals(ResourceType.Observation)) {
                  var observation = (Observation) entry3.getResource();


                } else if (entry3.getResource().equals(ResourceType.Procedure)) {
                  Procedure procedure = (Procedure) entry3.getResource();
                  if (procedure.getReasonReferenceFirstRep().getId().equals(condition.getId())) {
                    resourceCondition.addProcedure(procedure);
                  }
                } else if (entry3.getResource().equals(ResourceType.MedicationStatement)) {
                  MedicationStatement medicationStatement = (MedicationStatement) entry3.getResource();

                  if (medicationStatement.getReasonReferenceFirstRep().getId()
                      .equals(condition.getId())) {
                    resourceCondition.addMedicationStatement(medicationStatement);
                  }
                } else if (entry3.getResource().equals(ResourceType.ClinicalImpression)) {
                  ClinicalImpression clinicalImpression = (ClinicalImpression) entry3.getResource();
                  if(clinicalImpression.getProblemFirstRep().getId().equals(condition.getId())) {
                    resourceCondition.addClinicalImpression(clinicalImpression);
                  }
                }
              }
            }
          }
        }
        patientResources.add(paR);
      }
    }
          case ClinicalImpression:
        var clinicalImpression = (ClinicalImpression) entry.getResource();
        result.get(clinicalImpression.getSubject().getReferenceElement().getIdPart())
            .add(clinicalImpression);
        break;
      case MedicationStatement:
        var medicationStatement = (MedicationStatement) entry.getResource();
        result.get(medicationStatement.getSubject().getReferenceElement().getIdPart())
            .add(medicationStatement);
    */

  }

  @NotNull
  private PatientContainer getPatientContainer(String reference) {
    return patientContainers.computeIfAbsent(reference, k -> new PatientContainer());
  }

}
