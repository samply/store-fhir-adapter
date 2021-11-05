package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttrValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.model.ClinicalImpressionNode;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class ProgressMappingTest {

  private static final String VITALSTATUS_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS";
  private static final String GESAMTBEURTEILUNG_TUMORSTATUS_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungTumorstatusCS";
  private static final String LOKALER_TUMORSTATUS_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VerlaufLokalerTumorstatusCS";
  private static final String TUMORSTATUS_LYMPHKNOTEN_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VerlaufTumorstatusLymphknotenCS";
  private static final String TUMORSTATUS_FERNMETASTASEN_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VerlaufTumorstatusFernmetastasenCS";

  private static final FhirContext fhirContext = FhirContext.forR4();

  private ClinicalImpression clinicalImpression;
  private Map<String, Resource> findings;
  private ProgressMapping mapping;

  @BeforeEach
  void setUp() {
    clinicalImpression = new ClinicalImpression();
    findings = new HashMap<>();
    FhirPathR4 fhirPathEngine = new FhirPathR4(fhirContext, new EvaluationContext(findings));
    mapping = new ProgressMapping(fhirPathEngine, new TnmMapping(fhirPathEngine));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/progressMappings.csv", numLinesToSkip = 1)
  void map_ProgressObservationCSVFile(
      String fhirDate, String vitalState, String histology,
      String fhirFullAssessment, String fhirPrimaryAssessment,
      String fhirLymphAssessment, String fhirMetaAssessment,
      String dktkDate,
      String dktkFullAssessment, String dktkPrimaryAssessment,
      String dktkLymphAssessment, String dktkMetaAssessment) {

    clinicalImpression.setEffective(new DateTimeType(fhirDate));

    if (vitalState != null) {
      withObservation("75186-7", VITALSTATUS_CS, vitalState);
    }

    if (histology != null) {
      withObservation("59847-4", "urn:oid:2.16.840.1.113883.6.43.1", histology);
    }

    if (fhirFullAssessment != null) {
      withObservation("21976-6", GESAMTBEURTEILUNG_TUMORSTATUS_CS, fhirFullAssessment);
    }

    if (fhirPrimaryAssessment != null) {
      withObservation("LA4583-6", LOKALER_TUMORSTATUS_CS, fhirPrimaryAssessment);
    }

    if (fhirLymphAssessment != null) {
      withObservation("LA4370-8", TUMORSTATUS_LYMPHKNOTEN_CS, fhirLymphAssessment);
    }

    if (fhirMetaAssessment != null) {
      withObservation("LA4226-2", TUMORSTATUS_FERNMETASTASEN_CS, fhirMetaAssessment);
    }

    var container = mapping.map(new ClinicalImpressionNode(clinicalImpression));

    assertEquals(Optional.ofNullable(dktkDate), findAttrValue(container, "25:4"));
    assertEquals(Optional.ofNullable(vitalState), findAttrValue(container, "53:3"));
    assertEquals(Optional.ofNullable(histology), findAttrValue(container, "7:2"));
    assertEquals(Optional.ofNullable(dktkFullAssessment), findAttrValue(container, "24:3"));
    assertEquals(Optional.ofNullable(dktkPrimaryAssessment), findAttrValue(container, "72:2"));
    assertEquals(Optional.ofNullable(dktkLymphAssessment), findAttrValue(container, "73:2"));
    assertEquals(Optional.ofNullable(dktkMetaAssessment), findAttrValue(container, "74:2"));
  }

  private void withObservation(String code, String valueSystem, String valueCode) {
    var id = UUID.randomUUID().toString();
    findings.put("Observation/" + id, createObservation(id, code, valueSystem, valueCode));
    clinicalImpression.addFinding().getItemReference().setReference("Observation/" + id);
  }

  private static Observation createObservation(String id, String code, String valueSystem,
      String valueCode) {
    var ob = new Observation();
    ob.setId(id);
    ob.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode(code);
    ob.getValueCodeableConcept().getCodingFirstRep().setSystem(valueSystem).setCode(valueCode);
    return ob;
  }
}
