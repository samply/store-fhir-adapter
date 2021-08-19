package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.MyIEvaluationContext;
import java.util.ArrayList;
import java.util.Optional;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * @author Patrick Skowronek
 */


public class TNMMappingTest {

  private TNMMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new TNMMapping(new FhirPathR4(FhirContext.forR4(), new MyIEvaluationContext()));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/tnmMappings.csv", numLinesToSkip = 1)
  void map_TumorConditionCSVFile(String fhirTNMDate, String fhirUICC, String fhirTNMT,
      String fhirTNMMS, String fhirTNMN, String fhirTNMM, String fhirPreT, String fhirPreN,
      String fhirPreM, String fhirTNMYS, String fhirTNMRS, String fhirTNMVersion,
      String dktkTNMDate, String dktkUICC, String dktkTNMT, String dktkTNMMS, String dktkTNMN,
      String dktkTNMM, String dktkPreT, String dktkPreN, String dktkPreM, String dktkTNMYS,
      String dktkTNMRS, String dktkTNMVersion) {
    var observation = new Observation();

    observation.setEffective(new DateTimeType(fhirTNMDate));
    observation.getValueCodeableConcept().getCodingFirstRep().setVersion(fhirTNMVersion)
        .setCode(fhirUICC);
    ArrayList<ObservationComponentComponent> compList;
    compList = new ArrayList<>();
    if (fhirTNMT != null || fhirPreT != null) {
      var comp = createCompontent(fhirTNMT, "21905-5", observation);
      var code = new CodeableConcept();
      code.getCodingFirstRep().setSystem(
              "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix")
          .setCode(fhirPreT);
      comp.getExtensionFirstRep().setUrl(
          "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix");
      comp.getExtensionFirstRep().setValue(code);
      compList.add(comp);
    }
    if (fhirTNMMS != null || fhirTNMN != null) {
      compList.add(createCompontent(fhirTNMMS, "42030-7", observation));
    }
    if (fhirTNMN != null) {
      var comp = createCompontent(fhirTNMN, "201906-3", observation);
      var code = new CodeableConcept();
      code.getCodingFirstRep().setSystem(
              "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix")
          .setCode(fhirPreN);
      comp.getExtensionFirstRep().setUrl(
          "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix");
      comp.getExtensionFirstRep().setValue(code);
      compList.add(comp);
    }
    if (fhirTNMM != null || fhirPreM != null) {
      var comp = createCompontent(fhirTNMM, "21907-1", observation);

      var code = new CodeableConcept();
      code.getCodingFirstRep().setSystem(
              "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix")
          .setCode(fhirPreM);
      comp.getExtensionFirstRep().setUrl(
          "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix");
      comp.getExtensionFirstRep().setValue(code);
      compList.add(comp);
    }
    if (fhirTNMYS != null) {
      compList.add(createCompontent(fhirTNMYS, "59479-6", observation));
    }
    if (fhirTNMRS != null) {
      compList.add(createCompontent(fhirTNMRS, "21983-2", observation));
    }
    observation.setComponent(compList);

    var container = mapping.map(observation);

    assertEquals(Optional.ofNullable(dktkTNMDate),
        findAttributeValue(container, "urn:dktk:dataelement:2:3"));
    assertEquals(Optional.ofNullable(dktkUICC),
        findAttributeValue(container, "urn:dktk:dataelement:89:1"));
    assertEquals(Optional.ofNullable(dktkTNMVersion),
        findAttributeValue(container, "urn:dktk:dataelement:18:2"));
    assertEquals(Optional.ofNullable(dktkTNMT),
        findAttributeValue(container, "urn:dktk:dataelement:100:1"));
    assertEquals(Optional.ofNullable(dktkTNMMS),
        findAttributeValue(container, "urn:dktk:dataelement:10:2"));
    assertEquals(Optional.ofNullable(dktkTNMN),
        findAttributeValue(container, "urn:dktk:dataelement:101:1"));
    assertEquals(Optional.ofNullable(dktkTNMM),
        findAttributeValue(container, "urn:dktk:dataelement:99:1"));
    assertEquals(Optional.ofNullable(dktkPreT),
        findAttributeValue(container, "urn:dktk:dataelement:78:1"));
    assertEquals(Optional.ofNullable(dktkPreN),
        findAttributeValue(container, "urn:dktk:dataelement:79:1"));
    assertEquals(Optional.ofNullable(dktkPreM),
        findAttributeValue(container, "urn:dktk:dataelement:80:1"));
    assertEquals(Optional.ofNullable(dktkTNMYS),
        findAttributeValue(container, "urn:dktk:dataelement:82:1"));
    assertEquals(Optional.ofNullable(dktkTNMRS),
        findAttributeValue(container, "urn:dktk:dataelement:81:1"));

  }

  private ObservationComponentComponent createCompontent(String fhirValue, String code,
      Observation observation) {
    ObservationComponentComponent comp = new ObservationComponentComponent();
    comp.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode(code);
    comp.getValueCodeableConcept().getCodingFirstRep().setCode(fhirValue);

    return comp;
  }

}
