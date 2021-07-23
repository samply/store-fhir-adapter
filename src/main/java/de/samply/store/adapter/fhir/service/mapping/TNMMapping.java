package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.MyIEvaluationContext;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;

public class TNMMapping {

  private final FhirPathR4 fhirPathR4;

  public TNMMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  public Container map(Observation tnm) {
    var builder = new ContainerBuilder(fhirPathR4, tnm, "TNM");

    builder.addAttributeOptional("Observation.effective", DateTimeType.class,
        "urn:dktk:dataelement:2:3",
        DATE_STRING);
    builder.addAttribute("Observation.value.coding.version", StringType.class,
        "urn:dktk:dataelement:18:2", PrimitiveType::getValue);
    builder.addAttribute("Observation.value.coding.code", CodeType.class,
        "urn:dktk:dataelement:89:1", PrimitiveType::getValue);
    builder.addAttribute("Observation.component.where(code.coding.code = '21905-5').value.coding.code", CodeType.class,
        "urn:dktk:dataelement:100:1", PrimitiveType::getValue);
    builder.addAttribute("Observation.component.where(code.coding.code = '201906-3').value.coding.code", CodeType.class,
        "urn:dktk:dataelement:101:1", PrimitiveType::getValue);
    builder.addAttribute("Observation.component.where(code.coding.code = '21907-1').value.coding.code", CodeType.class,
        "urn:dktk:dataelement:99:1", PrimitiveType::getValue);
    builder.addAttribute("Observation.component.where(code.coding.code = '21983-2').value.coding.code", CodeType.class,
        "urn:dktk:dataelement:81:1", PrimitiveType::getValue);
    builder.addAttribute("Observation.component.where(code.coding.code = '42030-7').value.coding.code", CodeType.class,
        "urn:dktk:dataelement:10:2", PrimitiveType::getValue);
    builder.addAttribute("Observation.component.where(code.coding.code = '59479-6').value.coding.code", CodeType.class,
        "urn:dktk:dataelement:82:1", PrimitiveType::getValue);

    //Extensions
    builder.addAttribute("Observation.component.where(code.coding.code = '21905-5').extension.value.coding.code", CodeType.class,
        "urn:dktk:dataelement:78:1", PrimitiveType::getValue);
    builder.addAttribute("Observation.component.where(code.coding.code = '201906-3').extension.value.coding.code", CodeType.class,
        "urn:dktk:dataelement:79:1", PrimitiveType::getValue);
    builder.addAttribute("Observation.component.where(code.coding.code = '21907-1').extension.value.coding.code", CodeType.class,
        "urn:dktk:dataelement:80:1", PrimitiveType::getValue);

    return builder.build();
  }
}
