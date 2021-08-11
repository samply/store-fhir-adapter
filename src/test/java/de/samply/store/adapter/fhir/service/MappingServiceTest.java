package de.samply.store.adapter.fhir.service;

import static org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirContext;
import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping;
import de.samply.store.adapter.fhir.service.mapping.MetastasisMapping;
import de.samply.store.adapter.fhir.service.mapping.RadiationTherapyMapping;
import de.samply.store.adapter.fhir.service.mapping.SampleMapping;
import de.samply.store.adapter.fhir.service.mapping.SurgeryMapping;
import de.samply.store.adapter.fhir.service.mapping.SystemTherapyMapping;
import de.samply.store.adapter.fhir.service.mapping.TNMMapping;
import de.samply.store.adapter.fhir.service.mapping.TumorMapping;
import java.util.function.Consumer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Alexander Kiel
 */
@ExtendWith(MockitoExtension.class)
class MappingServiceTest {

  public static final String PATIENT_ID = "id-193956";

  @Mock
  private DiagnosisMapping diagnosisMapping;
  @Mock
  private SampleMapping sampleMapping;
  @Mock
  private MetastasisMapping metastasisMapping;
  @Mock
  private SurgeryMapping surgeryMapping;
  @Mock
  private TNMMapping tnmMapping;
  @Mock
  private TumorMapping tumorMapping;
  @Mock
  private RadiationTherapyMapping radiationTherapyMapping;
  @Mock
  private SystemTherapyMapping systemTherapyMapping;

  private MappingService service;

  @BeforeEach
  void setUp() {
    service = new MappingService(FhirContext.forR4(), diagnosisMapping, sampleMapping,
        metastasisMapping, surgeryMapping, tnmMapping, tumorMapping, radiationTherapyMapping,
        systemTherapyMapping);
  }

  @Test
  void map() {
    var result = service.map(new Bundle());

    assertTrue(result.getPatient().isEmpty());
  }

  @Test
  void map_withOnePatient() {
    var bundle = new Bundle();
    bundle.getEntry().add(createPatientEntry());

    var result = service.map(bundle);

    assertEquals(PATIENT_ID, result.getPatient().get(0).getId());
  }

  @Test
  void map_genderMale() {
    var bundle = new Bundle();
    bundle.getEntry().add(createPatientEntry(p -> p.setGender(MALE)));

    var result = service.map(bundle);

    var firstAttribute = result.getPatient().get(0).getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:1:3", firstAttribute.getMdrKey());
    assertEquals("M", firstAttribute.getValue().getValue());
  }

  @Test
  void map_birthDate() {
    var bundle = new Bundle();
    bundle.getEntry()
        .add(createPatientEntry(p -> p.setBirthDateElement(new DateType("2020-05-10"))));

    var result = service.map(bundle);

    var firstAttribute = result.getPatient().get(0).getAttribute().get(0);

    assertEquals("urn:dktk:dataelement:26:4", firstAttribute.getMdrKey());
    assertEquals("10.05.2020", firstAttribute.getValue().getValue());
  }

  @Test
  void map_vitalStatus() {
    var bundle = new Bundle();
    var entry = bundle.getEntry();
    entry.add(createPatientEntry());
    entry.add(createObservationEntry(
        o -> {
          o.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode("75186-7");
          o.getValueCodeableConcept().getCodingFirstRep()
              .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS")
              .setCode("lebend");
        }));

    var result = service.map(bundle);

    var firstAttribute = result.getPatient().get(0).getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:53:3", firstAttribute.getMdrKey());
    assertEquals("lebend", firstAttribute.getValue().getValue());
  }

  @Test
  void map_vitalStatusDate() {
    var bundle = new Bundle();
    var entry = bundle.getEntry();
    entry.add(createPatientEntry());
    entry.add(createObservationEntry(
        o -> {
          o.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode("75186-7");
          o.setEffective(new DateTimeType("2027-10-02"));
        }));

    var result = service.map(bundle);

    var firstAttribute = result.getPatient().get(0).getAttribute().get(0);
    assertEquals("urn:dktk:dataelement:48:3", firstAttribute.getMdrKey());
    assertEquals("02.10.2027", firstAttribute.getValue().getValue());
  }

  @Test
  void map_diagnosis() {
    var bundle = new Bundle();
    var entry = bundle.getEntry();
    entry.add(createPatientEntry(p -> p.setBirthDateElement(new DateType("2000-01-01"))));
    var condition = new Condition();
    condition.getSubject().setReference("Patient/" + PATIENT_ID);
    entry.add(new BundleEntryComponent().setResource(condition));
    var expectedContainer = new Container();
    when(diagnosisMapping.map(condition, (Patient) entry.get(0).getResource()))
        .thenReturn(expectedContainer);
    var result = service.map(bundle);

    assertEquals(expectedContainer, result.getPatient().get(0).getContainer().get(0));
  }

  private BundleEntryComponent createPatientEntry() {
    return createPatientEntry(p -> {
    });
  }

  private BundleEntryComponent createPatientEntry(Consumer<Patient> consumer) {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    consumer.accept(patient);
    return new BundleEntryComponent().setResource(patient);
  }

  private BundleEntryComponent createObservationEntry(Consumer<Observation> consumer) {
    var observation = new Observation();
    observation.getSubject().setReference("Patient/" + PATIENT_ID);
    consumer.accept(observation);
    return new BundleEntryComponent().setResource(observation);
  }
}
