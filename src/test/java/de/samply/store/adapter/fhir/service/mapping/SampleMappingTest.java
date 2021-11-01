package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttrValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Optional;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class SampleMappingTest {

  private static final String SAMPLE_MATERIAL_TYPE = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType";

  private static final FhirContext fhirContext = FhirContext.forR4();

  private SampleMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new SampleMapping(new FhirPathR4(fhirContext, new EvaluationContext()));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/sampleMappings.csv", numLinesToSkip = 1)
  void map_sampleMaterialTypeCSVFile(
      String bbmriType, String cxxCode, String dktkProbenart, String dktkProbentyp,
      String dktkFixierungsart, String preserved
  ) {
    var specimen = new Specimen();
    specimen.getType().addCoding().setSystem(SAMPLE_MATERIAL_TYPE).setCode(bbmriType);
    if (cxxCode != null) {
      specimen.getType().addCoding().setSystem("urn:centraxx").setCode(cxxCode);
    }

    var container = mapping.map(specimen);

    assertEquals(Optional.ofNullable(dktkProbenart), findAttrValue(container, "97:1"));
    assertEquals(Optional.ofNullable(dktkProbentyp), findAttrValue(container, "95:2"));
    assertEquals(Optional.ofNullable(dktkFixierungsart), findAttrValue(container, "90:1"));
    assertEquals(Optional.ofNullable(preserved), findAttrValue(container, "50:2"));
  }

  @Test
  void map_collectedDateTime() {
    var specimen = new Specimen();
    specimen.getCollection().setCollected(new DateTimeType("2000-01-01"));

    var container = mapping.map(specimen);

    assertEquals(Optional.of("01.01.2000"), findAttrValue(container, "49:4"));
  }
}
