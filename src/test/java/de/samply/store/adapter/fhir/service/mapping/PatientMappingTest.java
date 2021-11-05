package de.samply.store.adapter.fhir.service.mapping;

import static org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirContext;
import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.model.ConditionNode;
import de.samply.store.adapter.fhir.model.PatientNode;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientMappingTest {

  private static final String LOCAL_ID = "201158";
  private static final String GLOBAL_ID = "201117";
  private static final String LOINC = "http://loinc.org";
  private static final String PSEUDONYM_ART_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS";
  private static final String VITAL_STATE_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS";

  private static final FhirContext fhirContext = FhirContext.forR4();

  @Mock
  private DiagnosisMapping diagnosisMapping;

  @Mock
  private SampleMapping sampleMapping;

  private PatientMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new PatientMapping(new FhirPathR4(fhirContext, new EvaluationContext()),
        diagnosisMapping, sampleMapping);
  }

  @Test
  void map_emptyPatient() {
    var result = mapping.map(createPatientNode());

    assertEquals(LOCAL_ID, result.getId());
  }

  @Test
  void map_globalId() {
    var result = mapping.map(createPatientNode(p -> setIdentifier(p, "Global", GLOBAL_ID)));

    var firstAttribute = result.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:54:1", firstAttribute.getMdrKey());
    assertEquals(GLOBAL_ID, firstAttribute.getValue().getValue());
  }

  @Test
  void map_genderMale() {
    var node = createPatientNode(p -> p.setGender(MALE));

    var result = mapping.map(node);

    var firstAttribute = result.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:1:3", firstAttribute.getMdrKey());
    assertEquals("M", firstAttribute.getValue().getValue());
  }

  @Test
  void map_birthDate() {
    var node = createPatientNode(p -> p.setBirthDateElement(new DateType("2020-05-10")));

    var result = mapping.map(node);

    var firstAttribute = result.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:26:4", firstAttribute.getMdrKey());
    assertEquals("10.05.2020", firstAttribute.getValue().getValue());
  }

  @Test
  void map_vitalStatus() {
    var vital = new Observation();
    vital.getCode().getCodingFirstRep().setSystem(LOINC).setCode("75186-7");
    vital.getValueCodeableConcept().getCodingFirstRep().setSystem(VITAL_STATE_CS).setCode("lebend");
    var node = createPatientNode(vital);

    var result = mapping.map(node);

    var firstAttribute = result.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:53:3", firstAttribute.getMdrKey());
    assertEquals("lebend", firstAttribute.getValue().getValue());
  }

  @Test
  void map_vitalStatusDate() {
    var vital = new Observation();
    vital.getCode().getCodingFirstRep().setSystem(LOINC).setCode("75186-7");
    vital.setEffective(new DateTimeType("2027-10-02"));
    var node = createPatientNode(vital);

    var result = mapping.map(node);

    var firstAttribute = result.getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:48:3", firstAttribute.getMdrKey());
    assertEquals("02.10.2027", firstAttribute.getValue().getValue());
  }

  @Test
  void map_oneCondition() {
    var patient = createPatient();
    var condition = new Condition();
    condition.setId("123");
    var conditionNode = new ConditionNode(patient, condition);
    var node = new PatientNode(patient, Optional.empty(), List.of(conditionNode), List.of());
    var diagnosis = new Container();
    when(diagnosisMapping.map(conditionNode)).thenReturn(diagnosis);

    var result = mapping.map(node);

    assertEquals(diagnosis, result.getContainer().get(0));
  }

  @Test
  void map_twoConditions() {
    var patient = createPatient();
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
    var patient = createPatient();
    var specimen = new Specimen();
    var node = new PatientNode(patient, Optional.empty(), List.of(), List.of(specimen));
    var sample = new Container();
    when(sampleMapping.map(specimen)).thenReturn(sample);

    var result = mapping.map(node);

    assertEquals(sample, result.getContainer().get(0));
  }

  @Test
  void map_twoSample() {
    var patient = createPatient();
    var specimen1 = new Specimen();
    var specimen2 = new Specimen();
    var node = new PatientNode(patient, Optional.empty(), List.of(), List.of(specimen1, specimen2));
    var sample1 = new Container();
    var sample2 = new Container();
    when(sampleMapping.map(specimen1)).thenReturn(sample1);
    when(sampleMapping.map(specimen2)).thenReturn(sample2);

    var result = mapping.map(node);

    assertEquals(List.of(sample1, sample2), result.getContainer());
  }

  private static PatientNode createPatientNode() {
    return new PatientNode(createPatient());
  }

  private static PatientNode createPatientNode(Consumer<Patient> patientBuilder) {
    Patient patient = createPatient();
    patientBuilder.accept(patient);
    return new PatientNode(patient);
  }

  private static Patient createPatient() {
    var patient = new Patient();
    setIdentifier(patient, "Lokal", LOCAL_ID);
    return patient;
  }

  private static void setIdentifier(Patient patient, String type, String value) {
    var localId = patient.addIdentifier();
    localId.getType().getCodingFirstRep().setSystem(PSEUDONYM_ART_CS).setCode(type);
    localId.setValue(value);
  }

  private static PatientNode createPatientNode(Observation vitalState) {
    return new PatientNode(createPatient(), vitalState);
  }
}
