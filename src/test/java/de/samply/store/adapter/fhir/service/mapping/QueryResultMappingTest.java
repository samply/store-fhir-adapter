package de.samply.store.adapter.fhir.service.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import de.samply.store.adapter.fhir.model.PatientContainer;
import de.samply.store.adapter.fhir.service.mapping.PatientMapping;
import de.samply.store.adapter.fhir.service.mapping.QueryResultMapping;
import java.util.List;
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
    de.samply.share.model.ccp.Patient patient1 = new de.samply.share.model.ccp.Patient();
    var patientContainer = createPatientContainer();
    when(patientMapping.map(patientContainer)).thenReturn(patient1);

    var result = service.map(List.of(patientContainer));

    assertEquals(List.of(patient1), result.getPatient());
  }

  @Test
  void mapTwoPatients() {
    var patientContainer1 = createPatientContainer();
    var patientContainer2 = createPatientContainer();
    de.samply.share.model.ccp.Patient patient1 = new de.samply.share.model.ccp.Patient();
    when(patientMapping.map(patientContainer1)).thenReturn(patient1);
    de.samply.share.model.ccp.Patient patient2 = new de.samply.share.model.ccp.Patient();
    when(patientMapping.map(patientContainer2)).thenReturn(patient2);

    var result = service.map(List.of(patientContainer1, patientContainer2));

    assertEquals(List.of(patient1, patient2), result.getPatient());
  }

  private PatientContainer createPatientContainer() {
    Patient patient = new Patient();
    PatientContainer patientContainer = new PatientContainer();
    patientContainer.setPatient(patient);
    return patientContainer;
  }
}
