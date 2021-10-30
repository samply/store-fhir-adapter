package de.samply.store.adapter.fhir.service;

import de.samply.share.model.ccp.ObjectFactory;
import de.samply.share.model.ccp.Patient;
import de.samply.store.adapter.fhir.service.mapping.AbstractBuilder;
import org.hl7.fhir.r4.model.StringType;

/**
 * A builder for MDS Patients.
 */
public class PatientBuilder extends AbstractBuilder<Patient> {

  private static final String PSEUDONYM_ART_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS";
  private static final String LOCAL_ID_PATH =
      "Patient.identifier.where(type.coding.where(system= \"" + PSEUDONYM_ART_CS
          + "\" and code=\"Lokal\").exists()).value";


  /**
   * Creates a new Patient builder.
   *
   * @param fhirPathEngine the FHIRPath engine
   * @param patient        the FHIR patient resource
   */
  public PatientBuilder(FhirPathR4 fhirPathEngine, org.hl7.fhir.r4.model.Patient patient) {
    super(fhirPathEngine, new ObjectFactory().createPatient(), patient);
    fhirPathEngine.evaluateFirst(patient, LOCAL_ID_PATH, StringType.class)
        .ifPresent(id -> entity.setId(id.getValue()));
  }
}
