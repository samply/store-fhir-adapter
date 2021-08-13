package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.share.model.ccp.Entity;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.Resource;

public class AbstractBuilder<T extends Entity> {

  protected final FhirPathR4 fhirPathR4;
  protected final T entity;
  protected final Resource resource;

  public AbstractBuilder(FhirPathR4 fhirPathR4, T entity, Resource resource) {
    this.fhirPathR4 = fhirPathR4;
    this.entity = entity;
    this.resource = resource;
  }

  public <S extends IBase> void addAttribute(String path, Class<S> type, String mdrKey,
      Function<? super S, String> toString) {
    addAttribute(resource, path, type, mdrKey, toString);
  }

  public <S extends IBase> void addAttribute(Resource resource, String path, Class<S> type,
      String mdrKey,
      Function<? super S, String> toString) {
    addAttributeOptional(resource, path, type, mdrKey, v -> Optional.of(toString.apply(v)));
  }

  public <S extends IBase> void addAttributeOptional(String path, Class<S> type, String mdrKey,
      Function<? super S, Optional<String>> toString) {
    addAttributeOptional(resource, path, type, mdrKey, toString);
  }

  public <S extends IBase> void addAttributeOptional(Resource resource, String path, Class<S> type,
      String mdrKey,
      Function<? super S, Optional<String>> toString) {
    fhirPathR4.evaluateFirst(resource, path, type)
        .flatMap(toString)
        .map(v -> Util.createAttribute(mdrKey, v))
        .ifPresent(a -> entity.getAttribute().add(a));
  }

  public <S extends IBase> void addContainer(String path, Class<S> type,
      Function<? super S, Container> toContainer) {
    addContainers(fhirPathR4.evaluate(resource, path, type)
        .stream().map(toContainer)
        .collect(Collectors.toList()));
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
