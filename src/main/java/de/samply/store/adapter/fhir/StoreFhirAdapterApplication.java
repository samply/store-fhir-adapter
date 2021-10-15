package de.samply.store.adapter.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping;
import de.samply.store.adapter.fhir.service.mapping.HistologyMapping;
import de.samply.store.adapter.fhir.service.mapping.MetastasisMapping;
import de.samply.store.adapter.fhir.service.mapping.PatientMapping;
import de.samply.store.adapter.fhir.service.mapping.ProgressMapping;
import de.samply.store.adapter.fhir.service.mapping.QueryResultMapping;
import de.samply.store.adapter.fhir.service.mapping.SampleMapping;
import de.samply.store.adapter.fhir.service.mapping.TnmMapping;
import de.samply.store.adapter.fhir.service.mapping.TumorMapping;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main Application Entrypoint.
 */
@SpringBootApplication
public class StoreFhirAdapterApplication {

  @Value("${app.store.url}")
  private String storeUrl;

  public static void main(String[] args) {
    SpringApplication.run(StoreFhirAdapterApplication.class, args);
  }

  /**
   * Creates a mapping service factory, a function from a map of resources to a QueryResultMapping.
   *
   * @param fhirContext the FHIR context
   * @return the mapping service factory
   */
  @Bean
  public Function<Map<String, Resource>, QueryResultMapping> mappingServiceFactory(
      FhirContext fhirContext) {
    return resources -> {
      FhirPathR4 fhirPathEngine = new FhirPathR4(fhirContext, new EvaluationContext(resources));
      return new QueryResultMapping(new PatientMapping(fhirPathEngine,
          new DiagnosisMapping(fhirPathEngine,
              new TumorMapping(fhirPathEngine, new HistologyMapping(fhirPathEngine),
                  new MetastasisMapping(fhirPathEngine), new ProgressMapping(fhirPathEngine),
                  new TnmMapping(fhirPathEngine))),
          new SampleMapping(fhirPathEngine)));
    };
  }

  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forR4();
  }

  @Bean
  public FhirPathR4 fhirPathEngine(FhirContext context, IEvaluationContext evaluationContext) {
    return new FhirPathR4(context, evaluationContext);
  }

  @Bean
  public IEvaluationContext evaluationContext() {
    return new EvaluationContext();
  }

  @Bean
  public IGenericClient storeClient(FhirContext fhirContext) {
    return fhirContext.newRestfulGenericClient(storeUrl);
  }

  @Bean
  public Supplier<String> resultIdSupplier() {
    return () -> UUID.randomUUID().toString();
  }
}
