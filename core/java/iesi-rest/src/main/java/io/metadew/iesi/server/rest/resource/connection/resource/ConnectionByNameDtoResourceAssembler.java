package io.metadew.iesi.server.rest.resource.connection.resource;

import io.metadew.iesi.metadata.definition.Connection;
import io.metadew.iesi.server.rest.controller.ConnectionsController;
import io.metadew.iesi.server.rest.resource.connection.dto.ConnectionByNameDto;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ConnectionByNameDtoResourceAssembler extends ResourceAssemblerSupport<List<Connection>, ConnectionByNameDto> {

    private final ModelMapper modelMapper;

    public ConnectionByNameDtoResourceAssembler() {
        super(ConnectionsController.class, ConnectionByNameDto.class);
        this.modelMapper = new ModelMapper();
    }

    @Override
    public ConnectionByNameDto toResource(List<Connection> connections) {
        ConnectionByNameDto connectionByNameDto = convertToDto(connections);
        for (String environment : connectionByNameDto.getEnvironments()) {
            connectionByNameDto.add(linkTo(methodOn(ConnectionsController.class).getByNameandEnvironment(connectionByNameDto.getName(), environment))
                    .withRel("connection:"+connectionByNameDto.getName()+"-"+environment));
        }
        return connectionByNameDto;
    }

    private ConnectionByNameDto convertToDto(List<Connection> connections) {
        if (connections.isEmpty()) {
            throw new IllegalArgumentException("Connections have to be non empty");
        }
        if (!connections.stream().allMatch(connection -> connection.getName().equals(connections.get(0).getName()))) {
            throw new IllegalArgumentException(MessageFormat.format("Connections ''{0}'' do not define the same name ''{1}''", connections, connections.get(0).getName()));
        }
        if (!connections.stream().allMatch(connection -> connection.getType().equals(connections.get(0).getType()))) {
            throw new IllegalArgumentException(MessageFormat.format("Connections ''{0}'' do not define the same type ''{1}''", connections, connections.get(0).getName()));
        }

        ConnectionByNameDto connectionByNameDto = modelMapper.map(connections.get(0), ConnectionByNameDto.class);
        connectionByNameDto.setEnvironments(connections.stream()
                .map(Connection::getEnvironment)
                .collect(Collectors.toList()));
        return connectionByNameDto;
    }
}