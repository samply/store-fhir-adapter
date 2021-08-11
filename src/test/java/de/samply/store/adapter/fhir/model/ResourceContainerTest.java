package de.samply.store.adapter.fhir.model;

import static org.junit.jupiter.api.Assertions.*;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;

class ResourceContainerTest {

  @Test
  void testEmptyBundle() {
    Bundle bundle = new Bundle();

    var container = ResourceContainer.fromBundle(bundle);

    assertTrue(container.getPatientContainers().isEmpty());
    assertTrue(container.getResources().isEmpty());
  }

  @Test
  void testPatientBundle() {
    Bundle bundle = new Bundle();
    Patient patient = new Patient();
    patient.setId("123");
    bundle.addEntry().setResource(patient);

    var container = ResourceContainer.fromBundle(bundle);

    assertTrue(container.getPatientContainers().stream().findFirst().isPresent());
    assertEquals(patient, container.getPatientContainers().stream().findFirst().get().getPatient());
    assertEquals(patient, container.getResources().get("Patient/123"));
  }

  @Test
  void testConditionBundle() {
    Bundle bundle = new Bundle();
    Condition condition = new Condition();
    condition.setId("c123");
    condition.setSubject(new Reference("Patient/123"));
    bundle.addEntry().setResource(condition);

    var container = ResourceContainer.fromBundle(bundle);

    assertTrue(container.getPatientContainers().stream().findFirst().isPresent());
    assertEquals(condition, container.getPatientContainers().stream().findFirst().get().getConditionContainerList().get(0).getCondition());
    assertEquals(condition, container.getResources().get("Condition/c123"));
  }

  @Test
  void testPatientConditionBundle() {
    Bundle bundle = new Bundle();
    Patient patient = new Patient();
    patient.setId("123");
    bundle.addEntry().setResource(patient);
    Condition condition = new Condition();
    condition.setId("c123");
    condition.setSubject(new Reference("Patient/123"));
    bundle.addEntry().setResource(condition);

    var container = ResourceContainer.fromBundle(bundle);

    assertTrue(container.getPatientContainers().stream().findFirst().isPresent());
    assertEquals(patient, container.getPatientContainers().stream().findFirst().get().getPatient());
    assertEquals(condition, container.getPatientContainers().stream().findFirst().get().getConditionContainerList().get(0).getCondition());
    assertEquals(patient, container.getResources().get("Patient/123"));
    assertEquals(condition, container.getResources().get("Condition/c123"));
  }

  @Test
  void testVitalStatusBundle() {
    Bundle bundle = new Bundle();
    Observation observation = new Observation();
    observation.setId("v123");
    observation.setSubject(new Reference("Patient/123"));
    observation.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode("75186-7");
    bundle.addEntry().setResource(observation);

    var container = ResourceContainer.fromBundle(bundle);

    assertTrue(container.getPatientContainers().stream().findFirst().isPresent());
    assertTrue(container.getPatientContainers().stream().findFirst().get().getVitalState().isPresent());
    assertEquals(observation, container.getPatientContainers().stream().findFirst().get().getVitalState().get());
    assertEquals(observation, container.getResources().get("Observation/v123"));
  }

}