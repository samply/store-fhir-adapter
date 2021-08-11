package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;
import static de.samply.store.adapter.fhir.service.mapping.Util.LOCAL_DATE;
import static de.samply.store.adapter.fhir.service.mapping.Util.lift2;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

/**
 * Mapping of the Sample container.
 *
 * @author Patrick Skowronek
 */

@Component
public class TumorMapping {

  public static final String ICD_0_3 = "urn:oid:2.16.840.1.113883.6.43.1";
  public static final String ADT_Site = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS";

  private final FhirPathR4 fhirPathR4;

  public TumorMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  public Container map(Condition condition) {
    var builder = new ContainerBuilder(fhirPathR4, condition, "Tumor");

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ICD_0_3 + "').code",
        CodeType.class, "urn:dktk:dataelement:4:2", PrimitiveType::getValue);

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ICD_0_3 + "').version",
        StringType.class, "urn:dktk:dataelement:5:2", PrimitiveType::getValue);

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ADT_Site + "').code",
        CodeType.class, "urn:dktk:dataelement:6:2", PrimitiveType::getValue);

    return builder.build();
  }
}
