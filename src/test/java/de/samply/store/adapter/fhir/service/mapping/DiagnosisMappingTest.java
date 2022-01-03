package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttrValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.model.ConditionNode;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiagnosisMappingTest {

  private static final String ICD_10_GM = "http://fhir.de/CodeSystem/dimdi/icd-10-gm";

  @Mock(lenient = true)
  private FhirPathR4 fhirPathEngine;

  @Mock
  private TumorMapping tumorMapping;

  private DiagnosisMapping mapping;

  private Patient patient;

  @BeforeEach
  void setUp() {
    patient = new Patient();
    patient.setBirthDateElement(new DateType("2000-01-01"));

    mapping = new DiagnosisMapping(fhirPathEngine, tumorMapping);
  }

  @Test
  void map_conditionCode() {
    var condition = new Condition();
    condition.getCode().getCodingFirstRep().setSystem(ICD_10_GM).setCode("G24.1");
    var conditionNode = new ConditionNode(patient, condition);
    when(fhirPathEngine.evaluateFirst(condition,
        "Condition.code.coding.where(system = '" + ICD_10_GM + "').code",
        CodeType.class)).thenReturn(Optional.of(new CodeType("G24.1")));

    var container = mapping.map(conditionNode);

    var attribute = container.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:29:2", attribute.getMdrKey());
    assertEquals("G24.1", attribute.getValue().getValue());
  }

  @Test
  void map_conditionDate() {
    var condition = new Condition();
    condition.setOnset(new DateTimeType("2010-01-01T01:02:03+02:00"));
    var conditionNode = new ConditionNode(patient, condition);
    when(fhirPathEngine.evaluateFirst(condition,
        "Condition.onset",
        DateTimeType.class)).thenReturn(Optional.of(new DateTimeType("2010-01-01T01:02:03+02:00")));

    var container = mapping.map(conditionNode);

    var attribute = container.getAttribute().stream()
        .filter(a -> "urn:dktk:dataelement:83:3".equals(a.getMdrKey())).findFirst();
    assertEquals("01.01.2010", attribute.orElseThrow().getValue().getValue());
    attribute = container.getAttribute().stream()
        .filter(a -> "urn:dktk:dataelement:28:1".equals(a.getMdrKey())).findFirst();
    assertEquals("10", attribute.orElseThrow().getValue().getValue());
  }

  @Test
  void map_onsetAge() {
    var condition = new Condition();
    condition.getOnsetAge().setValue(9);
    var conditionNode = new ConditionNode(patient, condition);
    when(fhirPathEngine.evaluateFirst(condition,
        "Condition.onset.value",
        DecimalType.class)).thenReturn(Optional.of(new DecimalType(9)));

    var container = mapping.map(conditionNode);

    var attribute = container.getAttribute().stream()
        .filter(a -> "urn:dktk:dataelement:28:1".equals(a.getMdrKey())).findFirst();
    assertEquals("9", attribute.orElseThrow().getValue().getValue());
  }

  @Test
  void map_ICDVersion() {
    var condition = new Condition();
    condition.getCode().getCodingFirstRep().setSystem(ICD_10_GM).setVersion("2014").setCode("C61");
    var conditionNode = new ConditionNode(patient, condition);
    when(fhirPathEngine.evaluateFirst(condition,
        "Condition.code.coding.where(system = '" + ICD_10_GM + "').version",
        StringType.class)).thenReturn(Optional.of(new StringType("2014")));

    var container = mapping.map(conditionNode);

    var attribute = container.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:3:2", attribute.getMdrKey());
    assertEquals("10 2014 GM", attribute.getValue().getValue());
  }

  @Test
  void map_diagnosisYear() {
    var condition = new Condition();
    condition.setRecordedDate(new DateTimeType("2010-01-01").getValue());
    var conditionNode = new ConditionNode(patient, condition);
    when(fhirPathEngine.evaluateFirst(condition, "Condition.recordedDate", DateTimeType.class))
        .thenReturn(Optional.of(new DateTimeType("2010-01-01")));

    var container = mapping.map(conditionNode);

    assertEquals(Optional.of("01.01.2010"), findAttrValue(container, "83:3"));
  }

  @Test
  void map_yearAndAge() {
    var condition = new Condition();
    condition.setRecordedDate(new DateTimeType("2010-01-01").getValue());
    condition.getOnsetAge().setValue(9);
    var conditionNode = new ConditionNode(patient, condition);
    when(fhirPathEngine.evaluateFirst(condition, "Condition.recordedDate", DateTimeType.class))
        .thenReturn(Optional.of(new DateTimeType("2010-01-01")));
    when(fhirPathEngine.evaluateFirst(condition, "Condition.onset.value", DecimalType.class))
        .thenReturn(Optional.of(new DecimalType(9)));

    var container = mapping.map(conditionNode);

    assertEquals(Optional.of("01.01.2010"), findAttrValue(container, "83:3"));
    assertEquals(Optional.of("9"), findAttrValue(container, "28:1"));
  }

  @Test
  void map_tumor() {
    var condition = new Condition();
    var conditionNode = new ConditionNode(patient, condition);
    Container tumor = new Container();
    when(tumorMapping.map(conditionNode)).thenReturn(tumor);

    var container = mapping.map(conditionNode);

    assertEquals(List.of(tumor), container.getContainer());
  }
}
