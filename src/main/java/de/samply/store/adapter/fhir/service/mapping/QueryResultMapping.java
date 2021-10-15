package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.QueryResult;
import de.samply.store.adapter.fhir.model.PatientNode;
import java.util.Collection;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Mapping of a collection of patient nodes to a query result.
 */
@Component
public class QueryResultMapping {

  private final PatientMapping patientMapping;

  /**
   * Creates a new QueryResultMapping.
   *
   * @param patientMapping the PatientMapping to use for reach patient
   */
  public QueryResultMapping(PatientMapping patientMapping) {
    this.patientMapping = Objects.requireNonNull(patientMapping);
  }

  /**
   * Maps a collection of patient nodes to a query result.
   *
   * @param patientNodes the collection of patient nodes
   * @return the query result
   */
  public QueryResult map(Collection<PatientNode> patientNodes) {
    var result = new QueryResult();
    result.getPatient().addAll(patientNodes.stream().map(patientMapping::map).toList());
    return result;
  }
}
