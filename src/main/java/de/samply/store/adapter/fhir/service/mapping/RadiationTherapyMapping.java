package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR Procedure to MDS RadiationTherapy.
 */
@Component
public class RadiationTherapyMapping {

  private final FhirPathR4 fhirPathR4;

  /**
   * Creates a new RadiationTherapyMapping.
   *
   * @param fhirPathR4 the FHIRPath engine
   */
  public RadiationTherapyMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = Objects.requireNonNull(fhirPathR4);
  }

  /**
   * Maps FHIR Procedure to MDS RadiationTherapy.
   *
   * @param therapy the FHIR Procedure
   * @return the MDS RadiationTherapy
   */
  public Container map(Procedure therapy) {
    var builder = new ContainerBuilder(fhirPathR4, therapy, "RadiationTherapy");

    builder.addAttribute("urn:dktk:dataelement:34:2", "true");
    builder.addAttributeOptional("Procedure.performed.start", DateTimeType.class,
        "urn:dktk:dataelement:77:1", DATE_STRING);
    builder.addAttributeOptional("Procedure.performed.end", DateTimeType.class,
        "urn:dktk:dataelement:78:1", DATE_STRING);

    return builder.build();
  }
}
