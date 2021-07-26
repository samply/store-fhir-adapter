package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.PrimitiveType;

public class ProgressMapping {
  private final FhirPathR4 fhirPathR4;


  public ProgressMapping(FhirPathR4 fhirPathR4) {
    this.fhirPathR4 = fhirPathR4;
  }

  public Container map(ClinicalImpression clinicalImpression) {

    var builder = new ContainerBuilder(fhirPathR4, clinicalImpression, "Progress");

    builder.addAttributeOptional("ClinicalImpression.effective", DateTimeType.class,
        "urn:dktk:dataelement:25:4",
        DATE_STRING);

    builder.addAttribute("ClinicalImpression.finding.itemReference.resolve().where(code.coding.code = 'LA4583-6').value.coding.code",
        CodeType.class, "urn:dktk:dataelement:72:2", PrimitiveType::getValue);

    builder.addAttribute("ClinicalImpression.finding.itemReference.resolve().where(code.coding.code = '21976-6').value.coding.code",
        CodeType.class, "urn:dktk:dataelement:24:3", PrimitiveType::getValue);

    builder.addAttribute("ClinicalImpression.finding.itemReference.resolve().where(code.coding.code = 'LA4370-8').value.coding.code",
        CodeType.class, "urn:dktk:dataelement:73:2", PrimitiveType::getValue);

    builder.addAttribute("ClinicalImpression.finding.itemReference.resolve().where(code.coding.code = '21907-1').value.coding.code",
        CodeType.class, "urn:dktk:dataelement:74:2", PrimitiveType::getValue);

    return builder.build();
  }


  }
