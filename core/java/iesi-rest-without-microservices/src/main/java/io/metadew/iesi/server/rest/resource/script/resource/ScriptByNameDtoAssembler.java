package io.metadew.iesi.server.rest.resource.script.resource;

import io.metadew.iesi.metadata.definition.script.Script;
import io.metadew.iesi.server.rest.controller.ScriptController;
import io.metadew.iesi.server.rest.resource.script.dto.ScriptByNameDto;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ScriptByNameDtoAssembler extends ResourceAssemblerSupport<List<Script>, ScriptByNameDto> {

    private final ModelMapper modelMapper;

    public ScriptByNameDtoAssembler() {
        super(ScriptController.class, ScriptByNameDto.class);
        this.modelMapper = new ModelMapper();
    }

    @Override
    public ScriptByNameDto toResource(List<Script> scripts) {
        ScriptByNameDto scriptDto = convertToDto(scripts);
        scriptDto.add(linkTo(methodOn(ScriptController.class)
                .getByNameScript(scriptDto.getName()))
                .withSelfRel());
        Link versionLink = linkTo(methodOn(ScriptController.class).getByNameScript(String.valueOf(scripts.get(0).getVersion().getNumber())))
                    .withRel("version");
        scriptDto.add(versionLink);
        return scriptDto;
    }

    private ScriptByNameDto convertToDto(List<Script> scripts) {
        return new ScriptByNameDto(scripts.get(0).getName(), scripts.get(0).getType(), scripts.get(0).getDescription(), scripts.stream().map(script -> script.getVersion().getNumber()).collect(Collectors.toList()));
    }
}