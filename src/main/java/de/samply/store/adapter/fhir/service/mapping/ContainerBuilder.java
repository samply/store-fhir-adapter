package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.share.model.ccp.ObjectFactory;
import de.samply.store.adapter.fhir.service.FhirPathR4;
import java.util.Objects;
import org.hl7.fhir.r4.model.Resource;

class ContainerBuilder extends AbstractBuilder<Container> {

  ContainerBuilder(FhirPathR4 fhirPathR4, Resource resource, String designation) {
    super(Objects.requireNonNull(fhirPathR4), new ObjectFactory().createContainer(),
        Objects.requireNonNull(resource));
    entity.setId(resource.getResourceType() + "-" + resource.getIdElement().getIdPart());
    entity.setDesignation(designation);
  }
}
