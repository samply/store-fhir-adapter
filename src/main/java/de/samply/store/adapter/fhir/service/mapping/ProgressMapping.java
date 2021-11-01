package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.model.ClinicalImpressionNode;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR ClinicalImpression to MDS Progress.
 */
@Component
public class ProgressMapping {

  private static final String TNM_C = "21908-9";
  private static final String TNM_P = "21902-2";
  private static final String HISTOLOGY = "59847-4";
  private static final String VITAL_STATE = "75186-7";
  private static final String CANCER_OUTCOME_STATE = "21976-6";

  private final FhirPathR4 fhirPathEngine;
  private final TnmMapping tnmMapping;

  /**
   * Creates a new ProgressMapping.
   *
   * @param fhirPathEngine the FHIRPath engine
   * @param tnmMapping     the TNM mapping
   */
  public ProgressMapping(FhirPathR4 fhirPathEngine, TnmMapping tnmMapping) {
    this.fhirPathEngine = Objects.requireNonNull(fhirPathEngine);
    this.tnmMapping = Objects.requireNonNull(tnmMapping);
  }

  /**
   * Maps FHIR ClinicalImpression to MDS Progress.
   *
   * @param node the node with the ClinicalImpression
   * @return the MDS Progress
   */
  public Container map(ClinicalImpressionNode node) {
    var builder = new ContainerBuilder(fhirPathEngine, node.clinicalImpression(), "Progress");

    builder.addAttributeOptional("ClinicalImpression.effective", DateTimeType.class,
        "urn:dktk:dataelement:25:4", DATE_STRING);

    builder.addAttributeOptional("ClinicalImpression.effective", DateTimeType.class,
        "urn:dktk:dataelement:43:3", DATE_STRING);

    builder.addAttributeOptional("ClinicalImpression.effective", DateTimeType.class,
        "urn:dktk:dataelement:45:3", DATE_STRING);

    builder.addAttribute(itemValuePath(VITAL_STATE), CodeType.class,
        "urn:dktk:dataelement:53:3", PrimitiveType::getValue);

    builder.addAttribute(itemValuePath(HISTOLOGY), CodeType.class,
        "urn:dktk:dataelement:7:2", PrimitiveType::getValue);

    builder.addAttribute(itemValuePath(CANCER_OUTCOME_STATE), CodeType.class,
        "urn:dktk:dataelement:24:3", PrimitiveType::getValue);

    builder.addAttribute(itemValuePath("LA4583-6"), CodeType.class,
        "urn:dktk:dataelement:72:2", PrimitiveType::getValue);

    builder.addAttribute(itemValuePath("LA4370-8"), CodeType.class,
        "urn:dktk:dataelement:73:2", PrimitiveType::getValue);

    builder.addAttribute(itemValuePath("LA4226-2"), CodeType.class,
        "urn:dktk:dataelement:74:2", PrimitiveType::getValue);

    builder.addContainer(itemPath(TNM_C), Observation.class, tnmMapping::map);
    builder.addContainer(itemPath(TNM_P), Observation.class, tnmMapping::map);

    return builder.build();
  }

  private static String itemValuePath(String code) {
    return itemPath(code) + ".value.coding.code";
  }

  private static String itemPath(String code) {
    return "ClinicalImpression.finding.itemReference.resolve().where(code.coding.code = '" + code
        + "')";
  }
}
