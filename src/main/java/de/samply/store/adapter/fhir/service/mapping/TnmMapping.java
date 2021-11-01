package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.mapping.Util.DATE_STRING;

import de.samply.share.model.ccp.Container;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import java.util.Optional;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

/**
 * Mapping of FHIR Observation to MDS TNM container.
 */
@Component
public class TnmMapping {

  private static final String CPU_PRAEFIX_URL =
      "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix";

  private static final String TNM_C = "21908-9";
  private static final String TNM_P = "21902-2";

  private static final String TNM_T_C = "21905-5";
  private static final String TNM_T_P = "21899-0";

  private static final String TNM_N_C = "21906-3";
  private static final String TNM_N_P = "21900-6";

  private static final String TNM_M_C = "21907-1";
  private static final String TNM_M_P = "21901-4";

  private static final String TNM_Y_S = "59479-6";
  private static final String TNM_R_S = "21983-2";
  private static final String TNM_M_S = "42030-7";

  private final FhirPathR4 fhirPathEngine;

  /**
   * Creates a new TnmMapping.
   *
   * @param fhirPathEngine the FHIRPath engine
   */
  public TnmMapping(FhirPathR4 fhirPathEngine) {
    this.fhirPathEngine = Objects.requireNonNull(fhirPathEngine);
  }

  /**
   * Maps FHIR TNM Observation to MDS TNM container.
   *
   * @param tnm the FHIR TNM Observation
   * @return the MDS TNM container
   */
  public Container map(Observation tnm) {
    var builder = new ContainerBuilder(fhirPathEngine, tnm, "TNM");

    builder.addAttributeOptional("Observation.effective", DateTimeType.class,
        "urn:dktk:dataelement:2:3", DATE_STRING);

    builder.addAttribute("Observation.value.coding.version", StringType.class,
        "urn:dktk:dataelement:18:2", PrimitiveType::getValue);

    builder.addAttribute("Observation.value.coding.code", CodeType.class,
        "urn:dktk:dataelement:89:1", PrimitiveType::getValue);

    // TNM-T
    var codeT = getTnmCode(tnm, TNM_T_C, TNM_T_P);
    builder.addAttribute(componentCpuExtensionValuePath(codeT), CodeType.class,
        "urn:dktk:dataelement:78:1", PrimitiveType::getValue);
    builder.addAttribute(componentValuePath(codeT), CodeType.class,
        "urn:dktk:dataelement:100:1", PrimitiveType::getValue);

    // TNM-N
    var codeN = getTnmCode(tnm, TNM_N_C, TNM_N_P);
    builder.addAttribute(componentCpuExtensionValuePath(codeN), CodeType.class,
        "urn:dktk:dataelement:79:1", PrimitiveType::getValue);
    builder.addAttribute(componentValuePath(codeN), CodeType.class,
        "urn:dktk:dataelement:101:1", PrimitiveType::getValue);

    // TNM-M
    var codeM = getTnmCode(tnm, TNM_M_C, TNM_M_P);
    builder.addAttribute(componentCpuExtensionValuePath(codeM), CodeType.class,
        "urn:dktk:dataelement:80:1", PrimitiveType::getValue);
    builder.addAttribute(componentValuePath(codeM), CodeType.class,
        "urn:dktk:dataelement:99:1", PrimitiveType::getValue);

    // TNM-y-Symbol
    builder.addAttribute(componentValuePath(TNM_Y_S), CodeType.class,
        "urn:dktk:dataelement:82:1", PrimitiveType::getValue);

    // TNM-r-Symbol
    builder.addAttribute(componentValuePath(TNM_R_S), CodeType.class,
        "urn:dktk:dataelement:81:1", PrimitiveType::getValue);

    // TNM-m-Symbol
    builder.addAttribute(componentValuePath(TNM_M_S), CodeType.class,
        "urn:dktk:dataelement:10:2", PrimitiveType::getValue);

    return builder.build();
  }

  private String getTnmCode(Observation tnm, String codeC, String codeP) {
    return getCode(tnm).flatMap(code -> switch (code) {
      case TNM_C -> Optional.of(codeC);
      case TNM_P -> Optional.of(codeP);
      default -> Optional.empty();
    }).orElse(codeC);
  }

  private Optional<String> getCode(Observation tnm) {
    return fhirPathEngine.evaluateFirst(tnm,
            "Observation.code.coding.where(system = 'http://loinc.org').code", CodeType.class)
        .map(CodeType::getCode);
  }

  private static String componentValuePath(String code) {
    return componentPath(code) + ".value.coding.code";
  }

  private static String componentCpuExtensionValuePath(String code) {
    return componentPath(code) + ".extension('" + CPU_PRAEFIX_URL + "').value.coding.code";
  }

  private static String componentPath(String code) {
    return "Observation.component.where(code.coding.code = '" + code + "')";
  }
}
