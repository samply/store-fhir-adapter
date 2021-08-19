package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Patient;
import de.samply.store.adapter.fhir.model.PatientContainer;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.PatientBuilder;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.springframework.stereotype.Component;

@Component
public class PatientMapping {

  private final FhirPathR4 fhirPathEngine;
  private DiagnosisMapping diagnosisMapping;
  private SampleMapping sampleMapping;

  public PatientMapping(FhirPathR4 fhirPathEngine, DiagnosisMapping diagnosisMapping,
      SampleMapping sampleMapping) {
    this.fhirPathEngine = fhirPathEngine;

    this.diagnosisMapping = diagnosisMapping;
    this.sampleMapping = sampleMapping;
  }

  public Patient map(PatientContainer patientContainer) {

    var patient = patientContainer.getPatient();
    var patientBuilder = new PatientBuilder(fhirPathEngine, patient);

    patientBuilder.addAttribute("Patient.gender", Enumeration.class, "urn:dktk:dataelement:1:3",
        gen -> mapGenderValue(gen.getValueAsString()));

    patientBuilder.addAttributeOptional("Patient.birthDate", DateType.class,
        "urn:dktk:dataelement:26:4", DATE_STRING);

    patientContainer.getVitalState().ifPresent(vital -> patientBuilder.addAttribute(vital,
        "Observation.value.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS').code",
        CodeType.class, "urn:dktk:dataelement:53:3", PrimitiveType::getValue));

    patientContainer.getVitalState().ifPresent(vital -> patientBuilder.addAttributeOptional(vital,
        "Observation.effective",
        DateTimeType.class, "urn:dktk:dataelement:48:3", DATE_STRING));

    patientBuilder.addContainers(patientContainer.getConditionContainers().stream()
        .map(c -> diagnosisMapping.map(c, patient))
        .collect(Collectors.toList()));

    patientBuilder.addContainers(patientContainer.getSpecimenList().stream()
        .map(sampleMapping::map)
        .collect(Collectors.toList()));

    return patientBuilder.build();
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
