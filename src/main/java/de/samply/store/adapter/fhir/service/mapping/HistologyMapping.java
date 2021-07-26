package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

@Component
public class HistologyMapping {

  private final FhirPathR4 fhirPathR4;

  public HistologyMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  public Container map(Observation histology) {

    var builder = new ContainerBuilder(fhirPathR4, histology, "Histology");

    builder.addAttribute("Observation.value.coding.where(system = '" + "urn:oid:2.16.840.1.113883.6.43.1" + "').code",
        CodeType.class, "urn:dktk:dataelement:7:2", PrimitiveType::getValue);

    builder.addAttribute("Observation.value.coding.where(system = '" + "urn:oid:2.16.840.1.113883.6.43.1" + "').version",
        StringType.class, "urn:dktk:dataelement:8:2", PrimitiveType::getValue);

    builder.addAttribute("Observation.hasMember.resolve().value.coding.code",
        CodeType.class, "urn:dktk:dataelement:9:2", PrimitiveType::getValue);


    return builder.build();

  }
}
