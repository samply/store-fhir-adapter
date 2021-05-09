package de.samply.store.adapter.fhir.service;

import de.samply.share.model.ccp.QueryResult;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Service;

/**
 * A service for the mapping of FHIR resources to {@link QueryResult QueryResults}.
 *
 * @author Alexander Kiel
 */
@Service
public class MappingService {

  /**
   * Map's a {@code Bundle} of a result page to a {@code QueryResult}.
   *
   * @param bundle the bundle to map
   * @return the mapped {@code QueryResult}
   */
  public QueryResult map(Bundle bundle) {
    var result = new QueryResult();
    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      var patient = new de.samply.share.model.ccp.Patient();
      patient.setDktkId(entry.getResource().getId());
      result.getPatient().add(patient);
    }
    return result;
  }
}
