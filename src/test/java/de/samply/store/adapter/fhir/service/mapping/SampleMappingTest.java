package de.samply.store.adapter.fhir.service.mapping;

import ca.uhn.fhir.context.FhirContext;
import java.util.Optional;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static de.samply.store.adapter.fhir.service.TestUtil.convertCsvValue;
import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Patrick Skowronek
 */

class SampleMappingTest {

  private SampleMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new SampleMapping(new FhirPathR4(FhirContext.forR4()));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/sampleMappings.csv", numLinesToSkip = 1)
  void toUpperCase_ShouldGenerateTheExpectedUppercaseValueCSVFile(
      String fhir_sample, String kind_value, String type_value, String fixing_value
  ) {
    var specimen = new Specimen();
    specimen.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode(fhir_sample);

    var container = mapping.map(specimen);

    assertEquals(convertCsvValue(kind_value),
        findAttributeValue(container, "urn:dktk:dataelement:95:2"));
    assertEquals(convertCsvValue(type_value),
        findAttributeValue(container, "urn:dktk:dataelement:97:1"));
    assertEquals(convertCsvValue(fixing_value),
        findAttributeValue(container, "urn:dktk:dataelement:90:1"));
  }

  @Test
  void map_specimenTypeWholeBlood() {
    var specimen = new Specimen();
    specimen.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode("whole-blood");

    var container = mapping.map(specimen);

    assertEquals(Optional.of("Vollblut"),
        findAttributeValue(container, "urn:dktk:dataelement:95:2"));
    assertEquals(Optional.of("Flüssigprobe"),
        findAttributeValue(container, "urn:dktk:dataelement:97:1"));
    assertTrue(findAttributeValue(container, "urn:dktk:dataelement:90:1").isEmpty());
  }

  @Test
  void map_specimenTypeTissueFfpe() {
    var specimen = new Specimen();
    specimen.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode("tissue-ffpe");

    var container = mapping.map(specimen);

    assertEquals(Optional.of("Tumorgewebe"),
        findAttributeValue(container, "urn:dktk:dataelement:95:2"));
    assertEquals(Optional.of("Gewebeprobe"),
        findAttributeValue(container, "urn:dktk:dataelement:97:1"));
    assertEquals(Optional.of("Paraffin (FFPE)"),
        findAttributeValue(container, "urn:dktk:dataelement:90:1"));
  }

  @Test
  void map_specimenTypeTissueFrozen() {
    var specimen = new Specimen();
    specimen.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType")
        .setCode("tumor-tissue-frozen");

    var container = mapping.map(specimen);

    assertEquals(Optional.of("Tumorgewebe"),
        findAttributeValue(container, "urn:dktk:dataelement:95:2"));
    assertEquals(Optional.of("Gewebeprobe"),
        findAttributeValue(container, "urn:dktk:dataelement:97:1"));
    assertEquals(Optional.of("Kryo/Frisch (FF)"),
        findAttributeValue(container, "urn:dktk:dataelement:90:1"));
  }

  @Test
  void map_specimenTypePlasmaEdta() {
    var specimen = new Specimen();
    specimen.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode("plasma-edta");

    var container = mapping.map(specimen);

    assertEquals(Optional.of("Plasma"), findAttributeValue(container, "urn:dktk:dataelement:95:2"));
    assertEquals(Optional.of("Flüssigprobe"),
        findAttributeValue(container, "urn:dktk:dataelement:97:1"));
    assertTrue(findAttributeValue(container, "urn:dktk:dataelement:90:1").isEmpty());
  }

  @Test
  void map_collectedDateTime() {
    var specimen = new Specimen();
    specimen.getCollection().setCollected(new DateTimeType("2000-01-01"));

    var container = mapping.map(specimen);

    assertEquals(Optional.of("01.01.2000"),
        findAttributeValue(container, "urn:dktk:dataelement:49:4"));
  }


  @Test
  void map_specimenTypeError() {
    var specimen = new Specimen();
    specimen.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType")
        .setCode("derivative-other");

    Exception exception = assertThrows(IllegalArgumentException.class, () -> mapping.map(specimen));

    String expectedMessage = "Not mapable value ";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }


}