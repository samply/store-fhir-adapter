package de.samply.store.adapter.fhir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.PatientBuilder;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PatientBuilderTest {

  private static final FhirContext fhirContext = FhirContext.forR4();

  @Test
  void check_id() {

    Patient patient = new Patient();
    CodeableConcept con = new CodeableConcept();
    con.getCodingFirstRep().setCode("Lokal").setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS");
        patient.getIdentifierFirstRep().setValue("73TST2").setType(con);
    PatientBuilder patientBuilder = new PatientBuilder(
        new FhirPathR4(fhirContext, new EvaluationContext()), patient);

    var container = patientBuilder.build();

    assertEquals("73TST2", container.getId());
  }

}
