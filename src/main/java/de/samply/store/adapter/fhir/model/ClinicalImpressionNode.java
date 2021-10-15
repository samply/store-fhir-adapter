package de.samply.store.adapter.fhir.model;

import org.hl7.fhir.r4.model.ClinicalImpression;

/**
 * The clinical impression node of the tree of FHIR Resources modeled after the hierarchy of the
 * MDS data set.
 */
public record ClinicalImpressionNode(ClinicalImpression clinicalImpression) {

}
