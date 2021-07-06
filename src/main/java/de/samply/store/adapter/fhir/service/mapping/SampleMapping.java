package de.samply.store.adapter.fhir.service.mapping;


import de.samply.share.model.ccp.Container;
import de.samply.share.model.ccp.ObjectFactory;
import java.util.function.Function;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
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


  private final ObjectFactory objectFactory = new ObjectFactory();
  private final FhirPathR4 fhirPathR4;

  public SampleMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  public Container map(Specimen specimen) {
    var builder = new ContainerBuilder(fhirPathR4, specimen, "Sample");

    builder.addAttribute(
        "Specimen.type.coding.where(system = 'https://fhir.bbmri.de/CodeSystem/SampleMaterialType').code",
        CodeType.class, "urn:dktk:dataelement:97:1",
        MAP_TYPE_VALUE.compose(PrimitiveType::getValue));

    return builder.build();
  }

  private static final Function<String, String> MAP_TYPE_VALUE = typeValue -> switch (typeValue) {
    case "whole-blood" -> "Vollblut";
    case "tissue-ffpe" -> "Tumorgewebe";
    case "tissue-other" -> "Normalgewebe";
    case "blood-serum" -> "Serum";
    case "blood-plasma" -> "Plasma";
    case "urine" -> "Urin";
    case "liquid-other" -> "Liquor";
    case "bone-marrow" -> "Knochenmark";
    case "dna" -> "DNA";
    case "rna" -> "RNA";
    //TODO: find value for protein
    // case "" -> "Protein";
    default -> throw new IllegalArgumentException("Not mapable value " + typeValue);
  };

}
