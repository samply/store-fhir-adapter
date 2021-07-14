package de.samply.store.adapter.fhir.service;

import static de.samply.store.adapter.fhir.service.mapping.Util.createAttribute;

import de.samply.share.model.ccp.Attribute;
import de.samply.share.model.ccp.ObjectFactory;
import de.samply.share.model.ccp.QueryResult;
import de.samply.store.adapter.fhir.service.mapping.DiagnosisMapping;
import de.samply.store.adapter.fhir.service.mapping.SampleMapping;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;
import org.springframework.stereotype.Service;

/**
 * A service for the mapping of FHIR resources to {@link QueryResult QueryResults}.
 *
 * @author Alexander Kiel
 */
@Service
public class MappingService {

  public static final String ICD_10_GM = "http://fhir.de/CodeSystem/dimdi/icd-10-gm";

  private final ObjectFactory objectFactory = new ObjectFactory();
  private final DiagnosisMapping diagnosisMapping;
  private final SampleMapping sampleMapping;

  public MappingService(DiagnosisMapping diagnosisMapping, SampleMapping sampleMapping) {
    this.diagnosisMapping = diagnosisMapping;
    this.sampleMapping = sampleMapping;
  }

  /**
   * Partitions the resources in {@code bundle} by patient.
   *
   * @param bundle the bundle to partition
   * @return a list of lists of all resources of one patient. The patient is the first element in
   * each inner lists.
   */
  private static Collection<List<Resource>> partitionByPatient(Bundle bundle) {
    var result = new HashMap<String, List<Resource>>();
    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      switch (entry.getResource().getResourceType()) {
        case Patient:
          var patientResources = new ArrayList<Resource>();
          var patient = entry.getResource();
          patientResources.add(patient);
          result.put(patient.getIdElement().getIdPart(), patientResources);
          break;
        case Observation:
          var observation = (Observation) entry.getResource();
          result.get(observation.getSubject().getReferenceElement().getIdPart()).add(observation);
          break;
        case Condition:
          var condition = (Condition) entry.getResource();
          result.get(condition.getSubject().getReferenceElement().getIdPart()).add(condition);
          break;
        default:
      }
    }
    return result.values();
  }

  /**
   * Map's a {@code Bundle} of a result page to a {@code QueryResult}.
   *
   * @param bundle the bundle to map
   * @return the mapped {@code QueryResult}
   */
  public QueryResult map(Bundle bundle) {
    var result = new QueryResult();

    for (List<Resource> resources : partitionByPatient(bundle)) {
      var dktkPatient = objectFactory.createPatient();

      var patient = (Patient) resources.get(0);
      dktkPatient.setId(patient.getIdElement().getIdPart());

      // gender
      var gender = patient.getGender();
      if (gender != null) {
        dktkPatient.getAttribute().add(
            createAttribute("urn:dktk:dataelement:1:3", mapGenderValue(gender)));
      }

      // birthDate
      LocalDate birthDate = null;
      if (patient.hasBirthDate()) {
        birthDate = LocalDate.parse(patient.getBirthDateElement().getValueAsString());
        dktkPatient.getAttribute().add(
            createAttribute("urn:dktk:dataelement:26:4", mapDateValue(birthDate)));
      }

      final AtomicReference<Condition> firstConditionRef = new AtomicReference<>();

      // other resources (skip Patient resource)
      resources.stream().skip(1).forEach(resource -> {
        switch (resource.getResourceType()) {
          case Observation:
            var observation = (Observation) resource;
            var code = findFirstCode(observation.getCode(), "http://loinc.org");
            if (code.isPresent()) {
              switch (code.get()) {
                case "75186-7": {
                  mapVitalStatus(observation.getValueCodeableConcept())
                      .ifPresent(value -> dktkPatient.getAttribute().add(value));
                  break;
                }
                default: {
                }
              }
            }
            break;

          case Condition:
            var condition = (Condition) resource;

            dktkPatient.getContainer().add(diagnosisMapping.map(condition, patient));
            break;

          case Specimen:
            dktkPatient.getContainer().add(sampleMapping.map((Specimen) resource));
            break;

          default:
        }
      });

      result.getPatient().add(dktkPatient);
    }
    return result;
  }

  private Optional<LocalDate> onsetAsLocalDate(Condition condition) {
    return onsetAsString(condition).map(s -> s.substring(0, 10)).map(LocalDate::parse);
  }

  private Optional<String> onsetAsString(Condition condition) {
    return condition.hasOnsetDateTimeType()
        ? Optional.of(condition.getOnsetDateTimeType().getValueAsString())
        : Optional.empty();
  }

  private Optional<String> findFirstCode(CodeableConcept concept, String system) {
    return concept.getCoding().stream()
        .filter(c -> system.equals(c.getSystem()))
        .map(Coding::getCode)
        .findFirst();
  }

  private String mapGenderValue(AdministrativeGender gender) {
    switch (gender) {
      case MALE:
        return "M";
      case FEMALE:
        return "W";
      case OTHER:
        return "S";
      default:
        return "U";
    }
  }

  private String mapDateValue(LocalDate date) {
    return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
  }

  private Optional<Attribute> mapVitalStatus(CodeableConcept value) {
    return findFirstCode(value, "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS")
        .map(code -> createAttribute("urn:dktk:dataelement:53:3", code));
  }
}
