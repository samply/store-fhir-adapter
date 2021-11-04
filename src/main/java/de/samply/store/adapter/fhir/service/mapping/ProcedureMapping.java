package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import org.hl7.fhir.r4.model.Procedure;

/**
 * Common Procedure Mapping Service.
 */
public interface ProcedureMapping {

  Container map(Procedure procedure);
}
