package de.samply.store.adapter.fhir.service.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.model.ConditionNode;
import de.samply.store.adapter.fhir.model.PatientNode;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGenderEnumFactory;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PatientMappingTest {

  private static final String PATIENT_ID = "123";
  private static final String LOINC = "http://loinc.org";
  private static final Enumeration<AdministrativeGender> MALE = new AdministrativeGenderEnumFactory().fromType(
      new CodeType("male"));

  @Mock(lenient = true)
  private FhirPathR4 fhirPathEngine;

  @Mock
  private DiagnosisMapping diagnosisMapping;

  @Mock
  private SampleMapping sampleMapping;

  private PatientMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new PatientMapping(fhirPathEngine, diagnosisMapping, sampleMapping);
  }

  @Test
  void map_withEmptyPatient() {
    var result = mapping.map(createPatientNode());

    assertEquals(PATIENT_ID, result.getId());
  }

  @Test
  void map_genderMale() {
    var node = createPatientNode();
    when(fhirPathEngine.evaluateFirst(node.patient(), "Patient.gender", Enumeration.class))
        .thenReturn(Optional.of(MALE));

    var result = mapping.map(node);

    var firstAttribute = result.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:1:3", firstAttribute.getMdrKey());
    assertEquals("M", firstAttribute.getValue().getValue());
  }

  @Test
  void map_birthDate() {
    var node = createPatientNode();
    when(fhirPathEngine.evaluateFirst(node.patient(), "Patient.birthDate",
        DateType.class)).thenReturn(Optional.of(new DateType("2020-05-10")));

    var result = mapping.map(node);

    var firstAttribute = result.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:26:4", firstAttribute.getMdrKey());
    assertEquals("10.05.2020", firstAttribute.getValue().getValue());
  }

  @Test
  void map_vitalStatus() {
    Observation vital = new Observation();
    vital.getCode().getCodingFirstRep().setSystem(LOINC).setCode("75186-7");
    var node = createPatientNode(vital);
    when(fhirPathEngine.evaluateFirst(vital,
        "Observation.value.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS').code",
        CodeType.class)).thenReturn(Optional.of(new CodeType("lebend")));

    var result = mapping.map(node);

    var firstAttribute = result.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:53:3", firstAttribute.getMdrKey());
    assertEquals("lebend", firstAttribute.getValue().getValue());
  }

  @Test
  void map_vitalStatusDate() {
    Observation vital = new Observation();
    vital.getCode().getCodingFirstRep().setSystem(LOINC).setCode("75186-7");
    var node = createPatientNode(vital);
    when(fhirPathEngine.evaluateFirst(vital, "Observation.effective",
        DateTimeType.class)).thenReturn(Optional.of(new DateTimeType("2027-10-02")));

    var result = mapping.map(node);

    var firstAttribute = result.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:48:3", firstAttribute.getMdrKey());
    assertEquals("02.10.2027", firstAttribute.getValue().getValue());
  }

  @Test
  void map_oneCondition() {
    Patient patient = new Patient();
    patient.setId(PATIENT_ID);
    var condition = new Condition();
    condition.setId("123");
    var conditionNode = new ConditionNode(patient, condition);
    var node = new PatientNode(patient, Optional.empty(), List.of(conditionNode),
        List.of());
    var diagnosis = new Container();
    when(diagnosisMapping.map(conditionNode)).thenReturn(diagnosis);

    var result = mapping.map(node);

    assertEquals(diagnosis, result.getContainer().get(0));
  }

  @Test
  void map_twoConditions() {
    Patient patient = new Patient();
    patient.setId(PATIENT_ID);
    var condition1 = new Condition();
    condition1.setId("123");
    var condition2 = new Condition();
    condition2.setId("456");
    var conditionNode1 = new ConditionNode(patient, condition1);
    var conditionNode2 = new ConditionNode(patient, condition2);
    var node = new PatientNode(patient, Optional.empty(), List.of(conditionNode1, conditionNode2),
        List.of());
    var diagnosis1 = new Container();
    var diagnosis2 = new Container();
    when(diagnosisMapping.map(conditionNode1)).thenReturn(diagnosis1);
    when(diagnosisMapping.map(conditionNode2)).thenReturn(diagnosis2);

    var result = mapping.map(node);

    assertEquals(List.of(diagnosis1, diagnosis2), result.getContainer());
  }

  @Test
  void map_oneSample() {
    Patient patient = new Patient();
    patient.setId(PATIENT_ID);
    var specimen = new Specimen();
    var node = new PatientNode(patient, Optional.empty(), List.of(), List.of(specimen));
    Container sample = new Container();
    when(sampleMapping.map(specimen)).thenReturn(sample);

    var result = mapping.map(node);

    assertEquals(sample, result.getContainer().get(0));
  }

  @Test
  void map_twoSample() {
    Patient patient = new Patient();
    patient.setId(PATIENT_ID);
    var specimen1 = new Specimen();
    var specimen2 = new Specimen();
    var node = new PatientNode(patient, Optional.empty(), List.of(), List.of(specimen1, specimen2));
    Container sample1 = new Container();
    Container sample2 = new Container();
    when(sampleMapping.map(specimen1)).thenReturn(sample1);
    when(sampleMapping.map(specimen2)).thenReturn(sample2);

    var result = mapping.map(node);

    assertEquals(List.of(sample1, sample2), result.getContainer());
  }

  private static PatientNode createPatientNode() {
    Patient patient = new Patient();
    patient.setId(PATIENT_ID);
    return new PatientNode(patient);
  }

  private static PatientNode createPatientNode(Observation vitalState) {
    Patient patient = new Patient();
    patient.setId(PATIENT_ID);
    return new PatientNode(patient, vitalState);
  }
}
