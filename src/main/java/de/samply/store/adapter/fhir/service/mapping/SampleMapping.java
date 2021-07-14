package de.samply.store.adapter.fhir.service.mapping;


import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.share.model.ccp.ObjectFactory;
import java.util.Optional;
import java.util.function.Function;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Specimen;
import org.springframework.stereotype.Component;

/**
 * Mapping of the Sample container.
 *
 * @author Patrick Skowronek
 */

@Component
public class SampleMapping {

  private static final String samplePath = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType";

  private final ObjectFactory objectFactory = new ObjectFactory();
  private final FhirPathR4 fhirPathR4;

  public SampleMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  public Container map(Specimen specimen) {
    var builder = new ContainerBuilder(fhirPathR4, specimen, "Sample");

    builder.addAttribute(
        "Specimen.type.coding.where(system = '" + samplePath + "').code",
        CodeType.class, "urn:dktk:dataelement:95:2",
        MAP_SPECIMEN_KIND_VALUE.compose(PrimitiveType::getValue));

    builder.addAttributeOptional(
        "Specimen.type.coding.where(system = '" + samplePath + "').code",
        CodeType.class, "urn:dktk:dataelement:97:1",
        MAP_SPECIMEN_TYPE_VALUE.compose(PrimitiveType::getValue));

    builder.addAttributeOptional("Specimen.type.coding.where(system = '" + samplePath + "').code",
        CodeType.class, "urn:dktk:dataelement:90:1",
        MAP_SPECIMEN_FIRMING_TYPE_VALUE.compose(PrimitiveType::getValue));

    builder.addAttributeOptional("Specimen.collection.collected", DateTimeType.class,
        "urn:dktk:dataelement:49:4", DATE_STRING);

    return builder.build();
  }

  private static final Function<String, Optional<String>> MAP_SPECIMEN_TYPE_VALUE = typeValue ->
      switch (typeValue) {
        case "tumor-tissue-ffpe", "normal-tissue-ffpe", "other-tissue-ffpe", "tissue-ffpe", "tissue-frozen", "tumor-tissue-frozen", "normal-tissue-frozen", "other-tissue-frozen", "tissue-other" -> Optional
            .of("Gewebeprobe");
        case "whole-blood", "blood-plasma", "plasma-edta", "plasma-citrat", "plasma-heparin", "plasma-cell-free",
            "plasma-other", "urine", "csf-liquor", "blood-serum", "liquid-other" -> Optional
            .of("Flüssigprobe");
        default -> Optional.empty();
      };

  private static final Function<String, String> MAP_SPECIMEN_KIND_VALUE = typeValue -> switch (typeValue) {
    case "whole-blood" -> "Vollblut";
    case "bone-marrow" -> "Knochenmark";
    case "blood-plasma", "plasma-edta", "plasma-citrat", "plasma-heparin", "plasma-cell-free", "plasma-other" -> "Plasma";
    case "blood-serum" -> "Serum";
    case "csf-liquor", "liquid-other" -> "Liquor";
    case "urine" -> "Urin";
    case "tumor-tissue-ffpe", "tumor-tissue-frozen" -> "Tumorgewebe";
    case "normal-tissue-ffpe", "normal-tissue-frozen", "tissue-ffpe", "tissue-frozen", "other-tissue-frozen", "other-tissue-ffpe", "other-tissue" -> "Normalgewebe";
    case "dna", "cf-dna", "g-dna" -> "DNA";
    case "rna" -> "RNA";
    //TODO: find value for protein
    // case "" -> "Protein";
    default -> typeValue;
  };

  private static String mapTypeValue(String type) {
    return type.contains("tissue") ? "Gewebeprobe" : "";
  }

  private static final Function<String, Optional<String>> MAP_SPECIMEN_FIRMING_TYPE_VALUE = typeValue -> {
    if (typeValue.contains("ffpe")) {
      return Optional.of("Paraffin (FFPE)");
    } else if (typeValue.contains("frozen")) {
      return Optional.of("Kryo/Frisch (FF)");
    } else {
      return Optional.empty();
    }
  };

}
