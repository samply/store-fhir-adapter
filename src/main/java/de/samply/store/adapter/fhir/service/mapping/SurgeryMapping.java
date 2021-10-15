package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR Procedure to MDS Surgery.
 */
@Component
public class SurgeryMapping {

  private static final String SYSTEM_LOCAL = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS";
  private static final String SYSTEM_GLOBAL = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS";

  private final FhirPathR4 fhirPathR4;

  /**
   * Creates a new SurgeryMapping.
   *
   * @param fhirPathR4 the FHIRPath engine
   */
  public SurgeryMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = Objects.requireNonNull(fhirPathR4);
  }

  /**
   * Maps FHIR Procedure to MDS Surgery.
   *
   * @param procedure the FHIR Procedure
   * @return the MDS Surgery
   */
  public Container map(Procedure procedure) {
    var builder = new ContainerBuilder(fhirPathR4, procedure, "Surgery");

    builder.addAttribute(
        "Procedure.outcome.coding.where(system = '" + SYSTEM_LOCAL + "').code",
        CodeType.class,
        "urn:dktk:dataelement:19:2", PrimitiveType::getValue);
    builder.addAttribute(
        "Procedure.outcome.coding.where(system = '" + SYSTEM_GLOBAL + "').code",
        CodeType.class,
        "urn:dktk:dataelement:20:3", PrimitiveType::getValue);

    return builder.build();
  }
}
