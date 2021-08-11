package de.samply.store.adapter.fhir.model;

import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Procedure;

public class ConditionContainer {

  public Condition getCondition() {
    return condition;
  }

  private final Condition condition;

  //Contains all Progress objects, which are connected through the problem to Condition
  // Has Surgery or Radiation or SystemTherarpy
  private final List<ClinicalImpression> clinicalImpressions = new ArrayList<>();

  //Contains all Histology objects from the Conditions evidence. The Histology has then a ref to the Gradings.
  private List<Observation> histologyObservations = new ArrayList<>();
  //Contains all Metastasis obejcts have a encounter which points to the condition. They have TMN Components through refs
  private List<Observation> metastasisObservations = new ArrayList<>();
  //Contians all Tmn objects from the Conditions stage.
  private List<Observation> tnmObservations = new ArrayList<>();

  private List<Procedure> procedureList = new ArrayList<>();

  private List<MedicationStatement> medicationStatementList = new ArrayList<>();

  public ConditionContainer(Condition condition) {
    this.condition = condition;
  }

  public void addClinicalImpression(ClinicalImpression clinicalImpression) {
    clinicalImpressions.add(clinicalImpression);
  }

  public void addHistology(Observation histology) {
    histologyObservations.add(histology);
  }

  public void addMedicationStatement(MedicationStatement medicationStatement) {
    medicationStatementList.add(medicationStatement);
  }

  public void addMetastasis(Observation meta) {
    metastasisObservations.add(meta);
  }

  public void addTNM(Observation tnm) {
    tnmObservations.add(tnm);
  }

  public void addProcedure(Procedure procedure) {
    procedureList.add(procedure);
  }
}
