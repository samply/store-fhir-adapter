package de.samply.store.adapter.fhir.service.mapping;

import de.samply.share.model.ccp.Container;
import de.samply.share.model.ccp.ObjectFactory;
import java.util.Optional;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Resource;

import java.util.function.Function;

class ContainerBuilder {

    private final FhirPathR4 fhirPathR4;
    private final Container container = new ObjectFactory().createContainer();
    private final Resource resource;

    ContainerBuilder(FhirPathR4 fhirPathR4, Resource resource, String designation) {
        this.fhirPathR4 = fhirPathR4;
        this.resource = resource;
        container.setDesignation(designation);
        container.setId(resource.getResourceType() + resource.getIdElement().getIdPart());
    }

    <T extends IBase> void addAttribute(String path, Class<T> type, String mdrKey,
                                         Function<T, String> toString) {
        addAttributeOptional(path,type,mdrKey, v -> Optional.of(toString.apply(v)));
    }

    <T extends IBase> void addAttributeOptional(String path, Class<T> type, String mdrKey,
                                        Function<T, Optional<String>> toString) {
        fhirPathR4.evaluateFirst(resource, path, type)
                .flatMap(toString)
                .map(v -> Util.createAttribute(mdrKey, v))
                .ifPresent(a -> container.getAttribute().add(a));
    }

    Container build() {
        return container;
    }
}
