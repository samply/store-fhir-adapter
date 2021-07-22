package de.samply.store.adapter.fhir.service.mapping;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.MyIEvaluationContext;
import java.util.Optional;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Patrick Skowronek
 */
class SampleMappingTest {

  private SampleMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new SampleMapping(new FhirPathR4(FhirContext.forR4(), new MyIEvaluationContext()));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/sampleMappings.csv", numLinesToSkip = 1)
  void map_sampleMaterialTypeCSVFile(
      String fhirSample, String kindValue, String typeValue, String fixingValue
  ) {
    var specimen = new Specimen();
    specimen.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode(fhirSample);

    var container = mapping.map(specimen);

    assertEquals(Optional.ofNullable(kindValue),
        findAttributeValue(container, "urn:dktk:dataelement:95:2"));
    assertEquals(Optional.ofNullable(typeValue),
        findAttributeValue(container, "urn:dktk:dataelement:97:1"));
    assertEquals(Optional.ofNullable(fixingValue),
        findAttributeValue(container, "urn:dktk:dataelement:90:1"));
  }

  @Test
  void map_collectedDateTime() {
    var specimen = new Specimen();
    specimen.getCollection().setCollected(new DateTimeType("2000-01-01"));

    var container = mapping.map(specimen);

    assertEquals(Optional.of("01.01.2000"),
        findAttributeValue(container, "urn:dktk:dataelement:49:4"));
  }
}