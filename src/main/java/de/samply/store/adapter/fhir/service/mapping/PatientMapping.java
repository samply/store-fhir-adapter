package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Patient;
import de.samply.store.adapter.fhir.model.PatientNode;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.PatientBuilder;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Mapping of a FHIR Patient and other resources to MDS Patient.
 */
@Component
public class PatientMapping {

  private static final String PSEUDONYM_ART_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS";
  private static final String GLOBAL_ID_PATH =
      "Patient.where(identifier.type.coding.where(system= \"" + PSEUDONYM_ART_CS
          + "\" and code= \"Global\" ).exists()).identifier.where(type.coding.code= \"Global\" ).value";
  private static final String VITAL_STATE_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS";

  private final FhirPathR4 fhirPathEngine;
  private final DiagnosisMapping diagnosisMapping;
  private final SampleMapping sampleMapping;

  /**
   * Creates a new PatientMapping.
   *
   * @param fhirPathEngine   the FHIRPath engine
   * @param diagnosisMapping DiagnosisMapping
   * @param sampleMapping    SampleMapping
   */
  public PatientMapping(FhirPathR4 fhirPathEngine, DiagnosisMapping diagnosisMapping,
      SampleMapping sampleMapping) {
    this.fhirPathEngine = Objects.requireNonNull(fhirPathEngine);
    this.diagnosisMapping = Objects.requireNonNull(diagnosisMapping);
    this.sampleMapping = Objects.requireNonNull(sampleMapping);
  }

  /**
   * Maps a FHIR Patient and other resources to MDS Patient.
   *
   * @param patientNode the patient node which holds the Patient and other resources like the
   *                    Condition und Specimen resources.
   * @return the MDS Patient
   */
  public Patient map(PatientNode patientNode) {
    var patient = patientNode.patient();
    var patientBuilder = new PatientBuilder(fhirPathEngine, patient);

    patientBuilder.addAttribute(GLOBAL_ID_PATH, StringType.class, "urn:dktk:dataelement:54:1",
        PrimitiveType::getValue);

    patientBuilder.addAttribute("Patient.gender", Enumeration.class, "urn:dktk:dataelement:1:3",
        gen -> mapGenderValue(gen.getValueAsString()));

    patientBuilder.addAttributeOptional("Patient.birthDate", DateType.class,
        "urn:dktk:dataelement:26:4", DATE_STRING);

    patientNode.vitalState().ifPresent(vital -> patientBuilder.addAttribute(vital,
        "Observation.value.coding.where(system = '" + VITAL_STATE_CS + "').code",
        CodeType.class, "urn:dktk:dataelement:53:3", PrimitiveType::getValue));

    //TODO: remove day
    patientNode.vitalState().ifPresent(vital -> patientBuilder.addAttributeOptional(vital,
        "Observation.effective",
        DateTimeType.class, "urn:dktk:dataelement:48:3", DATE_STRING));

    patientBuilder.addContainers(patientNode.conditions().stream()
        .map(diagnosisMapping::map)
        .toList());

    patientBuilder.addContainers(patientNode.specimens().stream()
        .map(sampleMapping::map)
        .toList());

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
