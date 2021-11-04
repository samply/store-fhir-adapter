package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttrValue;
import static de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping.ICD_O_3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.model.ClinicalImpressionNode;
import de.samply.store.adapter.fhir.model.ConditionNode;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TumorMappingTest {

  private static final String ADT_Site =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS";
  private static final String EXTENSION_FERNMETASTASEN =
      "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Fernmetastasen";
  private static final String CONDITION_ID = "203030";
  private static final String SURGERY_ID = "203016";
  private static final String RADIATION_THERAPY_ID = "204845";

  private TumorMapping mapping;

  @Mock(lenient = true)
  private FhirPathR4 fhirPathEngine;

  @Mock(lenient = true)
  private HistologyMapping histologyMapping;

  @Mock(lenient = true)
  private MetastasisMapping metaMapping;

  @Mock(lenient = true)
  private SurgeryMapping surgeryMapping;

  @Mock(lenient = true)
  private RadiationTherapyMapping radiationTherapyMapping;

  @Mock(lenient = true)
  private ProgressMapping progressMapping;

  @Mock(lenient = true)
  private TnmMapping tnmMapping;

  @BeforeEach
  void setUp() {
    mapping = new TumorMapping(fhirPathEngine, histologyMapping, metaMapping, surgeryMapping,
        radiationTherapyMapping, progressMapping, tnmMapping);
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/tumorMappings.csv", numLinesToSkip = 1)
  void map_TumorConditionCSVFile(
      String FHIR_ICD10, String FHIR_ICD_O_3, String FHIR_ADT_Site, String DKTK_ICD10,
      String DKTK_ICD_O_3, String DKTK_site
  ) {
    var patient = new Patient();
    var condition = new Condition();
    condition.getBodySiteFirstRep().getCodingFirstRep().setSystem(ICD_O_3).setCode(FHIR_ICD10)
        .setVersion(FHIR_ICD_O_3);
    var coding = new Coding().setSystem(
            "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS")
        .setCode(FHIR_ADT_Site);
    condition.getBodySiteFirstRep().addCoding(coding);
    var conditionNode = new ConditionNode(patient, condition);
    when(fhirPathEngine.evaluateFirst(condition,
        "Condition.bodySite.coding.where(system = '" + ICD_O_3 + "').code",
        CodeType.class)).thenReturn(Optional.of(new CodeType(DKTK_ICD10)));
    when(fhirPathEngine.evaluateFirst(condition,
        "Condition.bodySite.coding.where(system = '" + ICD_O_3 + "').version",
        StringType.class)).thenReturn(Optional.of(new StringType(DKTK_ICD_O_3)));
    when(fhirPathEngine.evaluateFirst(condition,
        "Condition.bodySite.coding.where(system = '" + ADT_Site + "').code",
        CodeType.class)).thenReturn(Optional.of(new CodeType(DKTK_site)));

    var container = mapping.map(conditionNode);

    assertEquals(Optional.ofNullable(DKTK_ICD10), findAttrValue(container, "4:2"));
    assertEquals(Optional.ofNullable(DKTK_ICD_O_3), findAttrValue(container, "5:2"));
    assertEquals(Optional.ofNullable(DKTK_site), findAttrValue(container, "6:2"));
  }

  @Test
  void map_Histology() {
    var patient = new Patient();
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getEvidenceFirstRep().getDetailFirstRep().setReference("123");
    var conditionNode = new ConditionNode(patient, condition);
    var observation = new Observation();
    when(fhirPathEngine.evaluate(condition, "Condition.evidence.detail.resolve()",
        Observation.class)).thenReturn(
        List.of(observation));
    var histologyContainer = new Container();
    when(histologyMapping.map(observation)).thenReturn(histologyContainer);

    var container = mapping.map(conditionNode);

    assertEquals(List.of(histologyContainer), container.getContainer());
  }

  @Test
  void map_Surgery() {
    var patient = new Patient();
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    var surgery = new Procedure();
    surgery.setId(SURGERY_ID);
    surgery.getCategory().getCodingFirstRep().setCode("OP");
    var conditionNode = new ConditionNode(patient, condition, List.of(), List.of(surgery),
        List.of());
    var surgeryContainer = new Container();
    when(surgeryMapping.map(surgery)).thenReturn(surgeryContainer);

    var container = mapping.map(conditionNode);

    assertEquals("Progress", container.getContainer().get(0).getDesignation());
    assertEquals(List.of(surgeryContainer), container.getContainer().get(0).getContainer());
  }

  @Test
  void map_RadiationTherapy() {
    var patient = new Patient();
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    var radiationTherapy = new Procedure();
    radiationTherapy.setId(RADIATION_THERAPY_ID);
    radiationTherapy.getCategory().getCodingFirstRep().setCode("ST");
    var conditionNode = new ConditionNode(patient, condition, List.of(), List.of(radiationTherapy),
        List.of());
    var radiationTherapyContainer = new Container();
    when(radiationTherapyMapping.map(radiationTherapy)).thenReturn(radiationTherapyContainer);

    var container = mapping.map(conditionNode);

    assertEquals("Progress", container.getContainer().get(0).getDesignation());
    assertEquals(List.of(radiationTherapyContainer), container.getContainer().get(0).getContainer());
  }

  @Test
  void map_UnknownProcedure() {
    var patient = new Patient();
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    var procedure = new Procedure();
    var conditionNode = new ConditionNode(patient, condition, List.of(), List.of(procedure),
        List.of());

    var container = mapping.map(conditionNode);

    assertTrue(container.getContainer().isEmpty());
  }

  @Test
  void map_Metastasis() {
    var patient = new Patient();
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getStageFirstRep().getAssessmentFirstRep().setReference("123");
    var conditionNodeBuilder = new ConditionNode(patient, condition);
    var observation = new Observation();
    var metastasisContainer = new Container();
    when(fhirPathEngine.evaluate(condition, "Condition.stage.assessment.resolve()",
        Observation.class)).thenReturn(
        List.of(observation));
    when(metaMapping.map(observation)).thenReturn(metastasisContainer);

    var container = mapping.map(conditionNodeBuilder);

    assertEquals(List.of(metastasisContainer), container.getContainer());
  }

  @Test
  void map_TNM() {
    var patient = new Patient();
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getExtension()
        .add(new Extension().setUrl(EXTENSION_FERNMETASTASEN)
            .setValue(new Reference("Condition/C123")));
    var conditionNode = new ConditionNode(patient, condition);
    var observation = new Observation();
    var tnmContainer = new Container();
    when(fhirPathEngine.evaluate(condition,
        "Condition.extension.where(url = '" + EXTENSION_FERNMETASTASEN + "').value.resolve()",
        Observation.class)).thenReturn(
        List.of(observation));
    when(tnmMapping.map(observation)).thenReturn(tnmContainer);

    var container = mapping.map(conditionNode);

    assertEquals(List.of(tnmContainer), container.getContainer());
  }

  @Test
  void map_clinicalImpression() {
    var patient = new Patient();
    var condition = new Condition();
    condition.setId(CONDITION_ID);
    condition.getEvidenceFirstRep().getDetailFirstRep().setReference("123");
    var clinicalImpressionNode = new ClinicalImpressionNode(new ClinicalImpression());
    var conditionNode = new ConditionNode(patient, condition, List.of(), List.of(),
        List.of(clinicalImpressionNode));
    var progressContainer = new Container();
    when(progressMapping.map(clinicalImpressionNode)).thenReturn(progressContainer);

    var container = mapping.map(conditionNode);

    assertEquals(List.of(progressContainer), container.getContainer());
  }
}
