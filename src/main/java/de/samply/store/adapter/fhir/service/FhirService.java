package de.samply.store.adapter.fhir.service;

import static ca.uhn.fhir.rest.api.SummaryEnum.COUNT;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IOperationUntypedWithInput;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.UriClientParam;
import de.samply.store.adapter.fhir.util.Either;
import java.util.Objects;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Common FHIR API functionality.
 */
@Service
public class FhirService {

  private static final Logger logger = LoggerFactory.getLogger(FhirDownloadService.class);

  private final IGenericClient client;

  /**
   * Creates a new {@code FhirDownloadService}.
   *
   * @param client the HAPI FHIR client
   */
  public FhirService(IGenericClient client) {
    this.client = Objects.requireNonNull(client);
  }

  /**
   * Checks whether a resource of {@code type} and canonical {@code uri} exists.
   *
   * @param type the resource type
   * @param uri  the canonical URI
   * @return a Right with {@code true} if the resource exists or a Left in case of an error
   */
  public Either<String, Boolean> resourceExists(Class<? extends IBaseResource> type, String uri) {
    logger.debug("Check whether {} with canonical URI {} exists.", type.getSimpleName(), uri);
    return Either.tryGet(() -> resourceQuery(type, uri).execute())
        .mapLeft(Exception::getMessage)
        .map(bundle -> bundle.getTotal() == 1);
  }

  private IQuery<Bundle> resourceQuery(Class<? extends IBaseResource> type, String uri) {
    return client.search().forResource(type)
        .where(new UriClientParam("url").matches().value(uri))
        .summaryMode(COUNT)
        .returnBundle(Bundle.class);
  }

  /**
   * Evaluates the measure with {@code uri}.
   *
   * @param uri the canonical URI of the measure to evaluate
   * @return a Right with the measure report or a Left in case of an error
   */
  public Either<String, MeasureReport> evaluateMeasure(String uri) {
    return Either.tryGet(() -> evaluateMeasureQuery(uri).execute())
        .mapLeft(Exception::getMessage);
  }

  private IOperationUntypedWithInput<MeasureReport> evaluateMeasureQuery(String uri) {
    return client.operation().onType(Measure.class).named("evaluate-measure")
        .withParameter(Parameters.class, "measure", new StringType(uri))
        .andParameter("reportType", new CodeType("subject-list"))
        .andParameter("periodStart", new DateType(1900, 1, 1))
        .andParameter("periodEnd", new DateType(2100, 1, 1))
        .returnResourceType(MeasureReport.class);
  }

  /**
   * Fetches the first page of patients on list with {@code listId}.
   *
   * @param listId   the ID of the list to fetch patients from
   * @param pageSize the page size
   * @return a Right with the bundle of the first page or a Left in case of an error
   */
  public Either<String, Bundle> fetchFirstPage(String listId, int pageSize) {
    return Either.tryGet(() -> firstPageQuery(listId, pageSize).execute())
        .mapLeft(Exception::getMessage);
  }

  private IQuery<Bundle> firstPageQuery(String listId, int pageSize) {
    return client.search()
        .byUrl("Patient?_list=" + Objects.requireNonNull(listId))
        .revInclude(new Include("Observation:patient"))
        .revInclude(new Include("Condition:patient"))
        .revInclude(new Include("Specimen:patient"))
        .revInclude(new Include("Procedure:patient"))
        .revInclude(new Include("MedicationStatement:patient"))
        .revInclude(new Include("ClinicalImpression:patient"))
        .count(pageSize)
        .returnBundle(Bundle.class);
  }

  /**
   * Fetches the bundle with the total size of the list with {@code listId}.
   *
   * @param listId the ID of the list to fetch the bundle from
   * @return a Right with the bundle or a Left in case of an error
   */
  public Either<String, Bundle> fetchTotalBundle(String listId) {
    return Either.tryGet(() -> totalBundleQuery(listId).execute()).mapLeft(Exception::getMessage);
  }

  private IQuery<Bundle> totalBundleQuery(String listId) {
    return client.search()
        .byUrl("Patient?_list=" + listId)
        .summaryMode(COUNT)
        .returnBundle(Bundle.class);
  }
}
