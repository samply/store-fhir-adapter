package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;
import static de.samply.store.adapter.fhir.service.mapping.Util.LOCAL_DATE;
import static de.samply.store.adapter.fhir.service.mapping.Util.createAttribute;
import static de.samply.store.adapter.fhir.service.mapping.Util.lift2;

import de.samply.share.model.ccp.Container;
import java.time.LocalDate;
import java.util.Optional;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PrimitiveType;
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

  public DiagnosisMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
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

    //TODO: Next ocnology version will enforce datetime
    builder.addAttributeOptional("Condition.onset", DateTimeType.class, "urn:dktk:dataelement:83:3",
        DATE_STRING);

    // age at first condition
    builder.addAttributeOptional("Condition.onset", DateTimeType.class, "urn:dktk:dataelement:28:1",
        onsetDateTime -> lift2(DiagnosisMapping::calcAgeValue)
            .apply(LOCAL_DATE.apply(pa.getBirthDateElement()),
                LOCAL_DATE.apply(onsetDateTime)));

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ICD_O_3 + "').code",
        CodeType.class, "urn:dktk:dataelement:4:2", PrimitiveType::getValue);

    return builder.build();
  }

}
