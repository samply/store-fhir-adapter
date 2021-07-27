package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.MyIEvaluationContext;
import java.util.Optional;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Patrick Skowronek
 */

public class RadiationTherapyTest {

  private RadiationTherapyMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new RadiationTherapyMapping(new FhirPathR4(FhirContext.forR4(), new MyIEvaluationContext()));
  }


  @Test
  void map_performedDate() {
    var therapy = new Procedure();
    var period = new Period();
    period.setStartElement(new DateTimeType("2017-03-15"));
    period.setEndElement(new DateTimeType("2017-03-16"));
    therapy.setPerformed(period);

    var container = mapping.map(therapy);

    assertEquals(Optional.of("15.03.2017"),
        findAttributeValue(container, "urn:dktk:dataelement:77:1"));
    assertEquals(Optional.of("16.03.2017"),
        findAttributeValue(container, "urn:dktk:dataelement:78:1"));
  }

  @Test
  void map_dateNotSet() {
    var therapy = new Procedure();

    var container = mapping.map(therapy);

    assertEquals(Optional.empty(),
        findAttributeValue(container, "urn:dktk:dataelement:77:1"));
    assertEquals(Optional.empty(),
        findAttributeValue(container, "urn:dktk:dataelement:78:1"));
  }

  @Test
  void map_startDate() {
    var therapy = new Procedure();
    var period = new Period();
    period.setStartElement(new DateTimeType("2017-03-15"));
    therapy.setPerformed(period);

    var container = mapping.map(therapy);

    assertEquals(Optional.of("15.03.2017"),
        findAttributeValue(container, "urn:dktk:dataelement:77:1"));
    assertEquals(Optional.empty(),
        findAttributeValue(container, "urn:dktk:dataelement:78:1"));
  }


}
