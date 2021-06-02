package de.samply.store.adapter.fhir.service;

import de.samply.share.model.ccp.Attribute;
import de.samply.share.model.ccp.ObjectFactory;
import de.samply.share.model.ccp.QueryResult;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Service;

/**
 * A service for the mapping of FHIR resources to {@link QueryResult QueryResults}.
 *
 * @author Alexander Kiel
 */
@Service
public class MappingService {

  private final ObjectFactory objectFactory = new ObjectFactory();

  /**
   * Map's a {@code Bundle} of a result page to a {@code QueryResult}.
   *
   * @param bundle the bundle to map
   * @return the mapped {@code QueryResult}
   */
  public QueryResult map(Bundle bundle) {
    var result = new QueryResult();

    for (List<Resource> resources : partitionByPatient(bundle)) {
      var patient = objectFactory.createPatient();

      var patientResource = (Patient) resources.get(0);
      patient.setId(patientResource.getIdElement().getIdPart());

      // gender
      var gender = patientResource.getGender();
      if (gender != null) {
        patient.getAttribute().add(mapGender(gender));
      }

      // birthDate
      var birthDate = patientResource.getBirthDate();
      if (birthDate != null) {
        patient.getAttribute().add(mapBirthDate(birthDate));
      }

      // other resources
      resources.stream().skip(1).forEach(resource -> {
        switch (resource.getResourceType()) {
          case Observation: {
            var observation = (Observation) resource;
            var code = findFirstCode(observation.getCode(), "http://loinc.org");
            if (code.isPresent()) {
              switch (code.get()) {
                case "75186-7": {
                  mapVitalStatus(observation.getValueCodeableConcept())
                      .ifPresent(value -> patient.getAttribute().add(value));
                  break;
                }
                default: {
                }
              }
            }
            break;
          }
          case Condition: {
            var condition = (Condition) resource;
            var code = findFirstCode(condition.getCode(), "http://fhir.de/CodeSystem/dimdi/icd-10-gm");
            code.ifPresent(value -> patient.getAttribute().add(mapConditionCode(value)));
            patient.getAttribute().add(mapConditionDate(condition.getOnsetDateTimeType()));
            break;
          }
          default: {
          }
        }
      });

      result.getPatient().add(patient);
    }
    return result;
  }

  /**
   * Partitions the resources in {@code bundle} by patient.
   *
   * @param bundle the bundle to partition
   * @return a list of lists of all resources of one patient. The patient is the first element in
   *     each inner lists.
   */
  private List<List<Resource>> partitionByPatient(Bundle bundle) {
    var result = new LinkedList<List<Resource>>();
    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      if (ResourceType.Patient == entry.getResource().getResourceType()) {
        var patientResources = new ArrayList<Resource>();
        patientResources.add(entry.getResource());
        result.add(patientResources);
      } else {
        result.getLast().add(entry.getResource());
      }
    }
    return result;
  }

  private Optional<String> findFirstCode(CodeableConcept concept, String system) {
    return concept.getCoding().stream()
        .filter(c -> system.equals(c.getSystem()))
        .map(Coding::getCode)
        .findFirst();
  }

  private Attribute mapGender(AdministrativeGender gender) {
    var attribute = objectFactory.createAttribute();
    attribute.setMdrKey("urn:dktk:dataelement:1:3");
    attribute.setValue(objectFactory.createValue(mapGenderValue(gender)));
    return attribute;
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

  private Attribute mapBirthDate(Date birthDate) {
    var attribute = objectFactory.createAttribute();
    attribute.setMdrKey("urn:dktk:dataelement:26:4");
    attribute.setValue(objectFactory.createValue(mapDateValue(birthDate)));
    return attribute;
  }

  private String mapDateValue(Date date) {
    return new SimpleDateFormat("dd.MM.yyyy").format(date);
  }

  private Optional<Attribute> mapVitalStatus(CodeableConcept value) {
    return findFirstCode(value, "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS")
        .map(code -> {
          var attribute = objectFactory.createAttribute();
          attribute.setMdrKey("urn:dktk:dataelement:53:3");
          attribute.setValue(objectFactory.createValue(code));
          return attribute;
        });
  }

  private Attribute mapConditionCode(String code) {
    var attribute = objectFactory.createAttribute();
    attribute.setMdrKey("urn:dktk:dataelement:29:2");
    attribute.setValue(objectFactory.createValue(code));
    return attribute;
  }

  private Attribute mapConditionDate(DateTimeType dateTimeType) {
    var attribute = objectFactory.createAttribute();
    attribute.setMdrKey("urn:dktk:dataelement:83:3");
    attribute.setValue(objectFactory.createValue(mapDateValue(dateTimeType.getValue())));
    return attribute;
  }
}
