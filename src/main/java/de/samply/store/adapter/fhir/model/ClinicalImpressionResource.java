package de.samply.store.adapter.fhir.model;

import java.util.List;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Procedure;

public class ClinicalImpressionResource {

  private final ClinicalImpression clinicalImpression;

  //Progress

  public ClinicalImpressionResource(ClinicalImpression clinicalImpression) {
    this.clinicalImpression = clinicalImpression;
  }
}
