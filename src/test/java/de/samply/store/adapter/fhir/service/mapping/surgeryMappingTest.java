package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.MyIEvaluationContext;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * @author Patrick Skowronek
 */

public class surgeryMappingTest {

  private SurgeryMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new SurgeryMapping(new FhirPathR4(FhirContext.forR4(), new MyIEvaluationContext()));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/surgeryMappings.csv", numLinesToSkip = 1)
  void map_HistologyObservationCSVFile(String fhirLocalRest, String fhirTotalRest, String dktkLocalRest, String dktkTotalRest
      ) {
    Procedure procedure = new Procedure();
    CodeableConcept codeCon = new CodeableConcept();
    Coding codeLocal = new Coding();
    codeLocal.setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS").setCode(fhirLocalRest);
    Coding codeTotal = new Coding();
    codeTotal.setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS").setCode(fhirTotalRest);
    codeCon.setCoding(List.of(codeLocal,codeTotal));
    procedure.setOutcome(codeCon);

    var container = mapping.map(procedure);

    assertEquals(Optional.ofNullable(dktkLocalRest),
        findAttributeValue(container, "urn:dktk:dataelement:19:2"));
    assertEquals(Optional.ofNullable(dktkTotalRest),
        findAttributeValue(container, "urn:dktk:dataelement:20:3"));
  }

}
