package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.QueryResult;
import de.samply.store.adapter.fhir.model.PatientContainer;
import de.samply.store.adapter.fhir.service.mapping.PatientMapping;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;


@Component
public class QueryResultMapping {

  private final PatientMapping patientMapping;

  public QueryResultMapping(PatientMapping patientMapping) {
    this.patientMapping = patientMapping;
  }

  public QueryResult map(Collection<PatientContainer> patientContainers) {
    var result = new QueryResult();
    result.getPatient()
        .addAll(patientContainers.stream().map(patientMapping::map).collect(Collectors.toList()));
    return result;
  }
}