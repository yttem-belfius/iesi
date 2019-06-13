package io.metadew.iesi.server.rest.ressource.environment;

import io.metadew.iesi.metadata.definition.Environment;
import io.metadew.iesi.metadata.definition.EnvironmentParameter;
import io.metadew.iesi.server.rest.controller.EnvironmentsController;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class EnvironmentDtoResourceAssembler extends ResourceAssemblerSupport<Environment, EnvironmentDto> {

    private final ModelMapper modelMapper;

    public EnvironmentDtoResourceAssembler() {
        super(EnvironmentsController.class, EnvironmentDto.class);
        this.modelMapper = new ModelMapper();
    }


    @Override
    public EnvironmentDto toResource(Environment environment) {
        EnvironmentDto environmentDto = convertToDto(environment);
        Link selfLink = linkTo(methodOn(EnvironmentsController.class).getByName(environmentDto.getName()))
                .withSelfRel();
        environmentDto.add(selfLink);
        Link connectionsLink = linkTo(methodOn(EnvironmentsController.class).getEnvironmentsConnections(environmentDto.getName()))
                .withRel("connections");
        environmentDto.add(connectionsLink);
        return environmentDto;
    }

    private EnvironmentDto convertToDto(Environment environment) {
        if (environment == null) {
            throw new IllegalArgumentException("Environments have to be non empty");
        }

        return modelMapper.map(environment, EnvironmentDto.class);
    }
}