package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Attribute;
import de.samply.share.model.ccp.ObjectFactory;

/**
 * Mapping Utilities.
 *
 * @author Alexander Kiel
 */
public class Util {

  private static final ObjectFactory objectFactory = new ObjectFactory();

  /**
   * Creates an attribute.
   *
   * @param mdrKey the MDR URN
   * @param value the value
   * @return the attribute
   */
  public static Attribute createAttribute(String mdrKey, String value) {
    var attribute = objectFactory.createAttribute();
    attribute.setMdrKey(mdrKey);
    attribute.setValue(objectFactory.createValue(value));
    return attribute;
  }
}
