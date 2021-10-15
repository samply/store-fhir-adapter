package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.model.ClinicalImpressionNode;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.ClinicalImpression.ClinicalImpressionFindingComponent;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class ProgressMappingTest {

  @ParameterizedTest
  @CsvFileSource(resources = "/progressMappings.csv", numLinesToSkip = 1)
  void map_ProgressObservationCSVFile(String fhirDate, String fhirReaktion, String fhirRezidiv,
      String fhirLymphnode, String fhirmetastasis, String dktkDate, String dktkReaktion,
      String dktkRezidiv, String dktkLymphnode, String dktkmetastasis) {

    ClinicalImpression clinicalImpression = new ClinicalImpression();

    clinicalImpression.setEffective(new DateTimeType(fhirDate));
    Map<String, Resource> findings;
    findings = new HashMap<>();
    List<ClinicalImpressionFindingComponent> refs;
    refs = new ArrayList<>();

    if (fhirRezidiv != null) {
      findings.put("Observation/rez123", createObservation("LA4583-6", "rez123", fhirRezidiv));
      refs.add(new ClinicalImpressionFindingComponent().setItemReference(
          new Reference("Observation/rez123")));
    }

    if (fhirReaktion != null) {
      findings.put("Observation/r123", createObservation("21976-6", "r123", fhirReaktion));
      refs.add(new ClinicalImpressionFindingComponent().setItemReference(
          new Reference("Observation/r123")));
    }

    if (fhirLymphnode != null) {
      findings.put("Observation/lym123", createObservation("LA4370-8", "lym123", fhirLymphnode));
      refs.add(new ClinicalImpressionFindingComponent().setItemReference(
          new Reference("Observation/lym123")));
    }

    if (fhirmetastasis != null) {
      findings.put("Observation/meta123", createObservation("21907-1", "meta123", fhirmetastasis));
      refs.add(new ClinicalImpressionFindingComponent().setItemReference(
          new Reference("Observation/meta123")));
    }

    clinicalImpression.setFinding(refs);

    var mapping = new ProgressMapping(new FhirPathR4(FhirContext.forR4(), new EvaluationContext(
        findings)));

    var container = mapping.map(new ClinicalImpressionNode(clinicalImpression));

    assertEquals(Optional.ofNullable(dktkDate),
        findAttributeValue(container, "urn:dktk:dataelement:25:4"));
    assertEquals(Optional.ofNullable(dktkReaktion),
        findAttributeValue(container, "urn:dktk:dataelement:24:3"));
    assertEquals(Optional.ofNullable(dktkRezidiv),
        findAttributeValue(container, "urn:dktk:dataelement:72:2"));
    assertEquals(Optional.ofNullable(dktkLymphnode),
        findAttributeValue(container, "urn:dktk:dataelement:73:2"));
    assertEquals(Optional.ofNullable(dktkmetastasis),
        findAttributeValue(container, "urn:dktk:dataelement:74:2"));

  }

  private Observation createObservation(String code, String ref, String fhirCode) {
    var ob = new Observation();
    ob.setId(ref);
    ob.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode(code);
    ob.getValueCodeableConcept().getCodingFirstRep().setCode(fhirCode);
    return ob;
  }
}
