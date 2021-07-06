package de.samply.store.adapter.fhir.service.mapping;

import ca.uhn.fhir.context.FhirContext;
import javax.naming.directory.InvalidAttributesException;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  @Test
  void map_specimenType() {
    var specimen = new Specimen();
    specimen.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode("whole-blood");

    var container = mapping.map(specimen);

    var attribute = container.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:97:1", attribute.getMdrKey());
    assertEquals("Vollblut", attribute.getValue().getValue());
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