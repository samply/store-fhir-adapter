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

  private static final String SAMPLE_MATERIAL_TYPE = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType";

  private final FhirPathR4 fhirPathR4;

  /**
   * Creates a new SampleMapping.
   *
   * @param fhirPathR4 the FHIRPath engine
   */
  public SampleMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = Objects.requireNonNull(fhirPathR4);
  }

  /**
   * Maps FHIR Specimen to MDS Sample.
   *
   * @param specimen the FHIR Specimen
   * @return the MDS Sample
   */
  public Container map(Specimen specimen) {
    var builder = new ContainerBuilder(fhirPathR4, specimen, "Sample");

    builder.addAttribute(
        "Specimen.type.coding.where(system = '" + SAMPLE_MATERIAL_TYPE + "').code",
        CodeType.class, "urn:dktk:dataelement:97:1",
        code -> mapSpecimenKindValue(code.getCode()));

    builder.addAttribute(
        "Specimen.type.coding.where(system = '" + SAMPLE_MATERIAL_TYPE + "').code.exists()",
        BooleanType.class, "urn:dktk:dataelement:50:2", PrimitiveType::getValueAsString);

    builder.addAttributeOptional(
        "Specimen.type.coding.where(system = '" + SAMPLE_MATERIAL_TYPE + "').code",
        CodeType.class, "urn:dktk:dataelement:95:2",
        code -> mapSpecimenTypeValue(code.getCode()));

    builder.addAttributeOptional(
        "Specimen.type.coding.where(system = '" + SAMPLE_MATERIAL_TYPE + "').code",
        CodeType.class, "urn:dktk:dataelement:90:1",
        code -> mapSpecimenFirmingTypeValue(code.getCode()));

    builder.addAttributeOptional("Specimen.collection.collected", DateTimeType.class,
        "urn:dktk:dataelement:49:4", DATE_STRING);

    return builder.build();
  }

  private static Optional<String> mapSpecimenTypeValue(String typeValue) {
    return switch (typeValue) {
      case "tumor-tissue-ffpe", "normal-tissue-ffpe", "other-tissue-ffpe", "tissue-ffpe",
          "tissue-frozen", "tumor-tissue-frozen", "normal-tissue-frozen", "other-tissue-frozen",
          "tissue-other" -> Optional.of("Gewebeprobe");
      case "whole-blood", "blood-plasma", "plasma-edta", "plasma-citrat", "plasma-heparin",
          "plasma-cell-free", "plasma-other", "urine", "csf-liquor", "blood-serum",
          "liquid-other" -> Optional.of("Flüssigprobe");
      default -> Optional.empty();
    };
  }

  private static String mapSpecimenKindValue(String code) {
    return switch (code) {
      case "whole-blood" -> "Vollblut";
      case "bone-marrow" -> "Knochenmark";
      case "blood-plasma", "plasma-edta", "plasma-citrat", "plasma-heparin", "plasma-cell-free",
          "plasma-other" -> "Plasma";
      case "blood-serum" -> "Serum";
      case "csf-liquor", "liquid-other" -> "Liquor";
      case "urine" -> "Urin";
      case "tumor-tissue-ffpe", "tumor-tissue-frozen" -> "Tumorgewebe";
      case "normal-tissue-ffpe", "normal-tissue-frozen", "tissue-ffpe", "tissue-frozen",
          "other-tissue-frozen", "other-tissue-ffpe", "other-tissue" -> "Normalgewebe";
      case "dna", "cf-dna", "g-dna" -> "DNA";
      case "rna" -> "RNA";
      //TODO: find value for protein
      // case "" -> "Protein";
      default -> code;
    };
  }

  private static Optional<String> mapSpecimenFirmingTypeValue(String typeValue) {
    if (typeValue.contains("ffpe")) {
      return Optional.of("Paraffin (FFPE)");
    } else if (typeValue.contains("frozen") || typeValue.equals("blood-plasma")) {
      return Optional.of("Kryo/Frisch (FF)");
    } else {
      return Optional.empty();
    }
  }
}
