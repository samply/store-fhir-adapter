package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;
import static de.samply.store.adapter.fhir.service.mapping.Util.LOCAL_DATE;
import static de.samply.store.adapter.fhir.service.mapping.Util.lift2;

import de.samply.share.model.ccp.Container;
import java.time.LocalDate;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Mapping of the Diagnosis container.
 *
 * @author Alexander Kiel
 */
@Component
public class DiagnosisMapping {

  public static final String ICD_10_GM = "http://fhir.de/CodeSystem/dimdi/icd-10-gm";
  public static final String ICD_O_3 = "urn:oid:2.16.840.1.113883.6.43.1";

  private final FhirPathR4 fhirPathR4;
  private final HistologyMapping histologyMapping;

  public DiagnosisMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
    this.histologyMapping = new HistologyMapping(fhirPathR4);
  }

  private static String calcAgeValue(LocalDate birthDate, LocalDate firstConditionOnset) {
    return Integer.toString(birthDate.until(firstConditionOnset).getYears());
  }

  /**
   * Maps the Condition resource into a Diagnosis container.
   *
   * @param condition the Condition resource to map
   * @return the Diagnosis container
   */
  public Container map(Condition condition, Patient pa) {
    var builder = new ContainerBuilder(fhirPathR4, condition, "Diagnosis");

    builder.addAttribute("Condition.code.coding.where(system = '" + ICD_10_GM + "').code",
        CodeType.class, "urn:dktk:dataelement:29:2", PrimitiveType::getValue);

    if (condition.hasOnsetAge()) {
      builder.addAttribute("Condition.onset.value", DecimalType.class, "urn:dktk:dataelement:28:1",
          PrimitiveType::getValueAsString);
    }

    if (condition.hasRecordedDate()) {
      builder
          .addAttributeOptional("Condition.recordedDate", DateTimeType.class,
              "urn:dktk:dataelement:83:3",
              DATE_STRING);
    }

    //TODO: Next ocnology version will enforce datetime
    if (condition.hasOnsetDateTimeType()) {
      builder
          .addAttributeOptional("Condition.onset", DateTimeType.class, "urn:dktk:dataelement:83:3",
              DATE_STRING);

      // age at first condition
      builder
          .addAttributeOptional("Condition.onset", DateTimeType.class,
              "urn:dktk:dataelement:28:1",
              onsetDateTime -> lift2(DiagnosisMapping::calcAgeValue)
                  .apply(LOCAL_DATE.apply(pa.getBirthDateElement()),
                      LOCAL_DATE.apply(onsetDateTime)));
    }

    builder.addContainer("Condition.evidence.detail.resolve()", Observation.class,
        histologyMapping::map);

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ICD_O_3 + "').code",
        CodeType.class, "urn:dktk:dataelement:4:2", PrimitiveType::getValue);

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ICD_O_3 + "').version",
        StringType.class, "urn:dktk:dataelement:3:2", s -> "10 " + s + " GM");

    /*
      ProgressMapping progressMapping = new ProgressMapping(fhirPathR4);

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

    return builder.build();
  }

}
