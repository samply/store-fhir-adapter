package de.samply.store.adapter.fhir.service;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alexander Kiel
 */
@ExtendWith(MockitoExtension.class)
class MappingServiceTest {

    public static final String PATIENT_ID = "id-193956";

    @InjectMocks
    private MappingService service;

    @Test
    void map() {
        var result = service.map(new Bundle());

        assertTrue(result.getPatient().isEmpty());
    }

    @Test
    void map_withOnePatient() {
        var patient = new Patient();
        patient.setId(PATIENT_ID);
        var entry = new Bundle.BundleEntryComponent();
        entry.setResource(patient);
        var bundle = new Bundle();
        bundle.getEntry().add(entry);

        var result = service.map(bundle);

        assertEquals(PATIENT_ID, result.getPatient().get(0).getDktkId());
    }
}
