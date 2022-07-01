package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR MedicationStatement to MDS SystemTherapy.
 */
@Component
public class SystemTherapyMapping {

  private final FhirPathR4 fhirPathEngine;

  private final String codePro =
          "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SystemischeTherapieProtokoll";
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
        DateTimeType.class, "urn:adt:dataelement:90:1", DATE_STRING);

    builder.addAttributeOptional("MedicationStatement.effective.end",
        DateTimeType.class, "urn:adt:dataelement:93:1", DATE_STRING);

    builder.addAttribute("MedicationStatement.medication.text",
        StringType.class, "urn:adt:dataelement:91:1", PrimitiveType::getValue);

    builder.addAttribute("MedicationStatement.extension('" + codePro + "').value",
            StringType.class, "urn:adt:dataelement:89:1", PrimitiveType::getValue);

    return builder.build();
  }
}
