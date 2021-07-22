package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.MyIEvaluationContext;
import java.util.Optional;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * @author Patrick Skowronek
 */

public class MetatasisMappingTest {

  private MetastasisMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new MetastasisMapping(new FhirPathR4(FhirContext.forR4(), new MyIEvaluationContext()));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/metastasisMappings.csv", numLinesToSkip = 1)
  void map_metastasisMappingCSVFile(String fhirExtractionDate, String fhirFern, String fhirLocation,
      String dktkExtraction, String dktkFern, String dktkLocation) {
    var metastasis = new Observation();
    if (fhirExtractionDate != null) {
      metastasis.setEffective(new DateTimeType(fhirExtractionDate));
    }
    metastasis.getValueCodeableConcept().getCodingFirstRep().setCode(fhirFern);
    metastasis.getBodySite().getCodingFirstRep().setCode(fhirLocation);

    var container = mapping.map(metastasis);

    assertEquals(Optional.ofNullable(dktkExtraction),
        findAttributeValue(container, "urn:dktk:dataelement:28:1"));
    assertEquals(Optional.ofNullable(dktkFern),
        findAttributeValue(container, "urn:dktk:dataelement:77:1"));
    assertEquals(Optional.ofNullable(dktkLocation),
        findAttributeValue(container, "urn:dktk:dataelement:98:1"));

  }
}
