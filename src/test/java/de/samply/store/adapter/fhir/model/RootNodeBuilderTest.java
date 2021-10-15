package de.samply.store.adapter.fhir.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.Test;

class RootNodeBuilderTest {

  @Test
  void testEmptyBundle() {
    Bundle bundle = new Bundle();

    var node = RootNodeBuilder.fromBundle(bundle);

    assertTrue(node.patients().isEmpty());
    assertTrue(node.resources().isEmpty());
  }

  @Test
  void testPatientBundle() {
    Patient patient = new Patient();
    patient.setId("123");
    Bundle bundle = new Bundle();
    bundle.addEntry().setResource(patient);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(patient, node.patients().get(0).patient());
    assertEquals(patient, node.resources().get("Patient/123"));
  }

  @Test
  void testConditionBundle() {
    Patient patient = new Patient();
    patient.setId("123");
    Condition condition = new Condition();
    condition.setId("123");
    condition.getSubject().setReference("Patient/123");
    Bundle bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(condition);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(condition, node.patients().get(0).conditions().get(0).condition());
    assertEquals(condition, node.resources().get("Condition/123"));
  }

  @Test
  void testPatientConditionBundle() {
    Patient patient = new Patient();
    patient.setId("123");
    Condition condition = new Condition();
    condition.setId("123");
    condition.getSubject().setReference("Patient/123");
    Bundle bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(condition);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(patient, node.patients().get(0).patient());
    assertEquals(condition, node.patients().get(0).conditions().get(0).condition());
    assertEquals(patient, node.resources().get("Patient/123"));
    assertEquals(condition, node.resources().get("Condition/123"));
  }

  @Test
  void testVitalStatusBundle() {
    Patient patient = new Patient();
    patient.setId("123");
    Observation observation = new Observation();
    observation.setId("123");
    observation.getSubject().setReference("Patient/123");
    observation.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode("75186-7");
    Bundle bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(observation);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(observation, node.patients().get(0).vitalState().orElseThrow());
    assertEquals(observation, node.resources().get("Observation/123"));
  }

  @Test
  void testSpecimenBundle() {
    Patient patient = new Patient();
    patient.setId("123");
    Specimen specimen = new Specimen();
    specimen.setId("123");
    specimen.getSubject().setReference("Patient/123");
    Bundle bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(specimen);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(specimen, node.patients().get(0).specimens().get(0));
    assertEquals(specimen, node.resources().get("Specimen/123"));
  }

  @Test
  void testClinicalImpressionBundle() {
    Patient patient = new Patient();
    patient.setId("123");
    ClinicalImpression clinicalImpression = new ClinicalImpression();
    clinicalImpression.setId("123");
    clinicalImpression.getSubject().setReference("Patient/123");
    clinicalImpression.getProblemFirstRep().setReference("Condition/123");
    Condition condition = new Condition();
    condition.setId("123");
    condition.getSubject().setReference("Patient/123");
    Bundle bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(clinicalImpression);
    bundle.addEntry().setResource(condition);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(clinicalImpression,
        node.patients().get(0).conditions().get(0).clinicalImpressions().get(0)
            .clinicalImpression());
    assertEquals(clinicalImpression, node.resources().get("ClinicalImpression/123"));
  }
}
