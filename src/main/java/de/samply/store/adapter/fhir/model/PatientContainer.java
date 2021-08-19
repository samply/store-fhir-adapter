package de.samply.store.adapter.fhir.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;

public class PatientContainer {

  private Patient patient;
  private Observation vitalState;
  private final Map<String, ConditionContainer> conditionContainers = new HashMap<>();
  private final List<Specimen> specimenList = new ArrayList<>();

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
    Objects.requireNonNull(vitalState);
    this.vitalState = vitalState;
  }

  public ConditionContainer getConditionContainer(String reference) {
    Objects.requireNonNull(reference);
    return conditionContainers.computeIfAbsent(reference, k -> new ConditionContainer());
  }

  public Collection<ConditionContainer> getConditionContainers() {
    return conditionContainers.values();
  }

  public void addCondition(ConditionContainer conditionContainer) {
    Objects.requireNonNull(conditionContainer);
    conditionContainers.put(conditionContainer.getCondition().getId(), conditionContainer);
  }

  public List<Specimen> getSpecimenList() {
    return specimenList;
  }

  public void addSpecimen(Specimen specimen) {
    Objects.requireNonNull(specimen);
    specimenList.add(specimen);
  }
}


