package de.samply.store.adapter.fhir.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.Test;

class RootNodeBuilderTest {

  private static final String PATIENT_ID = "143111";
  private static final String CONDITION_ID = "143244";
  private static final String VITAL_STATE_ID = "143307";
  private static final String SPECIMEN_ID = "143336";
  private static final String PROCEDURE_ID = "195544";
  private static final String CLINICAL_IMPRESSION_ID = "143743";
  private static final String HISTOLOGY_ID = "143809";

  @Test
  void testEmptyBundle() {
    var bundle = new Bundle();

    var node = RootNodeBuilder.fromBundle(bundle);

    assertTrue(node.patients().isEmpty());
    assertTrue(node.resources().isEmpty());
  }

  @Test
  void testPatientBundle() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(patient, node.patients().get(0).patient());
    assertEquals(patient, node.resources().get("Patient/" + PATIENT_ID));
  }

  @Test
  void testConditionBundle() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getSubject().setReference("Patient/" + PATIENT_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(condition);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(condition, node.patients().get(0).conditions().get(0).condition());
    assertEquals(condition, node.resources().get("Condition/" + CONDITION_ID));
  }

  @Test
  void testPatientConditionBundle() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getSubject().setReference("Patient/" + PATIENT_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(condition);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(patient, node.patients().get(0).patient());
    assertEquals(condition, node.patients().get(0).conditions().get(0).condition());
    assertEquals(patient, node.resources().get("Patient/" + PATIENT_ID));
    assertEquals(condition, node.resources().get("Condition/" + CONDITION_ID));
  }

  @Test
  void testVitalStateBundle() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    var vitalState = new Observation();
    vitalState.setId(VITAL_STATE_ID);
    vitalState.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode("75186-7");
    vitalState.getSubject().setReference("Patient/" + PATIENT_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(vitalState);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(vitalState, node.patients().get(0).vitalState().orElseThrow());
    assertEquals(vitalState, node.resources().get("Observation/" + VITAL_STATE_ID));
  }

  @Test
  void testHistologyBundle() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getSubject().setReference("Patient/" + PATIENT_ID);
    var histology = new Observation();
    histology.setId(HISTOLOGY_ID);
    histology.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode("59847-4");
    histology.getSubject().setReference("Patient/" + PATIENT_ID);
    histology.getFocusFirstRep().setReference("Condition/" + CONDITION_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(condition);
    bundle.addEntry().setResource(histology);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(histology, node.patients().get(0).conditions().get(0).histologies().get(0));
    assertEquals(histology, node.resources().get("Observation/" + HISTOLOGY_ID));
  }

  @Test
  void testHistologyBundleWithoutFocus() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getSubject().setReference("Patient/" + PATIENT_ID);
    var histology = new Observation();
    histology.setId(HISTOLOGY_ID);
    histology.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode("59847-4");
    histology.getSubject().setReference("Patient/" + PATIENT_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(condition);
    bundle.addEntry().setResource(histology);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertTrue(node.patients().get(0).conditions().get(0).histologies().isEmpty());
    assertEquals(histology, node.resources().get("Observation/" + HISTOLOGY_ID));
  }

  @Test
  void testProcedureBundle() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getSubject().setReference("Patient/" + PATIENT_ID);
    var procedure = new Procedure();
    procedure.setId(PROCEDURE_ID);
    procedure.getSubject().setReference("Patient/" + PATIENT_ID);
    procedure.getReasonReferenceFirstRep().setReference("Condition/" + CONDITION_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(condition);
    bundle.addEntry().setResource(procedure);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(procedure, node.patients().get(0).conditions().get(0).procedures().get(0));
    assertEquals(procedure, node.resources().get("Procedure/" + PROCEDURE_ID));
  }

  @Test
  void testProcedureBundleWithoutReasonReference() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getSubject().setReference("Patient/" + PATIENT_ID);
    var procedure = new Procedure();
    procedure.setId(PROCEDURE_ID);
    procedure.getSubject().setReference("Patient/" + PATIENT_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(condition);
    bundle.addEntry().setResource(procedure);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertTrue(node.patients().get(0).conditions().get(0).procedures().isEmpty());
    assertEquals(procedure, node.resources().get("Procedure/" + PROCEDURE_ID));
  }

  @Test
  void testSpecimenBundle() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    var specimen = new Specimen();
    specimen.setId(SPECIMEN_ID);
    specimen.getSubject().setReference("Patient/" + PATIENT_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(specimen);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(specimen, node.patients().get(0).specimens().get(0));
    assertEquals(specimen, node.resources().get("Specimen/" + SPECIMEN_ID));
  }

  @Test
  void testClinicalImpressionBundle() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    ClinicalImpression clinicalImpression = new ClinicalImpression();
    clinicalImpression.setId(CLINICAL_IMPRESSION_ID);
    clinicalImpression.getSubject().setReference("Patient/" + PATIENT_ID);
    clinicalImpression.getProblemFirstRep().setReference("Condition/" + CONDITION_ID);
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getSubject().setReference("Patient/" + PATIENT_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(clinicalImpression);
    bundle.addEntry().setResource(condition);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertEquals(clinicalImpression,
        node.patients().get(0).conditions().get(0).clinicalImpressions().get(0)
            .clinicalImpression());
    assertEquals(clinicalImpression, node.resources().get(
        "ClinicalImpression/" + CLINICAL_IMPRESSION_ID));
  }

  @Test
  void testClinicalImpressionBundleWithoutProblem() {
    var patient = new Patient();
    patient.setId(PATIENT_ID);
    ClinicalImpression clinicalImpression = new ClinicalImpression();
    clinicalImpression.setId(CLINICAL_IMPRESSION_ID);
    clinicalImpression.getSubject().setReference("Patient/" + PATIENT_ID);
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getSubject().setReference("Patient/" + PATIENT_ID);
    var bundle = new Bundle();
    bundle.addEntry().setResource(patient);
    bundle.addEntry().setResource(clinicalImpression);
    bundle.addEntry().setResource(condition);

    var node = RootNodeBuilder.fromBundle(bundle);

    assertTrue(node.patients().get(0).conditions().get(0).clinicalImpressions().isEmpty());
    assertEquals(clinicalImpression, node.resources().get(
        "ClinicalImpression/" + CLINICAL_IMPRESSION_ID));
  }
}
