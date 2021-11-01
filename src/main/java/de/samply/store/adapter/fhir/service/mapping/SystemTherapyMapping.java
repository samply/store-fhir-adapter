package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR MedicationStatement to MDS SystemTherapy.
 */
@Component
public class SystemTherapyMapping {

  private final FhirPathR4 fhirPathEngine;

  /**
   * Creates a new SystemTherapyMapping.
   *
   * @param fhirPathEngine the FHIRPath engine
   */
  public SystemTherapyMapping(FhirPathR4 fhirPathEngine) {
    this.fhirPathEngine = Objects.requireNonNull(fhirPathEngine);
  }

  /**
   * Maps FHIR MedicationStatement to MDS SystemTherapy.
   *
   * @param medicationStatement the FHIR MedicationStatement
   * @return the MDS SystemTherapy
   */
  public Container map(MedicationStatement medicationStatement) {
    var builder = new ContainerBuilder(fhirPathEngine, medicationStatement, "SystemTherapy");

    builder.addAttributeOptional("MedicationStatement.effective.start",
        DateTimeType.class, "urn:dktk:dataelement:90:1", DATE_STRING);

    builder.addAttributeOptional("MedicationStatement.effective.end",
        DateTimeType.class, "urn:dktk:dataelement:93:1", DATE_STRING);

    builder.addAttribute("MedicationStatement.medication.coding.code",
        CodeType.class, "urn:dktk:dataelement:91:1", PrimitiveType::getValue);

    //TODO: Add Protocol

    return builder.build();
  }
}
