package de.samply.store.adapter.fhir.service;

import de.samply.share.model.ccp.QueryResult;
import de.samply.store.adapter.fhir.model.RootNodeBuilder;
import de.samply.store.adapter.fhir.service.mapping.QueryResultMapping;
import java.util.Map;
import java.util.function.Function;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

/**
 * A service for the mapping of FHIR resources to {@link QueryResult QueryResults}.
 */
@Service
public class MappingService {

  private final Function<Map<String, Resource>, QueryResultMapping> mappingServiceFactory;

  public MappingService(Function<Map<String, Resource>, QueryResultMapping> mappingServiceFactory) {
    this.mappingServiceFactory = mappingServiceFactory;
  }

  /**
   * Map's a {@code Bundle} of a result page to a {@code QueryResult}.
   *
   * @param bundle the bundle to map
   * @return the mapped {@code QueryResult}
   */
  public QueryResult map(Bundle bundle) {
    var rootNode = RootNodeBuilder.fromBundle(bundle);
    return mappingServiceFactory.apply(rootNode.resources()).map(rootNode.patients());
  }
}
