package de.samply.store.adapter.fhir.service;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import ca.uhn.fhir.context.FhirContext;
import de.samply.share.model.ccp.QueryResult;
import de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping;
import de.samply.store.adapter.fhir.service.mapping.HistologyMapping;
import de.samply.store.adapter.fhir.service.mapping.MetastasisMapping;
import de.samply.store.adapter.fhir.service.mapping.ProgressMapping;
import de.samply.store.adapter.fhir.service.mapping.RadiationTherapyMapping;
import de.samply.store.adapter.fhir.service.mapping.SampleMapping;
import de.samply.store.adapter.fhir.service.mapping.SurgeryMapping;
import de.samply.store.adapter.fhir.service.mapping.SystemTherapyMapping;
import de.samply.store.adapter.fhir.service.mapping.TNMMapping;
import de.samply.store.adapter.fhir.service.mapping.TumorMapping;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;
import org.springframework.stereotype.Service;

/**
 * A service for the mapping of FHIR resources to {@link QueryResult QueryResults}.
 *
 * @author Alexander Kiel
 */
@Service
public class MappingService {

  public static final String ICD_10_GM = "http://fhir.de/CodeSystem/dimdi/icd-10-gm";

  private final FhirContext fhirContext;
  private final DiagnosisMapping diagnosisMapping;
  private final SampleMapping sampleMapping;
  private final MetastasisMapping metastasisMapping;
  private final SurgeryMapping surgeryMapping;
  private final TNMMapping tnmMapping;
  private final TumorMapping tumorMapping;
  private final RadiationTherapyMapping radiationTherapyMapping;
  private final SystemTherapyMapping systemTherapyMapping;

  public MappingService(FhirContext fhirContext, DiagnosisMapping diagnosisMapping,
      SampleMapping sampleMapping,
      MetastasisMapping metastasisMapping,
      SurgeryMapping surgeryMapping,
      TNMMapping tnmMapping, TumorMapping tumorMapping,
      RadiationTherapyMapping radiationTherapyMapping,
      SystemTherapyMapping systemTherapyMapping) {
    this.fhirContext = fhirContext;
    this.diagnosisMapping = diagnosisMapping;
    this.sampleMapping = sampleMapping;
    this.metastasisMapping = metastasisMapping;
    this.surgeryMapping = surgeryMapping;
    this.tnmMapping = tnmMapping;
    this.tumorMapping = tumorMapping;
    this.radiationTherapyMapping = radiationTherapyMapping;
    this.systemTherapyMapping = systemTherapyMapping;
  }

  /**
   * Partitions the resources in {@code bundle} by patient.
   *
   * @param bundle the bundle to partition
   * @return a list of lists of all resources of one patient. The patient is the first element in
   * each inner lists.
   */
  private static Collection<List<Resource>> partitionByPatient(Bundle bundle) {
    var result = new HashMap<String, List<Resource>>();
    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      switch (entry.getResource().getResourceType()) {
        case Patient:
          var patientResources = new ArrayList<Resource>();
          var patient = entry.getResource();
          patientResources.add(patient);
          result.put(patient.getIdElement().getIdPart(), patientResources);
          break;
        case Observation:
          var observation = (Observation) entry.getResource();
          result.get(observation.getSubject().getReferenceElement().getIdPart()).add(observation);
          break;
        case Condition:
          var condition = (Condition) entry.getResource();
          result.get(condition.getSubject().getReferenceElement().getIdPart()).add(condition);
          break;
        case Specimen:
          var specimen = (Specimen) entry.getResource();
          result.get(specimen.getSubject().getReferenceElement().getIdPart()).add(specimen);
          break;
        case ClinicalImpression:
          var clinicalImpression = (ClinicalImpression) entry.getResource();
          result.get(clinicalImpression.getSubject().getReferenceElement().getIdPart())
              .add(clinicalImpression);
          break;
        case MedicationStatement:
          var medicationStatement = (MedicationStatement) entry.getResource();
          result.get(medicationStatement.getSubject().getReferenceElement().getIdPart())
              .add(medicationStatement);
          break;
        case Procedure:
          var procedure = (Procedure) entry.getResource();
          result.get(procedure.getSubject().getReferenceElement().getIdPart()).add(procedure);
          break;
        default:
      }
    }
    return result.values();
  }

  /**
   * Map's a {@code Bundle} of a result page to a {@code QueryResult}.
   *
   * @param bundle the bundle to map
   * @return the mapped {@code QueryResult}
   */
  public QueryResult map(Bundle bundle) {
    var result = new QueryResult();

    for (List<Resource> resources : partitionByPatient(bundle)) {
      FhirPathR4 fhirPathR4 = new FhirPathR4(fhirContext, new MyIEvaluationContext(resources));
      var patient = (Patient) resources.get(0);
      var patientBuilder = new PatientBuilder(fhirPathR4, patient);

      // gender
      var gender = patient.getGender();
      if (gender != null) {
        patientBuilder.addAttribute("Patient.gender", Enumeration.class, "urn:dktk:dataelement:1:3",
            gen -> mapGenderValue(gen.getValueAsString()));
      }

      patientBuilder.addAttributeOptional("Patient.birthDate", DateType.class,
          "urn:dktk:dataelement:26:4", DATE_STRING);

      HistologyMapping histologyMapping = new HistologyMapping(fhirPathR4);
      ProgressMapping progressMapping = new ProgressMapping(fhirPathR4);

      // other resources (skip Patient resource)
      resources.stream().skip(1).forEach(resource -> {
        switch (resource.getResourceType()) {
          case Observation:
            var observation = (Observation) resource;
            var code = findFirstLonicCode(observation.getCode());
            if (code.isPresent()) {
              switch (code.get()) {
                case "75186-7" -> {
                  patientBuilder.addAttribute(observation,
                      "Observation.value.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS').code",
                      CodeType.class, "urn:dktk:dataelement:53:3", PrimitiveType::getValue);
                  patientBuilder.addAttributeOptional(observation,
                      "Observation.effective",
                      DateTimeType.class, "urn:dktk:dataelement:48:3", DATE_STRING);
                }
                case "21907-1" -> patientBuilder.addContainer(metastasisMapping.map(observation));
                case "59847-4" -> patientBuilder.addContainer(histologyMapping.map(observation));
                case "21908-9", "21902-2" -> patientBuilder.addContainer(
                    tnmMapping.map(observation));
              }
            }
            break;

          case Condition:
            var condition = (Condition) resource;
            patientBuilder.addContainer(diagnosisMapping.map(condition, patient));
            patientBuilder.addContainer(tumorMapping.map(condition));
            break;

          case ClinicalImpression:
            var impression = (ClinicalImpression) resource;
            patientBuilder.addContainer(progressMapping.map(impression));
            break;
          case Procedure:
            var procedure = (Procedure) resource;

            patientBuilder.addContainer(surgeryMapping.map(procedure));
            patientBuilder.addContainer(radiationTherapyMapping.map(procedure));
            break;
          case Specimen:
            var speci = (Specimen) resource;
            patientBuilder.addContainer(sampleMapping.map(speci));
            break;
          case MedicationStatement:
            patientBuilder.addContainer(systemTherapyMapping.map((MedicationStatement) resource));
            break;
          default:
        }
      });

      result.getPatient().add(patientBuilder.build());
    }
    return result;
  }

  private Optional<String> findFirstLonicCode(CodeableConcept concept) {
    return concept.getCoding().stream()
        .filter(c -> "http://loinc.org".equals(c.getSystem()))
        .map(Coding::getCode)
        .findFirst();
  }

  private String mapGenderValue(String gender) {
    return switch (gender) {
      case "male" -> "M";
      case "female" -> "W";
      case "other" -> "S";
      default -> "U";
    };
  }


}