package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttrValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Optional;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class TnmMappingTest {

  private static final String CPU_PRAEFIX_URL =
      "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix";
  private static final String TNMCPU_PRAEFIX_TCS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS";

  private static final String TNM_C = "21908-9";
  private static final String TNM_P = "21902-2";

  private static final FhirContext fhirContext = FhirContext.forR4();

  private TnmMapping mapping;
  private Observation observation;

  @BeforeEach
  void setUp() {
    mapping = new TnmMapping(new FhirPathR4(fhirContext, new EvaluationContext()));
    observation = new Observation();
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/tnmMappings.csv", numLinesToSkip = 1)
  void map_TumorConditionCSVFile(String fhirTNMDate, String fhirUICC, String fhirTNMT,
      String fhirTNMMS, String fhirTNMN, String fhirTNMM, String fhirPreT, String fhirPreN,
      String fhirPreM, String fhirTNMYS, String fhirTNMRS, String fhirTNMVersion,
      String dktkTNMDate, String dktkUICC, String dktkTNMT, String dktkTNMMS, String dktkTNMN,
      String dktkTNMM, String dktkPreT, String dktkPreN, String dktkPreM, String dktkTNMYS,
      String dktkTNMRS, String dktkTNMVersion) {

    observation.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode(TNM_C);
    observation.setEffective(new DateTimeType(fhirTNMDate));
    observation.getValueCodeableConcept().getCodingFirstRep().setVersion(fhirTNMVersion)
        .setCode(fhirUICC);

    if (fhirTNMT != null || fhirPreT != null) {
      var comp = createCompontent("21905-5", fhirTNMT);
      comp.getExtensionFirstRep().setUrl(CPU_PRAEFIX_URL);
      comp.getExtensionFirstRep().setValue(createCpuConcept(fhirPreT));
      observation.addComponent(comp);
    }

    if (fhirTNMMS != null || fhirTNMN != null) {
      observation.addComponent(createCompontent("42030-7", fhirTNMMS));
    }

    if (fhirTNMN != null || fhirPreN != null) {
      var comp = createCompontent("21906-3", fhirTNMN);
      comp.getExtensionFirstRep().setUrl(CPU_PRAEFIX_URL);
      comp.getExtensionFirstRep().setValue(createCpuConcept(fhirPreN));
      observation.addComponent(comp);
    }

    if (fhirTNMM != null || fhirPreM != null) {
      var comp = createCompontent("21907-1", fhirTNMM);
      comp.getExtensionFirstRep().setUrl(CPU_PRAEFIX_URL);
      comp.getExtensionFirstRep().setValue(createCpuConcept(fhirPreM));
      observation.addComponent(comp);
    }

    if (fhirTNMYS != null) {
      observation.addComponent(createCompontent("59479-6", fhirTNMYS));
    }

    if (fhirTNMRS != null) {
      observation.addComponent(createCompontent("21983-2", fhirTNMRS));
    }

    var container = mapping.map(observation);

    assertEquals(Optional.ofNullable(dktkTNMDate), findAttrValue(container, "2:3"));
    assertEquals(Optional.ofNullable(dktkUICC), findAttrValue(container, "89:1"));
    assertEquals(Optional.ofNullable(dktkTNMVersion), findAttrValue(container, "18:2"));

    assertEquals(Optional.ofNullable(dktkPreT), findAttrValue(container, "78:1"));
    assertEquals(Optional.ofNullable(dktkTNMT), findAttrValue(container, "100:1"));

    assertEquals(Optional.ofNullable(dktkPreN), findAttrValue(container, "79:1"));
    assertEquals(Optional.ofNullable(dktkTNMN), findAttrValue(container, "101:1"));

    assertEquals(Optional.ofNullable(dktkPreM), findAttrValue(container, "80:1"));
    assertEquals(Optional.ofNullable(dktkTNMM), findAttrValue(container, "99:1"));

    assertEquals(Optional.ofNullable(dktkTNMYS), findAttrValue(container, "82:1"));
    assertEquals(Optional.ofNullable(dktkTNMRS), findAttrValue(container, "81:1"));
    assertEquals(Optional.ofNullable(dktkTNMMS), findAttrValue(container, "10:2"));
  }

  private static ObservationComponentComponent createCompontent(String code, String value) {
    ObservationComponentComponent comp = new ObservationComponentComponent();
    comp.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode(code);
    comp.getValueCodeableConcept().getCodingFirstRep().setCode(value);
    return comp;
  }

  private CodeableConcept createCpuConcept(String code) {
    var concept = new CodeableConcept();
    concept.getCodingFirstRep().setSystem(TNMCPU_PRAEFIX_TCS).setCode(code);
    return concept;
  }
}
