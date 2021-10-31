package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR Observation to MDS Histology.
 */
@Component
public class HistologyMapping {

  private static final String ICD_O_3 = "urn:oid:2.16.840.1.113883.6.43.1";
  private static final String GRADING = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GradingCS";

  private final FhirPathR4 fhirPathEngine;

  /**
   * Creates a new HistologyMapping.
   *
   * @param fhirPathEngine the FHIRPath engine
   */
  public HistologyMapping(FhirPathR4 fhirPathEngine) {
    this.fhirPathEngine = Objects.requireNonNull(fhirPathEngine);
  }

  /**
   * Maps FHIR histology Observation to MDS Histology.
   *
   * @param histology the FHIR histology Observation
   * @return the MDS Histology
   */
  public Container map(Observation histology) {
    var builder = new ContainerBuilder(fhirPathEngine, histology, "Histology");

    builder.addAttribute("Observation.value.coding.where(system = '" + ICD_O_3 + "').code",
        CodeType.class, "urn:dktk:dataelement:7:2", PrimitiveType::getValue);

    builder.addAttribute("Observation.value.coding.where(system = '" + ICD_O_3 + "').version",
        StringType.class, "urn:dktk:dataelement:8:2", PrimitiveType::getValue);

    builder.addAttribute(
        "Observation.hasMember.resolve().value.coding.where(system = '" + GRADING + "').code",
        CodeType.class, "urn:dktk:dataelement:9:2", PrimitiveType::getValue);

    return builder.build();
  }
}
