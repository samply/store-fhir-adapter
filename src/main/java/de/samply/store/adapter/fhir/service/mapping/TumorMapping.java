package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.model.ConditionContainer;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Mapping of the Sample container.
 *
 * @author Patrick Skowronek
 */

@Component
public class TumorMapping {

  private static final String ICD_0_3 = "urn:oid:2.16.840.1.113883.6.43.1";
  private static final String ADT_Site = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS";
  private final String url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Fernmetastasen";


  private final FhirPathR4 fhirPathR4;

  private final HistologyMapping histologyMapping;
  private final MetastasisMapping metastasisMapping;
  private final ProgressMapping progressMapping;
  private final TNMMapping tnmMapping;

  public TumorMapping(FhirPathR4 fhirPathR4,
      HistologyMapping histologyMapping,
      MetastasisMapping metastasisMapping,
      ProgressMapping progressMapping,
      TNMMapping tnmMapping) {
    this.fhirPathR4 = fhirPathR4;
    this.histologyMapping = histologyMapping;
    this.progressMapping = progressMapping;
    this.metastasisMapping = metastasisMapping;
    this.tnmMapping = tnmMapping;
  }

  public Container map(ConditionContainer conditionContainer) {
    var condition = conditionContainer.getCondition();
    var builder = new ContainerBuilder(fhirPathR4, condition, "Tumor");

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
        "Condition.extension.where(url = '" + url + "').value.resolve()",
        Observation.class,
        tnmMapping::map);

    builder.addContainers(conditionContainer.getClinicalImpressionContainers().stream()
        .map(impression -> progressMapping.map(
            impression.getClinicalImpression())).collect(Collectors.toList()));


    return builder.build();
  }
}
