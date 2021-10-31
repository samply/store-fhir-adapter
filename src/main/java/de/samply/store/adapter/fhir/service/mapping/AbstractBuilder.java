package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.share.model.ccp.Entity;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.Resource;

/**
 * Base class of a builder of CCP entities.
 *
 * @param <T> the type of the entity being build
 */
public class AbstractBuilder<T extends Entity> {

  protected final FhirPathR4 fhirPathR4;
  protected final T entity;
  protected final Resource resource;

  /**
   * Creates a new builder.
   *
   * @param fhirPathR4 the FHIRPath engine
   * @param entity     the initial entity being build
   * @param resource   the FHIR resource used to query by FHIRPath
   */
  public AbstractBuilder(FhirPathR4 fhirPathR4, T entity, Resource resource) {
    this.fhirPathR4 = Objects.requireNonNull(fhirPathR4);
    this.entity = Objects.requireNonNull(entity);
    this.resource = Objects.requireNonNull(resource);
  }

  public <S extends IBase> void addAttribute(String path, Class<S> type, String mdrKey,
      Function<? super S, String> toString) {
    addAttribute(resource, path, type, mdrKey, toString);
  }

  public <S extends IBase> void addAttribute(Resource resource, String path, Class<S> type,
      String mdrKey, Function<? super S, String> toString) {
    addAttributeOptional(resource, path, type, mdrKey, v -> Optional.ofNullable(toString.apply(v)));
  }

  public void addAttribute(String mdrKey, String value) {
    entity.getAttribute().add(Util.createAttribute(mdrKey, value));
  }

  public <S extends IBase> void addAttributeOptional(String path, Class<S> type, String mdrKey,
      Function<? super S, Optional<String>> toString) {
    addAttributeOptional(resource, path, type, mdrKey, toString);
  }

  /**
   * Adds an attribute to the entity managed by this builder if the {@code path} and {@code
   * toString} function returns a value.
   *
   * @param resource the resource to use as base for the FHIRPath evaluation
   * @param path     the FHIRPath
   * @param type     the class of the type of the FHIRPath return value
   * @param mdrKey   the MDR key to use for the attribute
   * @param toString a function from the FHIRPath return value to a string that will be the value of
   *                 the attribute
   * @param <S>      the type of the FHIRPath return value
   */
  public <S extends IBase> void addAttributeOptional(Resource resource, String path, Class<S> type,
      String mdrKey, Function<? super S, Optional<String>> toString) {
    fhirPathR4.evaluateFirst(resource, path, type)
        .flatMap(toString)
        .map(v -> Util.createAttribute(mdrKey, v))
        .ifPresent(a -> entity.getAttribute().add(a));
  }

  public <S extends IBase> void addAttribute2(String pathA, String pathB, Class<S> type,
      String mdrKey, BiFunction<? super S, Optional<? extends S>, String> toString) {
    addAttribute2(resource, pathA, pathB, type, mdrKey, toString);
  }

  public <S extends IBase> void addAttribute2(Resource resource, String pathA, String pathB,
      Class<S> type, String mdrKey,
      BiFunction<? super S, Optional<? extends S>, String> toString) {
    addAttributeOptional2(resource, pathA, pathB, type, mdrKey,
        (a, b) -> Optional.ofNullable(toString.apply(a, b)));
  }

  /**
   * Adds an attribute to the entity managed by this builder if the {@code pathA} and {@code
   * toString} function returns a value with the help of the value from {@code pathB}.
   *
   * @param resource the resource to use as base for the FHIRPath evaluation
   * @param pathA    the primary FHIRPath
   * @param pathB    the secondary FHIRPath
   * @param type     the class of the type of the FHIRPath return value
   * @param mdrKey   the MDR key to use for the attribute
   * @param toString a function from the FHIRPath A return value and optional FHIRPath B return
   *                 value to a string that will be the value of the attribute
   * @param <S>      the type of the FHIRPath return value
   */
  public <S extends IBase> void addAttributeOptional2(Resource resource, String pathA, String pathB,
      Class<S> type, String mdrKey,
      BiFunction<? super S, Optional<? extends S>, Optional<String>> toString) {
    fhirPathR4.evaluateFirst(resource, pathA, type)
        .flatMap(a -> toString.apply(a, fhirPathR4.evaluateFirst(resource, pathB, type)))
        .map(v -> Util.createAttribute(mdrKey, v))
        .ifPresent(a -> entity.getAttribute().add(a));
  }

  public <S extends IBase> void addContainer(String path, Class<S> type,
      Function<? super S, Container> toContainer) {
    addContainers(fhirPathR4.evaluate(resource, path, type).stream().map(toContainer).toList());
  }

  public void addContainer(Container container) {
    entity.getContainer().add(container);
  }

  public void addContainers(Collection<Container> containers) {
    entity.getContainer().addAll(containers);
  }

  public T build() {
    return entity;
  }
}
