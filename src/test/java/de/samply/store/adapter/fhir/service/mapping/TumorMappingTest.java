package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping.ICD_O_3;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.MyIEvaluationContext;
import java.util.Optional;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * @author Patrick Skowronek
 */


public class TumorMappingTest {

  private TumorMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new TumorMapping(new FhirPathR4(FhirContext.forR4(), new MyIEvaluationContext()));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/tumorMappings.csv", numLinesToSkip = 1)
  void map_TumorConditionCSVFile(
      String FHIR_ICD10, String FHIR_ICD_O_3, String FHIR_ADT_Site, String DKTK_ICD10,
      String DKTK_ICD_O_3, String DKTK_site
  ) {
    var condition = new Condition();
    condition.getBodySiteFirstRep().getCodingFirstRep().setSystem(ICD_O_3).setCode(FHIR_ICD10)
        .setVersion(FHIR_ICD_O_3);
    var coding = new Coding().setSystem("urn:oid:2.16.840.1.113883.2.6.60.7.1.1")
        .setCode(FHIR_ADT_Site);
    condition.getBodySiteFirstRep().addCoding(coding);

    var container = mapping.map(condition);

    assertEquals(Optional.ofNullable(DKTK_ICD10),
        findAttributeValue(container, "urn:dktk:dataelement:4:2"));
    assertEquals(Optional.ofNullable(DKTK_ICD_O_3),
        findAttributeValue(container, "urn:dktk:dataelement:5:2"));
    assertEquals(Optional.ofNullable(DKTK_site),
        findAttributeValue(container, "urn:dktk:dataelement:6:2"));
  }
}
