package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Attribute;
import de.samply.share.model.ccp.ObjectFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.tomcat.jni.Local;
import org.checkerframework.checker.units.qual.C;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.PrimitiveType;

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
   * @param value  the value
   * @return the attribute
   */
  public static Attribute createAttribute(String mdrKey, String value) {
    var attribute = objectFactory.createAttribute();
    attribute.setMdrKey(mdrKey);
    attribute.setValue(objectFactory.createValue(value));
    return attribute;
  }
  /**
   * Formates a given date string
   *
   * @param string of a date
   * @return Formated Datetime
   */
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  /**
   * Parses a given string, result may be optional if it matches the parse criteria
   *
   * @param date as a string
   * @return a localdate
   * @throws DateTimeParseException if date is not parseable
   */
  public static final Function<String, Optional<LocalDate>> PARSE_LOCAL_DATE = s -> {
    try {
      return Optional.of(LocalDate.parse(s));
    } catch (DateTimeParseException e) {
      return Optional.empty();
    }
  };
  /**
   * Parses a given string, result may be optional if it matches the parse criteria
   *
   * @param a PrimitiveType datetime
   * @return the parsed
   * @throws DateTimeParseException if date is not parseable
   */
  public static final Function<PrimitiveType, Optional<LocalDate>> LOCAL_DATE = dateTime -> dateTime.getValueAsString().length() < 10 ? Optional.empty() : PARSE_LOCAL_DATE.apply(dateTime.getValueAsString().substring(0, 10));
  /**
   * This function lift's a given function which is a -> b
   *
   * That means when the parameter a is optional and result b is then optional too
   * @param a function which is a -> b
   * @return a function which is optional(a) -> optional (b)
   */
  private static <A,B> Function<Optional<A>,Optional<B>> lift(Function<A,B> f){
    return a -> a.map(f);
  }
  /**
   * This function lift's a given function which is a,b -> c
   *
   * That means when one of the parameters is optional and result c is then optional
   * @param a function which is a,b -> c
   * @return a function which is optional(a), optional(b) -> optional(c)
   */
  public static <A,B,C> BiFunction<Optional<A>,Optional<B>, Optional<C>> lift2(BiFunction<A, B, C> f){
    return (oA,oB) -> oA.flatMap(a -> oB.map(b -> f.apply(a,b)));
  }
  private static final Function<LocalDate, String> FORMAT_LOCAL_DATE = localDate -> localDate
      .format(DATE_FORMATTER);
  /**
   * This function transfomres a DateTimeType to a string wit Format
   *
   * @param DateTimeType date
   * @return an optional(string) of the date
   */
  public static final Function<DateTimeType, Optional<String>> DATE_STRING = lift(FORMAT_LOCAL_DATE)
      .compose(LOCAL_DATE);
}

