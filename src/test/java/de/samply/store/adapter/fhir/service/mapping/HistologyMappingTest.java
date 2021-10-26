package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class HistologyMappingTest {

  private final FhirContext fhirContext = FhirContext.forR4();

  @ParameterizedTest
  @CsvFileSource(resources = "/histologyMappings.csv", numLinesToSkip = 1)
  void map_HistologyObservationCSVFile(String fhirMorphologie, String fhirICD_O, String fhirGrading,
      String dktkMorphologie, String dktkICD_O, String dktkGrading) {

    Map<String, Resource> gradings =
        fhirGrading == null ? Map.of() : Map.of("Observation/Test123", createGrading(fhirGrading));
    var mapping = new HistologyMapping(new FhirPathR4(fhirContext, new EvaluationContext(
        gradings)));
    var histology = new Observation();
    histology.getValueCodeableConcept().getCodingFirstRep()
        .setSystem("urn:oid:2.16.840.1.113883.6.43.1").setCode(fhirMorphologie)
        .setVersion(fhirICD_O);
    if (fhirGrading != null) {
      histology.setHasMember(List.of(new Reference("Observation/Test123")));
    }

    var container = mapping.map(histology);

    assertEquals(Optional.ofNullable(dktkMorphologie),
        findAttributeValue(container, "urn:dktk:dataelement:7:2"));
    assertEquals(Optional.ofNullable(dktkICD_O),
        findAttributeValue(container, "urn:dktk:dataelement:8:2"));
    assertEquals(Optional.ofNullable(dktkGrading),
        findAttributeValue(container, "urn:dktk:dataelement:9:2"));


  }

  private Observation createGrading(String fhirGrading) {
    var grading = new Observation();
    grading.setId(new IdType("Test123"));
    grading.getValueCodeableConcept().getCodingFirstRep()
        .setSystem("http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GradingCS").setCode(fhirGrading);
    return grading;
  }
}
