package de.samply.store.adapter.fhir.model;

import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Procedure;

public class ClinicalImpressionContainer {

  private ClinicalImpression clinicalImpression;

  public ClinicalImpression getClinicalImpression() {
    return clinicalImpression;
  }
  public void setClinicalImpression(ClinicalImpression clinicalImpression) {
    this.clinicalImpression = clinicalImpression;
  }
}
