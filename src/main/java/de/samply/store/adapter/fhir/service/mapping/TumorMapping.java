package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.model.ConditionNode;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR Condition and related resources to MDS Tumor.
 */
@Component
public class TumorMapping {

  private static final String ICD_0_3 = "urn:oid:2.16.840.1.113883.6.43.1";
  private static final String ADT_Site = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS";
  private static final String EXTENSION_FERNMETASTASEN = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Fernmetastasen";

  private final FhirPathR4 fhirPathR4;
  private final HistologyMapping histologyMapping;
  private final MetastasisMapping metastasisMapping;
  private final ProgressMapping progressMapping;
  private final TnmMapping tnmMapping;

  /**
   * Creates a new TumorMapping.
   *
   * @param fhirPathR4 the FHIRPath engine
   * @param histologyMapping histology mapping
   * @param metastasisMapping metastasis mapping
   * @param progressMapping progress mapping
   * @param tnmMapping TNM mapping
   */
  public TumorMapping(FhirPathR4 fhirPathR4,
      HistologyMapping histologyMapping,
      MetastasisMapping metastasisMapping,
      ProgressMapping progressMapping,
      TnmMapping tnmMapping) {
    this.fhirPathR4 = Objects.requireNonNull(fhirPathR4);
    this.histologyMapping = Objects.requireNonNull(histologyMapping);
    this.progressMapping = Objects.requireNonNull(progressMapping);
    this.metastasisMapping = Objects.requireNonNull(metastasisMapping);
    this.tnmMapping = Objects.requireNonNull(tnmMapping);
  }

  /**
   * Maps FHIR Condition and related resources to MDS Tumor.
   *
   * @param node the node with the Condition and related resources
   * @return the Tumor
   */
  public Container map(ConditionNode node) {
    var builder = new ContainerBuilder(fhirPathR4, node.condition(), "Tumor");

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ICD_0_3 + "').code",
        CodeType.class, "urn:dktk:dataelement:4:2", PrimitiveType::getValue);

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ICD_0_3 + "').version",
        StringType.class, "urn:dktk:dataelement:5:2", PrimitiveType::getValue);

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ADT_Site + "').code",
        CodeType.class, "urn:dktk:dataelement:6:2", PrimitiveType::getValue);

    builder.addContainer("Condition.evidence.detail.resolve()", Observation.class,
        histologyMapping::map);

    builder.addContainer("Condition.stage.assessment.resolve()", Observation.class,
        metastasisMapping::map);

    builder.addContainer(
        "Condition.extension.where(url = '" + EXTENSION_FERNMETASTASEN + "').value.resolve()",
        Observation.class,
        tnmMapping::map);

    builder.addContainers(node.clinicalImpressions().stream().map(progressMapping::map).toList());

    return builder.build();
  }
}
