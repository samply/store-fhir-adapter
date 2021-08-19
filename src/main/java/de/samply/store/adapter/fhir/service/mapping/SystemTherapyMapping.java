package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.springframework.stereotype.Component;

/**
 * @author Patrick Skowronek
 */

@Component
public class SystemTherapyMapping {

  private final FhirPathR4 fhirPathR4;


  public SystemTherapyMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  public Container map(MedicationStatement medicationStatement) {

    var builder = new ContainerBuilder(fhirPathR4, medicationStatement, "SystemTherapy");

    builder.addAttributeOptional("MedicationStatement.effective.start",
        DateTimeType.class, "urn:dktk:dataelement:90:1", DATE_STRING);

    builder.addAttributeOptional("MedicationStatement.effective.end",
        DateTimeType.class, "urn:dktk:dataelement:93:1", DATE_STRING);

    builder.addAttribute("MedicationStatement.medication.coding.code",
        CodeType.class, "urn:dktk:dataelement:91:1", PrimitiveType::getValue);

    //TODO: Add Protocl

    return builder.build();
  }

}
