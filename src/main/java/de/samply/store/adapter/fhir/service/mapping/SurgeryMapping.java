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
public class SurgeryMapping implements ProcedureMapping {

  private static final String SYSTEM_LOCAL =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS";
  private static final String SYSTEM_GLOBAL =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS";

  private final FhirPathR4 fhirPathEngine;

  /**
   * Creates a new SurgeryMapping.
   *
   * @param fhirPathEngine the FHIRPath engine
   */
  public SurgeryMapping(FhirPathR4 fhirPathEngine) {
    this.fhirPathEngine = Objects.requireNonNull(fhirPathEngine);
  }

  /**
   * Maps FHIR Procedure to MDS Surgery.
   *
   * @param procedure the FHIR Procedure
   * @return the MDS Surgery
   */
  public Container map(Procedure procedure) {
    var builder = new ContainerBuilder(fhirPathEngine, procedure, "Surgery");

    builder.addAttribute(outcomePath(SYSTEM_LOCAL), CodeType.class,
        "urn:dktk:dataelement:19:2", PrimitiveType::getValue);
    builder.addAttribute(outcomePath(SYSTEM_GLOBAL), CodeType.class,
        "urn:dktk:dataelement:20:3", PrimitiveType::getValue);
    builder.addAttribute("urn:dktk:dataelement:23:3", "X");

    return builder.build();
  }

  private static String outcomePath(String system) {
    return "Procedure.outcome.coding.where(system = '" + system + "').code";
  }
}
