package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping.ICD_O_3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.model.ConditionContainer;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Patrick Skowronek
 */

@ExtendWith(MockitoExtension.class)
public class TumorMappingTest {

  private static final String ADT_Site = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS";
  private final String url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Fernmetastasen";

  private TumorMapping mapping;

  @Mock(lenient = true)
  private FhirPathR4 fhirPathEngine;

  @Mock(lenient = true)
  private HistologyMapping histologyMapping;

  @Mock(lenient = true)
  private ProgressMapping progressMapping;

  @Mock(lenient = true)
  private MetastasisMapping metaMapping;

  @Mock(lenient = true)
  private TNMMapping tnmMapping;

  @BeforeEach
  void setUp() {
    mapping = new TumorMapping(fhirPathEngine, histologyMapping, metaMapping, progressMapping, tnmMapping);
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/tumorMappings.csv", numLinesToSkip = 1)
  void map_TumorConditionCSVFile(
      String FHIR_ICD10, String FHIR_ICD_O_3, String FHIR_ADT_Site, String DKTK_ICD10,
      String DKTK_ICD_O_3, String DKTK_site
  ) {
    ConditionContainer conditionContainer = new ConditionContainer();
    var condition = new Condition();
    condition.getBodySiteFirstRep().getCodingFirstRep().setSystem(ICD_O_3).setCode(FHIR_ICD10)
        .setVersion(FHIR_ICD_O_3);
    var coding = new Coding().setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS")
        .setCode(FHIR_ADT_Site);
    condition.getBodySiteFirstRep().addCoding(coding);
    conditionContainer.setCondition(condition);
    when(fhirPathEngine.evaluateFirst(condition, "Condition.bodySite.coding.where(system = '" + ICD_O_3 + "').code",
        CodeType.class)).thenReturn(Optional.of(new CodeType(DKTK_ICD10)));
    when(fhirPathEngine.evaluateFirst(condition, "Condition.bodySite.coding.where(system = '" + ICD_O_3 + "').version",
        StringType.class)).thenReturn(Optional.of(new StringType(DKTK_ICD_O_3)));
    when(fhirPathEngine.evaluateFirst(condition, "Condition.bodySite.coding.where(system = '" + ADT_Site + "').code",
        CodeType.class)).thenReturn(Optional.of(new CodeType(DKTK_site)));

    var container = mapping.map(conditionContainer);

    assertEquals(Optional.ofNullable(DKTK_ICD10),
        findAttributeValue(container, "urn:dktk:dataelement:4:2"));
    assertEquals(Optional.ofNullable(DKTK_ICD_O_3),
        findAttributeValue(container, "urn:dktk:dataelement:5:2"));
    assertEquals(Optional.ofNullable(DKTK_site),
        findAttributeValue(container, "urn:dktk:dataelement:6:2"));
  }

  @Test
  void map_Histology(){
    ConditionContainer conditionContainer = new ConditionContainer();
    var condition = new Condition();
    condition.setId("C123");
    conditionContainer.setCondition(condition);
    condition.getEvidenceFirstRep().getDetailFirstRep().setReference("123");
    var histoContainer = new Container();
    Observation observation = new Observation();
    when(fhirPathEngine.evaluate(condition, "Condition.evidence.detail.resolve()", Observation.class)).thenReturn(
        List.of(observation));
    when(histologyMapping.map(observation)).thenReturn(histoContainer);

    var container = mapping.map(conditionContainer);

    assertEquals(List.of(histoContainer), container.getContainer());
  }

  @Test
  void map_Metastasis(){
    ConditionContainer conditionContainer = new ConditionContainer();
    var condition = new Condition();
    condition.setId("C123");
    conditionContainer.setCondition(condition);
    condition.getStageFirstRep().getAssessmentFirstRep().setReference("123");
    var metastasisContainer = new Container();
    Observation observation = new Observation();
    when(fhirPathEngine.evaluate(condition, "Condition.stage.assessment.resolve()", Observation.class)).thenReturn(
        List.of(observation));
    when(metaMapping.map(observation)).thenReturn(metastasisContainer);

    var container = mapping.map(conditionContainer);

    assertEquals(List.of(metastasisContainer), container.getContainer());
  }

  @Test
  void map_TNM(){
    ConditionContainer conditionContainer = new ConditionContainer();
    var condition = new Condition();
    condition.setId("C123");
    conditionContainer.setCondition(condition);
    condition.getExtension().add(new Extension().setUrl(url).setValue(new Reference("Condition/C123")));
    var tnmContainer = new Container();
    Observation observation = new Observation();
    when(fhirPathEngine.evaluate(condition, "Condition.extension.where(url = '" + url + "').value.resolve()", Observation.class)).thenReturn(
        List.of(observation));
    when(tnmMapping.map(observation)).thenReturn(tnmContainer);

    var container = mapping.map(conditionContainer);

    assertEquals(List.of(tnmContainer), container.getContainer());
  }

  @Test
  void map_clinicalImpression(){
    ConditionContainer conditionContainer = new ConditionContainer();
    var condition = new Condition();
    condition.setId("C123");
    conditionContainer.setCondition(condition);
    condition.getEvidenceFirstRep().getDetailFirstRep().setReference("123");
    var progressContainer = new Container();
    ClinicalImpression progress = new ClinicalImpression();
    conditionContainer.getClinicalImpressionContainer("Condition/C123").setClinicalImpression(progress);
    when(progressMapping.map(progress)).thenReturn(progressContainer);

    var container = mapping.map(conditionContainer);

    assertEquals(List.of(progressContainer), container.getContainer());
  }
}
