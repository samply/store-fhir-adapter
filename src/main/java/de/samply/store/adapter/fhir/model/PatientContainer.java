package de.samply.store.adapter.fhir.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;

public class PatientContainer {

  private Patient patient;
  private Observation vitalState;
  private final List<ConditionContainer> conditionContainerList = new ArrayList<>();
  private final List<Specimen> specimenList  = new ArrayList<>();

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    Objects.requireNonNull(patient);
    this.patient = patient;
  }

  public Optional<Observation> getVitalState() {
    return Optional.ofNullable(vitalState);
  }

  public void setVitalState(Observation vitalState) {
    this.vitalState = vitalState;
  }

  public List<ConditionContainer> getConditionContainerList() {
    return conditionContainerList;
  }

  public void addCondition(ConditionContainer conditionContainer) {
    Objects.requireNonNull(conditionContainer);
    conditionContainerList.add(conditionContainer);
  }

  public List<Specimen> getSpecimenList() {
    return specimenList;
  }

  public void addSpecimen(Specimen specimen) {
    Objects.requireNonNull(specimen);
    specimenList.add(specimen);
  }

}


