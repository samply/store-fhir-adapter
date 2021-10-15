package de.samply.store.adapter.fhir.service;

import de.samply.share.model.ccp.ObjectFactory;
import de.samply.share.model.ccp.Patient;
import de.samply.store.adapter.fhir.service.mapping.AbstractBuilder;
import org.hl7.fhir.r4.model.Resource;

/**
 * A builder for MDS Patients.
 */
public class PatientBuilder extends AbstractBuilder<Patient> {

  public PatientBuilder(FhirPathR4 fhirPathR4, Resource resource) {
    super(fhirPathR4, new ObjectFactory().createPatient(), resource);
    entity.setId(resource.getIdElement().getIdPart());
  }
}
