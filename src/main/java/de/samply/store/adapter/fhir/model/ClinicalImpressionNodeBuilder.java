package de.samply.store.adapter.fhir.model;

import java.util.stream.Stream;
import org.hl7.fhir.r4.model.ClinicalImpression;

class ClinicalImpressionNodeBuilder {

  private ClinicalImpression clinicalImpression;

  public ClinicalImpression getClinicalImpression() {
    return clinicalImpression;
  }

  void setClinicalImpression(ClinicalImpression clinicalImpression) {
    this.clinicalImpression = clinicalImpression;
  }

  Stream<ClinicalImpressionNode> build() {
    return Stream.ofNullable(clinicalImpression).map(ClinicalImpressionNode::new);
  }
}
