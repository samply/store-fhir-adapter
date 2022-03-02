package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.share.model.ccp.ObjectFactory;
import de.samply.store.adapter.fhir.model.ConditionNode;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR Condition and related resources to MDS Tumor.
 */
@Component
public class TumorMapping {

  private static final String ICD_0_3 =
      "urn:oid:2.16.840.1.113883.6.43.1";
  private static final String ADT_SITE =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS";
  private static final String EXTENSION_FERNMETASTASEN =
      "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Fernmetastasen";

  private final FhirPathR4 fhirPathEngine;
  private final HistologyMapping histologyMapping;
  private final MetastasisMapping metastasisMapping;
  private final SurgeryMapping surgeryMapping;
  private final RadiationTherapyMapping radiationTherapyMapping;
  private final ProgressMapping progressMapping;
  private final TnmMapping tnmMapping;

  /**
   * Creates a new TumorMapping.
   *
   * @param fhirPathEngine          the FHIRPath engine
   * @param histologyMapping        the histology mapping
   * @param metastasisMapping       the metastasis mapping
   * @param surgeryMapping          the surgery mapping
   * @param radiationTherapyMapping the radiation  therapy mapping
   * @param progressMapping         the progress mapping
   * @param tnmMapping              the TNM mapping
   */
  public TumorMapping(FhirPathR4 fhirPathEngine,
      HistologyMapping histologyMapping,
      MetastasisMapping metastasisMapping,
      SurgeryMapping surgeryMapping,
      RadiationTherapyMapping radiationTherapyMapping,
      ProgressMapping progressMapping,
      TnmMapping tnmMapping) {
    this.fhirPathEngine = Objects.requireNonNull(fhirPathEngine);
    this.histologyMapping = Objects.requireNonNull(histologyMapping);
    this.surgeryMapping = Objects.requireNonNull(surgeryMapping);
    this.radiationTherapyMapping = Objects.requireNonNull(radiationTherapyMapping);
    this.progressMapping = Objects.requireNonNull(progressMapping);
    this.metastasisMapping = Objects.requireNonNull(metastasisMapping);
    this.tnmMapping = Objects.requireNonNull(tnmMapping);
  }

  private static String bodySitePath(String system) {
    return "Condition.bodySite.coding.where(system = '" + system + "').code";
  }

  /**
   * Maps FHIR Condition and related resources to MDS Tumor.
   *
   * @param node the node with the Condition and related resources
   * @return the Tumor
   */
  public Container map(ConditionNode node) {
    var builder = new ContainerBuilder(fhirPathEngine, node.condition(), "Tumor");

    builder.addAttribute(bodySitePath(ICD_0_3),
        CodeType.class, "urn:dktk:dataelement:4:2", PrimitiveType::getValue);

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ICD_0_3 + "').version",
        StringType.class, "urn:dktk:dataelement:5:2", PrimitiveType::getValue);

    builder.addAttribute(bodySitePath(ADT_SITE),
        CodeType.class, "urn:dktk:dataelement:6:2", PrimitiveType::getValue);

    builder.addContainer("Condition.evidence.detail.resolve()", Observation.class,
        histologyMapping::map);

    builder.addContainer("Condition.stage.assessment.resolve()", Observation.class,
        metastasisMapping::map);

    builder.addContainer(
        "Condition.extension.where(url = '" + EXTENSION_FERNMETASTASEN + "').value.resolve()",
        Observation.class,
        tnmMapping::map);

    builder.addContainers(node.procedures().stream()
        .filter(procedure -> "OP".equals(procedure.getCategory().getCodingFirstRep().getCode()))
        .map(surgery -> {
          Container container = mapProcedure(surgery, "Surgery", surgeryMapping);
          mapProgressFalse(container, "true", "false", "false", "false",
              "false", "false", "false");

          if (surgery.hasExtension(
              "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-OPIntention")) {

            CodeableConcept intention = (CodeableConcept) surgery.getExtensionByUrl(
                    "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-OPIntention")
                .getValue();
            container.getAttribute().add(Util.createAttribute("urn:dktk:dataelement:23:3",
                intention.getCodingFirstRep().getCode()));
          }
          return container;
        })
        .toList());

    builder.addContainers(node.procedures().stream()
        .filter(procedure -> "ST".equals(procedure.getCategory().getCodingFirstRep().getCode()))
        .map(radiationTherapy -> {
          Container container = mapProcedure(radiationTherapy, "RadiationTherapy",
              radiationTherapyMapping);
          mapProgressFalse(container, "false", "true", "false", "false",
              "false", "false", "false");
          return container;
        })
        .toList());

    builder.addContainers(node.procedures().stream()
        .filter(procedure -> "CH".equals(procedure.getCategory().getCodingFirstRep().getCode()))
        .map(chemoTherapy -> {
          Container progress = new ObjectFactory().createContainer();
          progress.setDesignation("Progress");
          mapProgressFalse(progress, "false", "flase", "true", "false",
              "false", "false", "false");
          return progress;
        }).toList());

    builder.addContainers(node.procedures().stream()
        .filter(procedure -> "HO".equals(procedure.getCategory().getCodingFirstRep().getCode()))
        .map(hormoneTherapy -> {
          Container progress = new ObjectFactory().createContainer();
          progress.setDesignation("Progress");
          mapProgressFalse(progress, "false", "flase", "flase", "false",
              "true", "false", "false");
          return progress;
        }).toList());

    builder.addContainers(node.procedures().stream()
        .filter(procedure -> "IM".equals(procedure.getCategory().getCodingFirstRep().getCode()))
        .map(immunoTherapy -> {
          Container progress = new ObjectFactory().createContainer();
          progress.setDesignation("Progress");
          mapProgressFalse(progress, "false", "flase", "flase", "true",
              "false", "false", "false");
          return progress;
        }).toList());

    builder.addContainers(node.procedures().stream()
        .filter(procedure -> "KM".equals(procedure.getCategory().getCodingFirstRep().getCode()))
        .map(boneMarrowTherapy -> {
          Container progress = new ObjectFactory().createContainer();
          progress.setDesignation("Progress");
          mapProgressFalse(progress, "false", "flase", "flase", "false",
              "false", "true", "false");
          return progress;
        }).toList());

    builder.addContainers(node.procedures().stream()
        .filter(procedure -> "SO".equals(procedure.getCategory().getCodingFirstRep().getCode()))
        .map(diverseTherapy -> {
          Container progress = new ObjectFactory().createContainer();
          progress.setDesignation("Progress");
          mapProgressFalse(progress, "false", "flase", "flase", "false",
              "false", "false", "true");
          return progress;
        }).toList());

    builder.addContainers(node.clinicalImpressions().stream().map(progressMapping::map).toList());

    return builder.build();
  }

  private Container mapProcedure(Procedure surgery, String type, ProcedureMapping mapping) {
    Container progress = new ObjectFactory().createContainer();
    progress.setId("Progress-" + type + "-" + surgery.getIdElement().getIdPart());
    progress.setDesignation("Progress");
    progress.getContainer().add(mapping.map(surgery));
    return progress;
  }

  private void mapProgressFalse(Container container, String operation, String radiation,
      String chemo, String immuno, String hormone, String boneMarrow, String diverse) {
    container.getAttribute().add(Util.createAttribute("urn:dktk:dataelement:33:2", operation));
    container.getAttribute().add(Util.createAttribute("urn:dktk:dataelement:34:2", radiation));
    container.getAttribute().add(Util.createAttribute("urn:dktk:dataelement:36:2", chemo));
    container.getAttribute().add(Util.createAttribute("urn:dktk:dataelement:38:2", immuno));
    container.getAttribute().add(Util.createAttribute("urn:dktk:dataelement:39:2", hormone));
    container.getAttribute().add(Util.createAttribute("urn:dktk:dataelement:40:2", boneMarrow));
    container.getAttribute().add(Util.createAttribute("urn:dktk:dataelement:41:3", diverse));
  }
}
