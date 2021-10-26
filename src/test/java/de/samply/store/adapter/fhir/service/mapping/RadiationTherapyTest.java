package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.EvaluationContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Optional;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class RadiationTherapyTest {

  private final FhirContext fhirContext = FhirContext.forR4();

  private RadiationTherapyMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new RadiationTherapyMapping(new FhirPathR4(fhirContext, new EvaluationContext()));
  }

  @Test
  void map_withoutDate() {
    var therapy = new Procedure();

    var container = mapping.map(therapy);

    assertEquals("RadiationTherapy", container.getDesignation());
    assertEquals(Optional.of("true"),
        findAttributeValue(container, "urn:dktk:dataelement:34:2"));
    assertEquals(Optional.empty(),
        findAttributeValue(container, "urn:dktk:dataelement:77:1"));
    assertEquals(Optional.empty(),
        findAttributeValue(container, "urn:dktk:dataelement:78:1"));
  }

  @Test
  void map_withStartDate() {
    var therapy = new Procedure();
    therapy.getPerformedPeriod().setStartElement(new DateTimeType("2017-03-15"));

    var container = mapping.map(therapy);

    assertEquals(Optional.of("15.03.2017"),
        findAttributeValue(container, "urn:dktk:dataelement:77:1"));
    assertEquals(Optional.empty(),
        findAttributeValue(container, "urn:dktk:dataelement:78:1"));
  }

  @Disabled
  @Test
  void map_withBothDates() {
    var therapy = new Procedure();
    therapy.getPerformedPeriod()
        .setStartElement(new DateTimeType("2017-03-15"))
        .setEndElement(new DateTimeType("2017-03-16"));

    var container = mapping.map(therapy);

    assertEquals(Optional.of("K"),
        findAttributeValue(container, "urn:dktk:dataelement:67:2"));
    assertEquals(Optional.of("A"),
        findAttributeValue(container, "urn:dktk:dataelement:68:3"));
    assertEquals(Optional.of("16.03.2017"),
        findAttributeValue(container, "urn:dktk:dataelement:25:4"));
    assertEquals(Optional.of("15.03.2017"),
        findAttributeValue(container, "urn:dktk:dataelement:77:1"));
    assertEquals(Optional.of("16.03.2017"),
        findAttributeValue(container, "urn:dktk:dataelement:78:1"));
  }
}
