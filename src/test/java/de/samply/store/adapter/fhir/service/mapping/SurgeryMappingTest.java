package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttrValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Optional;
import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class SurgeryMappingTest {

  private static final String LOKALE_BEURTEILUNG_RESIDUALSTATUS_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS";
  private static final String GESAMTBEURTEILUNG_RESIDUALSTATUS_CS =
      "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS";

  private static final FhirContext fhirContext = FhirContext.forR4();

  private SurgeryMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new SurgeryMapping(new FhirPathR4(fhirContext, new EvaluationContext()));
  }

  @Disabled
  @ParameterizedTest
  @CsvFileSource(resources = "/surgeryMappings.csv", numLinesToSkip = 1)
  void map_CsvFile(String fhirLocalRest, String fhirTotalRest, String dktkLocalRest,
      String dktkTotalRest) {
    Procedure procedure = new Procedure();
    procedure.getOutcome().addCoding().setSystem(LOKALE_BEURTEILUNG_RESIDUALSTATUS_CS)
        .setCode(fhirLocalRest);
    procedure.getOutcome().addCoding().setSystem(GESAMTBEURTEILUNG_RESIDUALSTATUS_CS)
        .setCode(fhirTotalRest);

    var container = mapping.map(procedure);

    assertEquals("Surgery", container.getDesignation());
    assertEquals(Optional.of("true"), findAttrValue(container, "33:2"));
    assertEquals(Optional.of("X"), findAttrValue(container, "23:3"));
    assertEquals(Optional.ofNullable(dktkLocalRest), findAttrValue(container, "19:2"));
    assertEquals(Optional.ofNullable(dktkTotalRest), findAttrValue(container, "20:3"));
  }
}
