package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import java.util.Optional;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Specimen;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR Specimen to MDS Sample.
 */
@Component
public class SampleMapping {

  private static final String SAMPLE_MATERIAL_TYPE =
      "https://fhir.bbmri.de/CodeSystem/SampleMaterialType";

  private final FhirPathR4 fhirPathEngine;

  /**
   * Creates a new SampleMapping.
   *
   * @param fhirPathEngine the FHIRPath engine
   */
  public SampleMapping(FhirPathR4 fhirPathEngine) {
    this.fhirPathEngine = Objects.requireNonNull(fhirPathEngine);
  }

  /**
   * Maps FHIR Specimen to MDS Sample.
   *
   * @param specimen the FHIR Specimen
   * @return the MDS Sample
   */
  public Container map(Specimen specimen) {
    var builder = new ContainerBuilder(fhirPathEngine, specimen, "Sample");

    builder.addAttribute2(typePath(SAMPLE_MATERIAL_TYPE), typePath("urn:centraxx"),
        CodeType.class, "urn:dktk:dataelement:97:1",
        (bbmriType, cxxCode) -> mapProbenart(bbmriType.getCode(), cxxCode.map(CodeType::getCode)));

    builder.addAttribute(typePath(SAMPLE_MATERIAL_TYPE) + ".exists()",
        BooleanType.class, "urn:dktk:dataelement:50:2", PrimitiveType::getValueAsString);

    builder.addAttributeOptional(typePath(SAMPLE_MATERIAL_TYPE),
        CodeType.class, "urn:dktk:dataelement:95:2", code -> mapProbentyp(code.getCode()));

    builder.addAttributeOptional(typePath(SAMPLE_MATERIAL_TYPE),
        CodeType.class, "urn:dktk:dataelement:90:1", code -> mapFixierungsart(code.getCode()));

    builder.addAttributeOptional("Specimen.collection.collected", DateTimeType.class,
        "urn:dktk:dataelement:49:4", DATE_STRING);

    return builder.build();
  }

  private static String typePath(String system) {
    return "Specimen.type.coding.where(system = '" + system + "').code";
  }

  private static String mapProbenart(String bbmriType, Optional<String> cxxCode) {
    return switch (bbmriType) {
      case "whole-blood" -> "Vollblut";
      case "bone-marrow" -> "Knochenmark";
      case "blood-plasma", "plasma-edta", "plasma-citrat", "plasma-heparin", "plasma-cell-free",
          "plasma-other" -> "Plasma";
      case "blood-serum" -> "Serum";
      case "csf-liquor", "liquid-other" -> "Liquor";
      case "urine" -> "Urin";
      case "tumor-tissue-ffpe", "tumor-tissue-frozen" -> "Tumorgewebe";
      case "normal-tissue-ffpe", "normal-tissue-frozen" -> "Normalgewebe";
      case "dna", "cf-dna", "g-dna" -> "DNA";
      case "rna" -> "RNA";
      //TODO: find value for protein
      // case "" -> "Protein";
      default -> cxxCode.flatMap(SampleMapping::mapProbenart2).orElse(bbmriType);
    };
  }

  private static Optional<String> mapProbenart2(String cxxCode) {
    return switch (cxxCode) {
      case "NGW" -> Optional.of("Normalgewebe");
      case "PTM", "TGW" -> Optional.of("Tumorgewebe");
      default -> Optional.empty();
    };
  }

  private static Optional<String> mapProbentyp(String code) {
    return switch (code) {
      case "tumor-tissue-ffpe", "normal-tissue-ffpe", "other-tissue-ffpe", "tissue-ffpe",
          "tissue-frozen", "tumor-tissue-frozen", "normal-tissue-frozen", "other-tissue-frozen",
          "tissue-other" -> Optional.of("Gewebeprobe");
      case "whole-blood", "blood-plasma", "plasma-edta", "plasma-citrat", "plasma-heparin",
          "plasma-cell-free", "plasma-other", "urine", "csf-liquor", "blood-serum",
          "liquid-other" -> Optional.of("Flüssigprobe");
      default -> Optional.empty();
    };
  }

  private static Optional<String> mapFixierungsart(String code) {
    if (code.contains("ffpe")) {
      return Optional.of("Paraffin (FFPE)");
    } else if (code.contains("frozen")) {
      return Optional.of("Kryo/Frisch (FF)");
    } else {
      return Optional.empty();
    }
  }
}
