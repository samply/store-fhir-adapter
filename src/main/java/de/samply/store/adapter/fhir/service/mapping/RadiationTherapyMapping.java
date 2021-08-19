package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.stereotype.Component;

@Component
public class RadiationTherapyMapping {

  private final FhirPathR4 fhirPathR4;

  public RadiationTherapyMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  public Container map(Procedure therapy) {
    var builder = new ContainerBuilder(fhirPathR4, therapy, "RadiationTherapy");

    builder.addAttributeOptional("Procedure.performed.start", DateTimeType.class,
        "urn:dktk:dataelement:77:1", DATE_STRING);
    builder.addAttributeOptional("Procedure.performed.end", DateTimeType.class,
        "urn:dktk:dataelement:78:1", DATE_STRING);

    return builder.build();
  }
}
