package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttrValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Optional;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class MetatasisMappingTest {

  private static final FhirContext fhirContext = FhirContext.forR4();

  private MetastasisMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new MetastasisMapping(new FhirPathR4(fhirContext, new EvaluationContext()));
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

    assertEquals(Optional.ofNullable(dktkExtraction), findAttrValue(container, "21:3"));
    assertEquals(Optional.ofNullable(dktkFern), findAttrValue(container, "77:1"));
    assertEquals(Optional.ofNullable(dktkLocation), findAttrValue(container, "98:1"));

  }
}
