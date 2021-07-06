package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.MappingService.ICD_10_GM;
import static de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping.ICD_O_3;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Alexander Kiel
 */
class DiagnosisMappingTest {

  private DiagnosisMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new DiagnosisMapping(new FhirPathR4(FhirContext.forR4()));
  }

  @Test
  void map_conditionCode() {
    var condition = new Condition();
    condition.getCode().getCodingFirstRep().setSystem(ICD_10_GM).setCode("G24.1");

    var container = mapping.map(condition);

    var attribute = container.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:29:2", attribute.getMdrKey());
    assertEquals("G24.1", attribute.getValue().getValue());
  }

  @Test
  void map_conditionDate() {
    var condition = new Condition();
    condition.setOnset(new DateTimeType("1970-01-01T01:02:03+02:00"));

    var container = mapping.map(condition);

    var attribute = container.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:83:3", attribute.getMdrKey());
    assertEquals("01.01.1970", attribute.getValue().getValue());
  }

  @Test
  void map_localization() {
    var condition = new Condition();
    condition.getBodySiteFirstRep().getCodingFirstRep().setSystem(ICD_O_3).setCode("C12.1");

    var container = mapping.map(condition);

    var attribute = container.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:4:2", attribute.getMdrKey());
    assertEquals("C12.1", attribute.getValue().getValue());
  }
}
