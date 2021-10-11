package de.samply.store.adapter.fhir.service.mapping;

import static de.samply.store.adapter.fhir.service.TestUtil.findAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import de.samply.store.adapter.fhir.service.MyIEvaluationContext;
import java.util.Optional;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Patrick Skowronek
 */

public class SystemTherapyTest {

  private SystemTherapyMapping mapping;

  @BeforeEach
  void setUp() {
    mapping = new SystemTherapyMapping(new FhirPathR4(
        FhirContext.forR4(), new MyIEvaluationContext()));
  }

  @Test
  void map_performedDate() {
    var therapy = new MedicationStatement();
    var period = new Period();
    period.setStartElement(new DateTimeType("2017-03-15"));
    period.setEndElement(new DateTimeType("2017-03-16"));
    therapy.setEffective(period);

    var progressContainer = mapping.map(therapy);
    assertEquals("Progress", progressContainer.getDesignation());

    assertEquals(Optional.of("true"),
        findAttributeValue(progressContainer, "urn:dktk:dataelement:34:2"));

    var systemContainer = progressContainer.getContainer().get(0);
    assertEquals("SystemTherapy", systemContainer.getDesignation());

    assertEquals(Optional.of("15.03.2017"),
        findAttributeValue(systemContainer, "urn:dktk:dataelement:90:1"));
    assertEquals(Optional.of("16.03.2017"),
        findAttributeValue(systemContainer, "urn:dktk:dataelement:93:1"));
  }

  @Test
  void map_Protocol() {
    var therapy = new MedicationStatement();
    var period = new Period();
    period.setStartElement(new DateTimeType("2017-03-15"));
    period.setEndElement(new DateTimeType("2017-03-16"));
    therapy.setEffective(period);
    therapy.getMedicationCodeableConcept().getCodingFirstRep()
        .setCode("Epirubicin Taxotere Cyclophosphamid");

    var progressContainer = mapping.map(therapy);
    assertEquals("Progress", progressContainer.getDesignation());

    assertEquals(Optional.of("true"),
        findAttributeValue(progressContainer, "urn:dktk:dataelement:34:2"));

    var systemContainer = progressContainer.getContainer().get(0);
    assertEquals("SystemTherapy", systemContainer.getDesignation());

    assertEquals(Optional.of("15.03.2017"),
        findAttributeValue(systemContainer, "urn:dktk:dataelement:90:1"));
    assertEquals(Optional.of("16.03.2017"),
        findAttributeValue(systemContainer, "urn:dktk:dataelement:93:1"));
    assertEquals(Optional.of("Epirubicin Taxotere Cyclophosphamid"),
        findAttributeValue(systemContainer, "urn:dktk:dataelement:91:1"));
  }
}
