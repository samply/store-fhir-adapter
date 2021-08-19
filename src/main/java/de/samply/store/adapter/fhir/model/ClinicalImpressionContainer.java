package de.samply.store.adapter.fhir.model;

import org.hl7.fhir.r4.model.ClinicalImpression;

public class ClinicalImpressionContainer {

  private ClinicalImpression clinicalImpression;

  public ClinicalImpression getClinicalImpression() {
    return clinicalImpression;
  }

  public void setClinicalImpression(ClinicalImpression clinicalImpression) {
    this.clinicalImpression = clinicalImpression;
  }
}
