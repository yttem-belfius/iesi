package io.metadew.iesi.server.rest.resource.component.resource;


import io.metadew.iesi.metadata.definition.Component;
import io.metadew.iesi.server.rest.controller.ComponentsController;
import io.metadew.iesi.server.rest.resource.component.dto.ComponentByNameDto;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@org.springframework.stereotype.Component
public class ComponentGetByNameDtoAssembler extends ResourceAssemblerSupport<List<Component>, ComponentByNameDto> {

    public ComponentGetByNameDtoAssembler() {
        super(ComponentsController.class, ComponentByNameDto.class);
    }

    @Override
    public ComponentByNameDto toResource(List<Component> components) {
        ComponentByNameDto componentGlobalDto = convertToDto(components);
        componentGlobalDto.add(linkTo(methodOn(ComponentsController.class)
                .getByName(componentGlobalDto.getName()))
                .withSelfRel());
        return componentGlobalDto;
    }

    private ComponentByNameDto convertToDto(List<Component> components) {
        // TODO: check if all components is not empty.
        // TODO: check if all Components have the same name and type.
        return new ComponentByNameDto(components.get(0).getName(), components.get(0).getType(), components.get(0).getDescription(),
                components.stream().map(component -> component.getVersion().getNumber()).collect(Collectors.toList()));

    }
}