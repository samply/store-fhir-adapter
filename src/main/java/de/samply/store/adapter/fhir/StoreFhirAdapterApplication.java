package de.samply.store.adapter.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.store.adapter.fhir.service.MyIEvaluationContext;
import java.util.UUID;
import java.util.function.Supplier;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StoreFhirAdapterApplication {

  @Value("${app.store.url}")
  private String storeUrl;

  public static void main(String[] args) {
    SpringApplication.run(StoreFhirAdapterApplication.class, args);
  }

  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forR4();
  }

  @Bean
  public FhirPathR4 fhirPath(FhirContext context, IEvaluationContext evaluationContext) {
    return new FhirPathR4(context, evaluationContext);
  }

  @Bean
  public IEvaluationContext evaluationContext() {
    return new MyIEvaluationContext();
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
