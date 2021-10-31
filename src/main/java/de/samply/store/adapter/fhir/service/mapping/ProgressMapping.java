package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.model.ClinicalImpressionNode;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR ClinicalImpression to MDS Progress.
 */
@Component
public class ProgressMapping {

  private static final String ITEM_REFERENCE = "ClinicalImpression.finding.itemReference.resolve()";

  private final FhirPathR4 fhirPathEngine;

  /**
   * Creates a new ProgressMapping.
   *
   * @param fhirPathEngine the FHIRPath engine
   */
  public ProgressMapping(FhirPathR4 fhirPathEngine) {
    this.fhirPathEngine = Objects.requireNonNull(fhirPathEngine);
  }

  /**
   * Maps FHIR ClinicalImpression to MDS Progress.
   *
   * @param node the node with the ClinicalImpression
   * @return the MDS Progress
   */
  public Container map(ClinicalImpressionNode node) {
    var builder = new ContainerBuilder(fhirPathEngine, node.clinicalImpression(), "Progress");

    builder.addAttribute(ITEM_REFERENCE + ".where(code.coding.code = '21976-6').value.coding.code",
        CodeType.class, "urn:dktk:dataelement:24:3", PrimitiveType::getValue);

    builder.addAttributeOptional("ClinicalImpression.effective", DateTimeType.class,
        "urn:dktk:dataelement:25:4", DATE_STRING);

    builder.addAttributeOptional("ClinicalImpression.effective", DateTimeType.class,
        "urn:dktk:dataelement:43:3", DATE_STRING);

    builder.addAttributeOptional("ClinicalImpression.effective", DateTimeType.class,
        "urn:dktk:dataelement:45:3", DATE_STRING);

    builder.addAttribute(ITEM_REFERENCE + ".where(code.coding.code = 'LA4583-6').value.coding.code",
        CodeType.class, "urn:dktk:dataelement:72:2", PrimitiveType::getValue);

    builder.addAttribute(ITEM_REFERENCE + ".where(code.coding.code = 'LA4370-8').value.coding.code",
        CodeType.class, "urn:dktk:dataelement:73:2", PrimitiveType::getValue);

    builder.addAttribute(ITEM_REFERENCE + ".where(code.coding.code = 'LA4226-2').value.coding.code",
        CodeType.class, "urn:dktk:dataelement:74:2", PrimitiveType::getValue);

    return builder.build();
  }
}
