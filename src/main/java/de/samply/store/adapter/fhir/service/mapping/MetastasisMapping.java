package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR Observation to MDS Metastasis.
 */
@Component
public class MetastasisMapping {

  private final FhirPathR4 fhirPathR4;

  /**
   * Creates a new MetastasisMapping.
   *
   * @param fhirPathR4 the FHIRPath engine
   */
  public MetastasisMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = Objects.requireNonNull(fhirPathR4);
  }

  /**
   * maps FHIR Observation to MDS Metastasis.
   *
   * @param metastasis the FHIR Observation
   * @return the MDS Metastasis
   */
  public Container map(Observation metastasis) {
    var builder = new ContainerBuilder(fhirPathR4, metastasis, "Metastasis");

    builder.addAttribute("Observation.value.coding.code",
        CodeType.class, "urn:dktk:dataelement:77:1", PrimitiveType::getValue);

    builder.addAttribute("Observation.bodySite.coding.code",
        StringType.class, "urn:dktk:dataelement:98:1", PrimitiveType::getValue);

    builder.addAttributeOptional("Observation.effective",
        DateTimeType.class, "urn:dktk:dataelement:21:3", DATE_STRING);

    return builder.build();
  }
}
