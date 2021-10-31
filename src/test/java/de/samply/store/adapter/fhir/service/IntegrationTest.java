package de.samply.store.adapter.fhir.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.model.RootNode;
import de.samply.store.adapter.fhir.model.RootNodeBuilder;
import de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping;
import de.samply.store.adapter.fhir.service.mapping.HistologyMapping;
import de.samply.store.adapter.fhir.service.mapping.MetastasisMapping;
import de.samply.store.adapter.fhir.service.mapping.PatientMapping;
import de.samply.store.adapter.fhir.service.mapping.ProgressMapping;
import de.samply.store.adapter.fhir.service.mapping.QueryResultMapping;
import de.samply.store.adapter.fhir.service.mapping.SampleMapping;
import de.samply.store.adapter.fhir.service.mapping.TnmMapping;
import de.samply.store.adapter.fhir.service.mapping.TumorMapping;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.Test;

public class IntegrationTest {

  private static final String PSEUDONYM_ART_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS";
  private static final String VITAL_STATE_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS";

  private final FhirContext fhirContext = FhirContext.forR4();

  private RootNode buildRootNode() {
    var bundle = new Bundle();

    var patient = new Patient();
    patient.setId("211712");
    var localId = patient.addIdentifier();
    localId.getType().getCodingFirstRep().setSystem(PSEUDONYM_ART_CS).setCode("Lokal");
    localId.setValue("123");
    patient.setGender(AdministrativeGender.MALE);
    patient.setBirthDateElement(new DateType("1995-01-01"));
    bundle.addEntry().setResource(patient);

    var vital = new Observation();
    vital.setId("0001807807");
    String loinc = "http://loinc.org";
    vital.getCode().getCodingFirstRep().setSystem(loinc).setCode("75186-7");
    vital.getSubject().setReference("Patient/211712");
    vital.getValueCodeableConcept().getCodingFirstRep().setSystem(VITAL_STATE_CS).setCode("lebend");
    bundle.addEntry().setResource(vital);

    var spec1 = new Specimen();
    spec1.setId("bioid1a");
    spec1.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode("blood-plasma");
    spec1.getSubject().setReference("Patient/211712");
    spec1.getCollection().setCollected(new DateTimeType("2017-12-23"));
    bundle.addEntry().setResource(spec1);

    var spec2 = new Specimen();
    spec2.setId("bioid1b");
    spec2.getType().getCodingFirstRep()
        .setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode("whole-blood");
    spec2.getSubject().setReference("Patient/211712");
    spec2.getCollection().setCollected(new DateTimeType("2017-12-22"));
    bundle.addEntry().setResource(spec2);

    var condition = new Condition();
    condition.setId("0000001490");
    condition.getCode().getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/dimdi/icd-10-gm")
        .setVersion("2014").setCode("C61");
    condition.getBodySiteFirstRep().getCodingFirstRep()
        .setSystem("urn:oid:2.16.840.1.113883.6.43.1").setVersion("32").setCode("C61.9");
    condition.getBodySiteFirstRep().addCoding(
        new Coding().setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS")
            .setCode("T"));
    condition.getSubject().setReference("Patient/211712");
    condition.getOnsetAge().setValue(59);
    condition.setRecordedDate(new DateTimeType("2014-05-06").getValue());
    condition.getStageFirstRep().getAssessmentFirstRep()
        .setReference("Observation/2014-05-06-d1e182");
    condition.getEvidenceFirstRep().getDetailFirstRep()
        .setReference("Observation/2014-05-06-d1e166");
    condition.getExtension().add(new Extension().setUrl(
            "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Fernmetastasen")
        .setValue(new Reference("Observation/M1712")));
    bundle.addEntry().setResource(condition);

    var encounter = new Encounter();
    encounter.setId("C123");
    encounter.getSubject().setReference("Patient/211712");
    encounter.setDiagnosis(
        List.of(new DiagnosisComponent().setCondition(new Reference("Condition/0000001490"))));
    bundle.addEntry().setResource(encounter);

    var histo1 = new Observation();
    histo1.setId("2014-05-06-d1e166");
    histo1.getCode().getCodingFirstRep().setSystem(loinc).setCode("59847-4");
    histo1.getValueCodeableConcept().getCodingFirstRep()
        .setSystem("urn:oid:2.16.840.1.113883.6.43.1").setVersion("32").setCode("8140/3");
    histo1.getSubject().setReference("Patient/211712");
    histo1.getHasMemberFirstRep().setReference("Observation/d1e166");
    bundle.addEntry().setResource(histo1);

    var grading = new Observation();
    grading.setId("d1e166");
    grading.getCode().getCodingFirstRep().setSystem(loinc).setCode("59542-1");
    grading.getSubject().setReference("Patient/211712");
    grading.getValueCodeableConcept().getCodingFirstRep()
        .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GradingCS").setCode("3");
    bundle.addEntry().setResource(grading);

    var clinicalImpression = new ClinicalImpression();
    clinicalImpression.setId("d1e166");
    clinicalImpression.getSubject().setReference("Patient/211712");
    clinicalImpression.setProblem(List.of(new Reference("Condition/0000001490")));

    var metastatis = new Observation();
    metastatis.setId("M1712");
    metastatis.getCode().getCodingFirstRep().setSystem(loinc).setCode("21907-1");
    metastatis.getSubject().setReference("Patient/211712");
    metastatis.setEffective(new DateTimeType("1996-03-23T08:42:24+01:00"));
    metastatis.getValueCodeableConcept().getCodingFirstRep()
        .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/JNUCS").setCode("J");
    metastatis.getBodySite().getCodingFirstRep()
        .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/FMLokalisationCS").setCode("BRA");
    metastatis.setEncounter(new Reference("Encounter/C123"));
    bundle.addEntry().setResource(metastatis);

    var tmn = new Observation();
    tmn.setId("2014-05-06-d1e182");
    tmn.getSubject().setReference("Patient/211712");
    tmn.getCode().getCodingFirstRep().setSystem(loinc).setCode("21908-9");
    tmn.setEffective(new DateTimeType("2014-05-05"));
    tmn.getValueCodeableConcept().getCodingFirstRep()
        .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/UiccstadiumCS").setCode("IIC");
    bundle.addEntry().setResource(tmn);

    var obserCC1 = new ObservationComponentComponent();
    obserCC1.getCode().getCodingFirstRep().setSystem(loinc).setCode("21905-5");
    obserCC1.getValueCodeableConcept().getCodingFirstRep()
        .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMTCS").setCode("1");
    tmn.setComponent(List.of(obserCC1));
    bundle.addEntry().setResource(tmn);

    var surgery = new Procedure();
    surgery.setId("22112019");
    surgery.addExtension(
        "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-OPIntention",
        new CodeableConcept().getCodingFirstRep()
            .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/OPIntentionCS").setCode("K"));
    surgery.getCategory().getCodingFirstRep()
        .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTTherapieartCS").setCode("OP");
    surgery.getCode().getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/dimdi/ops")
        .setCode("5-604.41");
    surgery.getSubject().setReference("Patient/211712");
    surgery.setReasonReference(List.of(new Reference("Condition/0000001490")));
    surgery.getOutcome().getCodingFirstRep().setSystem(
            "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS")
        .setCode("R1");
    surgery.getOutcome().addCoding(new Coding().setSystem(
            "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS")
        .setCode("R1"));
    bundle.addEntry().setResource(surgery);

    var medicationStatement = new MedicationStatement();
    medicationStatement.setId("2017-03-15-d1e95");
    medicationStatement.addExtension(
        "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp",
        new CodeableConcept().getCodingFirstRep()
            .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTStellungOPCS")
            .setCode("A"));
    medicationStatement.addExtension(
        "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention",
        new CodeableConcept().getCodingFirstRep()
            .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS")
            .setCode("K"));
    medicationStatement.getSubject().setReference("Patient/211712");
    medicationStatement.setEffective(new Period().setStartElement(new DateTimeType("2017-03-15"))
        .setEndElement(new DateTimeType("2017-07-30")));
    medicationStatement.setReasonReference(List.of(new Reference("Condition/0000001490")));
    bundle.addEntry().setResource(medicationStatement);

    return RootNodeBuilder.fromBundle(bundle);
  }

  @Test
  void applicationTest() {
    RootNode rootNode = buildRootNode();
    FhirPathR4 fhirPath = new FhirPathR4(fhirContext,
        new EvaluationContext(rootNode.resources()));
    QueryResultMapping queryResultMapping = new QueryResultMapping(new PatientMapping(fhirPath,
        new DiagnosisMapping(fhirPath, new TumorMapping(fhirPath, new HistologyMapping(fhirPath),
            new MetastasisMapping(fhirPath),
            new ProgressMapping(fhirPath), new TnmMapping(fhirPath))),
        new SampleMapping(fhirPath)));

    var result = queryResultMapping.map(rootNode.patients());

    assertEquals(result.getPatient().get(0).getId(), "123");
    assertEquals(result.getPatient().get(0).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:1:3")).toList().get(0)
        .getValue().getValue(), "M");
    assertEquals(result.getPatient().get(0).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:26:4")).toList().get(0)
        .getValue().getValue(), "01.01.1995");
    assertEquals(result.getPatient().get(0).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:53:3")).toList().get(0)
        .getValue().getValue(), "lebend");
    assertEquals(result.getPatient().get(0).getContainer().size(), 3);
    assertEquals(result.getPatient().get(0).getContainer().get(0).getDesignation(), "Diagnosis");
    assertEquals(result.getPatient().get(0).getContainer().get(0).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:29:2")).toList().get(0)
        .getValue().getValue(), "C61");
    assertEquals(result.getPatient().get(0).getContainer().get(0).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:28:1")).toList().get(0)
        .getValue().getValue(), "59");
    assertEquals(result.getPatient().get(0).getContainer().get(0).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:83:3")).toList().get(0)
        .getValue().getValue(), "06.05.2014");
    assertEquals(result.getPatient().get(0).getContainer().get(0).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:3:2")).toList().get(0)
        .getValue().getValue(), "10 32 GM");
    assertEquals(result.getPatient().get(0).getContainer().get(0).getContainer().size(), 1);
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getDesignation(),
        "Tumor");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getAttribute()
            .stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:4:2")).toList().get(0)
            .getValue().getValue(), "C61.9");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getAttribute()
            .stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:5:2")).toList().get(0)
            .getValue().getValue(), "32");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getAttribute()
            .stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:6:2")).toList().get(0)
            .getValue().getValue(), "T");

    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer()
            .size(), 3);
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(0)
            .getDesignation(), "Histology");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(0)
            .getAttribute().size(), 3);
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(0)
            .getAttribute().stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:7:2"))
            .toList().get(0).getValue().getValue(), "8140/3");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(0)
            .getAttribute().stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:8:2"))
            .toList().get(0).getValue().getValue(), "32");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(0)
            .getAttribute().stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:9:2"))
            .toList().get(0).getValue().getValue(), "3");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(1)
            .getDesignation(), "Metastasis");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(1)
            .getAttribute().size(), 2);
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(1)
            .getAttribute().stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:77:1"))
            .toList().get(0).getValue().getValue(), "IIC");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(1)
            .getAttribute().stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:21:3"))
            .toList().get(0).getValue().getValue(), "05.05.2014");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(2)
            .getDesignation(), "TNM");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(2)
            .getAttribute().size(), 2);
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(2)
            .getAttribute().stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:2:3"))
            .toList().get(0).getValue().getValue(), "23.03.1996");
    assertEquals(
        result.getPatient().get(0).getContainer().get(0).getContainer().get(0).getContainer().get(2)
            .getAttribute().stream().filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:89:1"))
            .toList().get(0).getValue().getValue(), "J");
    assertEquals(result.getPatient().get(0).getContainer().get(1).getDesignation(), "Sample");
    assertEquals(result.getPatient().get(0).getContainer().get(1).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:97:1")).toList().get(0)
        .getValue().getValue(), "Plasma");
    assertEquals(result.getPatient().get(0).getContainer().get(1).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:50:2")).toList().get(0)
        .getValue().getValue(), "true");
    assertEquals(result.getPatient().get(0).getContainer().get(1).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:95:2")).toList().get(0)
        .getValue().getValue(), "Flüssigprobe");
    assertEquals(result.getPatient().get(0).getContainer().get(1).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:49:4")).toList().get(0)
        .getValue().getValue(), "23.12.2017");
    assertEquals(result.getPatient().get(0).getContainer().get(2).getDesignation(), "Sample");
    assertEquals(result.getPatient().get(0).getContainer().get(2).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:97:1")).toList().get(0)
        .getValue().getValue(), "Vollblut");
    assertEquals(result.getPatient().get(0).getContainer().get(2).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:50:2")).toList().get(0)
        .getValue().getValue(), "true");
    assertEquals(result.getPatient().get(0).getContainer().get(2).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:95:2")).toList().get(0)
        .getValue().getValue(), "Flüssigprobe");
    assertEquals(result.getPatient().get(0).getContainer().get(2).getAttribute().stream()
        .filter(a -> a.getMdrKey().equals("urn:dktk:dataelement:49:4")).toList().get(0)
        .getValue().getValue(), "22.12.2017");
  }
}
