package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.stereotype.Component;

@Component
public class SurgeryMapping {

  private final FhirPathR4 fhirPathR4;


  public SurgeryMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  public Container map(Procedure procedure) {
    var builder = new ContainerBuilder(fhirPathR4, procedure, "Surgery");

    builder.addAttribute(
        "Procedure.outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code",
        CodeType.class,
        "urn:dktk:dataelement:19:2", PrimitiveType::getValue);
    builder.addAttribute(
        "Procedure.outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS').code",
        CodeType.class,
        "urn:dktk:dataelement:20:3", PrimitiveType::getValue);

    return builder.build();
  }
}
