package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping.ICD_O_3;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Alexander Kiel
 */
class DiagnosisMappingTest {

  private static final String ICD_10_GM = "http://fhir.de/CodeSystem/dimdi/icd-10-gm";

  private DiagnosisMapping mapping;
  private Patient pa;

  @BeforeEach
  void setUp() {
    pa = new Patient();
    pa.setBirthDateElement(new DateType("2000-01-01"));

   // DiagnosisMapping = new DiagnosisMapping();

  }

  @Test
  void map_conditionCode() {
    var condition = new Condition();
    condition.getCode().getCodingFirstRep().setSystem(ICD_10_GM).setCode("G24.1");

    var container = mapping.map(condition,pa);

    var attribute = container.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:29:2", attribute.getMdrKey());
    assertEquals("G24.1", attribute.getValue().getValue());
  }

  @Test
  void map_conditionDate() {
    var condition = new Condition();
    condition.setOnset(new DateTimeType("2010-01-01T01:02:03+02:00"));

    var container = mapping.map(condition, pa);

    var attribute = container.getAttribute().stream()
        .filter(a -> "urn:dktk:dataelement:83:3".equals(a.getMdrKey())).findFirst();
    assertEquals("01.01.2010", attribute.get().getValue().getValue());
    attribute =  container.getAttribute().stream()
        .filter(a -> "urn:dktk:dataelement:28:1".equals(a.getMdrKey())).findFirst();
    assertEquals("10", attribute.get().getValue().getValue());
  }

  @Test
  void map_onsetAge() {
    var condition = new Condition();
    condition.getOnsetAge().setValue(9);

    var container = mapping.map(condition, pa);

    var attribute =  container.getAttribute().stream()
        .filter(a -> "urn:dktk:dataelement:28:1".equals(a.getMdrKey())).findFirst();
    assertEquals("9", attribute.get().getValue().getValue());
  }

  @Test
  void map_localization() {
    var condition = new Condition();
    condition.getBodySiteFirstRep().getCodingFirstRep().setSystem(ICD_O_3).setCode("C12.1");

    var container = mapping.map(condition, pa);

    var attribute = container.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:4:2", attribute.getMdrKey());
    assertEquals("C12.1", attribute.getValue().getValue());
  }

  @Test
  void map_ICDVersion() {
    var condition = new Condition();
    condition.getBodySiteFirstRep().getCodingFirstRep().setSystem(ICD_O_3).setVersion("2014");

    var container = mapping.map(condition, pa);

    var attribute = container.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:3:2", attribute.getMdrKey());
    assertEquals("10 2014 GM", attribute.getValue().getValue());
  }

  @Test
  void map_diagnosisYear() {
    var condition = new Condition();
    condition.setRecordedDate(new DateTimeType("2010-01-01").getValue());

    var container = mapping.map(condition, pa);

    assertEquals(Optional.of("01.01.2010"),
        findAttributeValue(container, "urn:dktk:dataelement:83:3"));
  }

  @Test
  void map_yearAndAge() {
    var condition = new Condition();
    condition.setRecordedDate(new DateTimeType("2010-01-01").getValue());
    condition.getOnsetAge().setValue(9);

    var container = mapping.map(condition, pa);

    assertEquals(Optional.of("01.01.2010"),
        findAttributeValue(container, "urn:dktk:dataelement:83:3"));
    assertEquals(Optional.of("9"),
        findAttributeValue(container, "urn:dktk:dataelement:28:1"));
  }
}
