package de.samply.store.adapter.fhir.model;

import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Resource;

/**
 * The root node of the tree of FHIR Resources modeled after the hierarchy of the MDS data set.
 */
public record RootNode(List<PatientNode> patients, Map<String, Resource> resources) {

}
