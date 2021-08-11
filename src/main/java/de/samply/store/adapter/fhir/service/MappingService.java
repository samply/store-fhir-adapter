package de.samply.store.adapter.fhir.service;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import ca.uhn.fhir.context.FhirContext;
import de.samply.share.model.ccp.QueryResult;
import de.samply.store.adapter.fhir.model.ConditionContainer;
import de.samply.store.adapter.fhir.model.PatientContainer;
import de.samply.store.adapter.fhir.model.ResourceContainer;
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
import java.util.stream.Collectors;
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
import org.hl7.fhir.r4.model.ResourceType;
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
   * Map's a {@code Bundle} of a result page to a {@code QueryResult}.
   *
   * @param bundle the bundle to map
   * @return the mapped {@code QueryResult}
   */
  public QueryResult map(Bundle bundle) {
    var result = new QueryResult();
    var resourceContainer = ResourceContainer.fromBundle(bundle);
    FhirPathR4 fhirPathR4 = new FhirPathR4(fhirContext,
        new MyIEvaluationContext(resourceContainer.getResources()));

    for (PatientContainer patientContainer : resourceContainer.getPatientContainers()) {
      var patient = patientContainer.getPatient();
      var patientBuilder = new PatientBuilder(fhirPathR4, patient);

      // gender
      var gender = patient.getGender();
      if (gender != null) {
        patientBuilder.addAttribute("Patient.gender", Enumeration.class, "urn:dktk:dataelement:1:3",
            gen -> mapGenderValue(gen.getValueAsString()));
      }

      patientBuilder.addAttributeOptional("Patient.birthDate", DateType.class,
          "urn:dktk:dataelement:26:4", DATE_STRING);

      patientContainer.getVitalState().ifPresent(vital -> patientBuilder.addAttribute(vital,
          "Observation.value.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS').code",
          CodeType.class, "urn:dktk:dataelement:53:3", PrimitiveType::getValue));

      patientContainer.getVitalState().ifPresent(vital -> patientBuilder.addAttributeOptional(vital,
          "Observation.effective",
          DateTimeType.class, "urn:dktk:dataelement:48:3", DATE_STRING));

      HistologyMapping histologyMapping = new HistologyMapping(fhirPathR4);
      ProgressMapping progressMapping = new ProgressMapping(fhirPathR4);

      patientBuilder.addContainers(patientContainer.getConditionContainerList().stream()
          .map(c -> diagnosisMapping.map(c.getCondition(), patient))
          .collect(Collectors.toList()));

      patientBuilder.addContainers(patientContainer.getSpecimenList().stream()
          .map(sampleMapping::map)
          .collect(Collectors.toList()));


/*

      // other resources (skip Patient resource)
      patientContainer.stream().skip(1).forEach(resource -> {
        switch (resource.getResourceType()) {

          case Condition:
            var condition = (Condition) resource;
            var diagnosis = diagnosisMapping.map(condition, patient);
            diagnosis.getContainer().add(tumorMapping.map(condition));
            patientBuilder.addContainer(diagnosis);
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

          case MedicationStatement:
            patientBuilder.addContainer(systemTherapyMapping.map((MedicationStatement) resource));
            break;
          default:
        }
      });
*/

      result.getPatient().add(patientBuilder.build());
    }
    return result;
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