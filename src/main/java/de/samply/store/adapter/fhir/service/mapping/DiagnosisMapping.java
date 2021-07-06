package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.share.model.ccp.ObjectFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;

/**
 * Mapping of the Diagnosis container.
 *
 * @author Alexander Kiel
 */
@Component
public class DiagnosisMapping {

  public static final String ICD_10_GM = "http://fhir.de/CodeSystem/dimdi/icd-10-gm";
  public static final String ICD_O_3 = "urn:oid:2.16.840.1.113883.6.43.1";
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  private final ObjectFactory objectFactory = new ObjectFactory();
  private final FhirPathR4 fhirPathR4;

  public DiagnosisMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  /**
   * Maps the Condition resource into a Diagnosis container.
   *
   * @param condition the Condition resource to map
   * @return the Diagnosis container
   */
  public Container map(Condition condition) {
    var builder = new ContainerBuilder(condition, "Diagnosis");

    builder.addAttribute("Condition.code.coding.where(system = '" + ICD_10_GM + "').code",
        CodeType.class, "urn:dktk:dataelement:29:2", PrimitiveType::getValue);

    builder.addAttribute("Condition.onset", DateTimeType.class, "urn:dktk:dataelement:83:3",
        dateTime -> LocalDate.parse(dateTime.getValueAsString().substring(0, 10))
            .format(DATE_FORMATTER));

    builder.addAttribute("Condition.bodySite.coding.where(system = '" + ICD_O_3 + "').code",
        CodeType.class, "urn:dktk:dataelement:4:2", PrimitiveType::getValue);

    return builder.build();
  }

  class ContainerBuilder {

    private final Container container = objectFactory.createContainer();
    private final Resource resource;

    ContainerBuilder(Resource resource, String designation) {
      this.resource = resource;
      container.setDesignation(designation);
      container.setId(resource.getResourceType() + resource.getIdElement().getIdPart());
    }

    <T extends IBase> void addAttribute(String path, Class<T> type, String mdrKey,
        Function<T, String> toString) {
      fhirPathR4.evaluateFirst(resource, path, type)
          .map(toString)
          .map(v -> Util.createAttribute(mdrKey, v))
          .ifPresent(a -> container.getAttribute().add(a));
    }

    Container build() {
      return container;
    }
  }
}
