package de.samply.store.adapter.fhir.service.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import de.samply.store.adapter.fhir.model.PatientNode;
import java.util.List;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryResultMappingTest {

  @Mock
  private PatientMapping patientMapping;


  private QueryResultMapping service;

  @BeforeEach
  void setUp() {
    service = new QueryResultMapping(patientMapping);
  }

  @Test
  void map() {
    var result = service.map(List.of());

    assertTrue(result.getPatient().isEmpty());
  }

  @Test
  void mapPatient() {
    de.samply.share.model.ccp.Patient patient = new de.samply.share.model.ccp.Patient();
    var patientNode = createPatientNode();
    when(patientMapping.map(patientNode)).thenReturn(patient);

    var result = service.map(List.of(patientNode));

    assertEquals(List.of(patient), result.getPatient());
  }

  @Test
  void mapTwoPatients() {
    var patientNode1 = createPatientNode();
    var patientNode2 = createPatientNode();
    de.samply.share.model.ccp.Patient patient1 = new de.samply.share.model.ccp.Patient();
    when(patientMapping.map(patientNode1)).thenReturn(patient1);
    de.samply.share.model.ccp.Patient patient2 = new de.samply.share.model.ccp.Patient();
    when(patientMapping.map(patientNode2)).thenReturn(patient2);

    var result = service.map(List.of(patientNode1, patientNode2));

    assertEquals(List.of(patient1, patient2), result.getPatient());
  }

  private PatientNode createPatientNode() {
    return new PatientNode(new Patient());
  }
}
