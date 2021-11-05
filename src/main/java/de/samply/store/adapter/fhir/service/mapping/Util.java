package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Attribute;
import de.samply.share.model.ccp.ObjectFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.hl7.fhir.r4.model.PrimitiveType;

/**
 * Mapping Utilities.
 */
public class Util {

  private static final ObjectFactory objectFactory = new ObjectFactory();

  private Util() {
  }

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

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  /**
   * Parses a given string as local date. Returns {@link Optional#empty() empty} on parsing errors.
   */
  public static final Function<String, Optional<LocalDate>> PARSE_LOCAL_DATE = s -> {
    try {
      return Optional.of(LocalDate.parse(s));
    } catch (DateTimeParseException e) {
      return Optional.empty();
    }
  };

  public static final Function<PrimitiveType<?>, Optional<LocalDate>> LOCAL_DATE = dateTime ->
      dateTime.getValueAsString() == null || dateTime.getValueAsString().length() < 10
          ? Optional.empty()
          : PARSE_LOCAL_DATE.apply(dateTime.getValueAsString().substring(0, 10));

  /**
   * Lifts {@code f} into the Optional monad.
   */
  private static <A, B> Function<Optional<A>, Optional<B>> lift(Function<A, B> f) {
    return a -> a.map(f);
  }

  /**
   * Lifts {@code f}, a 2-arity function into the Optional monad.
   */
  public static <A, B, C> BiFunction<Optional<A>, Optional<B>, Optional<C>> lift2(
      BiFunction<A, B, C> f) {
    return (optionalA, optionalB) -> optionalA.flatMap(a -> optionalB.map(b -> f.apply(a, b)));
  }

  private static final Function<LocalDate, String> FORMAT_LOCAL_DATE = localDate -> localDate
      .format(DATE_FORMATTER);

  public static final Function<PrimitiveType<?>, Optional<String>> DATE_STRING =
      lift(FORMAT_LOCAL_DATE).compose(LOCAL_DATE);
}
