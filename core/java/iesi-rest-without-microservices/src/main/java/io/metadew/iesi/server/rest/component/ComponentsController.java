package io.metadew.iesi.server.rest.component;

import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.metadata.definition.component.key.ComponentKey;
import io.metadew.iesi.metadata.tools.IdentifierTools;
import io.metadew.iesi.server.rest.component.dto.ComponentDto;
import io.metadew.iesi.server.rest.component.dto.ComponentDtoResourceAssembler;
import io.metadew.iesi.server.rest.component.dto.IComponentDtoService;
import io.metadew.iesi.server.rest.error.DataBadRequestException;
import io.metadew.iesi.server.rest.resource.HalMultipleEmbeddedResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Tag(name = "components", description = "Everything about components")
@RequestMapping("/components")
public class ComponentsController {

    private final IComponentService componentService;
    private final IComponentDtoService componentDtoService;
    private final ComponentDtoResourceAssembler componentDtoResourceAssembler;
    private final PagedResourcesAssembler<ComponentDto> componentDtoPagedResourcesAssembler;

    @Autowired
    ComponentsController(ComponentDtoResourceAssembler componentDtoResourceAssembler,
                         IComponentService componentService,
                         IComponentDtoService componentDtoService,
                         PagedResourcesAssembler<ComponentDto> componentDtoPagedResourcesAssembler) {
        this.componentDtoResourceAssembler = componentDtoResourceAssembler;
        this.componentService = componentService;
        this.componentDtoService = componentDtoService;
        this.componentDtoPagedResourcesAssembler = componentDtoPagedResourcesAssembler;
    }

    @GetMapping("")
    @PreAuthorize("hasPrivilege('COMPONENTS_READ')")
    public PagedModel<ComponentDto> getAll(Pageable pageable, @RequestParam(required = false, name = "name") String name) {
        List<ComponentFilter> componentFilters = extractComponentFilterOptions(name);
        Page<ComponentDto> componentDtoPage = componentDtoService.getAll(pageable, componentFilters);

        if (componentDtoPage.hasContent())
            return componentDtoPagedResourcesAssembler.toModel(componentDtoPage, componentDtoResourceAssembler::toModel);
        //noinspection unchecked
        return (PagedModel<ComponentDto>) componentDtoPagedResourcesAssembler.toEmptyModel(componentDtoPage, ComponentDto.class);
    }

    private List<ComponentFilter> extractComponentFilterOptions(String name) {
        List<ComponentFilter> componentFilters = new ArrayList<>();
        if (name != null) {
            componentFilters.add(new ComponentFilter(ComponentFilterOption.NAME, name, false));
        }
        return componentFilters;
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasPrivilege('COMPONENTS_READ')")
    public PagedModel<ComponentDto> getByName(Pageable pageable, @PathVariable String name) {
        Page<ComponentDto> componentDtoPage = componentDtoService.getByName(pageable, name);
        if (componentDtoPage.hasContent())
            return componentDtoPagedResourcesAssembler.toModel(componentDtoPage, componentDtoResourceAssembler::toModel);
        //noinspection unchecked
        return (PagedModel<ComponentDto>) componentDtoPagedResourcesAssembler.toEmptyModel(componentDtoPage, ComponentDto.class);
    }

    @GetMapping("/{name}/{version}")
    @PreAuthorize("hasPrivilege('COMPONENTS_READ')")
    public ComponentDto get(@PathVariable String name, @PathVariable Long version) throws MetadataDoesNotExistException {
        ComponentDto component = componentDtoService.getByNameAndVersion(name, version)
                .orElseThrow(() -> new MetadataDoesNotExistException(new ComponentKey(IdentifierTools.getComponentIdentifier(name), version)));
        return componentDtoResourceAssembler.toModel(component);
    }

    @PostMapping("")
    @PreAuthorize("hasPrivilege('COMPONENTS_WRITE')")
    public ComponentDto post(@Valid @RequestBody ComponentDto component) {
        try {
            componentService.createComponent(component);
            return componentDtoResourceAssembler.toModel(component.convertToEntity());
        } catch (MetadataAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Component " + component.getName() + " already exists");
        }
    }

    @PutMapping("")
    @PreAuthorize("hasPrivilege('COMPONENTS_WRITE')")
    public HalMultipleEmbeddedResource<ComponentDto> putAll(@Valid @RequestBody List<ComponentDto> componentDtos) throws MetadataDoesNotExistException {
        componentService.updateComponents(componentDtos);
        HalMultipleEmbeddedResource<ComponentDto> halMultipleEmbeddedResource = new HalMultipleEmbeddedResource<>();
        for (ComponentDto componentDto : componentDtos) {
            halMultipleEmbeddedResource.embedResource(componentDto);
            halMultipleEmbeddedResource.add(linkTo(methodOn(ComponentsController.class)
                    .get(componentDto.getName(), componentDto.getVersion().getNumber()))
                    .withRel(componentDto.getName() + ":" + componentDto.getVersion().getNumber()));
        }

        return halMultipleEmbeddedResource;
    }

    @PutMapping("/{name}/{version}")
    @PreAuthorize("hasPrivilege('COMPONENTS_WRITE')")
    public ComponentDto put(@PathVariable String name, @PathVariable Long version, @RequestBody ComponentDto component) throws MetadataDoesNotExistException {
        if (!component.getName().equals(name)) {
            throw new DataBadRequestException(name);
        } else if (component.getVersion().getNumber() != version) {
            throw new DataBadRequestException(version);
        }
        componentService.updateComponent(component);
        return componentDtoResourceAssembler.toModel(component.convertToEntity());

    }

    @DeleteMapping("")
    @PreAuthorize("hasPrivilege('COMPONENTS_WRITE')")
    public ResponseEntity<?> deleteAll() {
        componentService.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasPrivilege('COMPONENTS_WRITE')")
    public ResponseEntity<?> deleteByName(@PathVariable String name) {
        componentService.deleteByName(name);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{name}/{version}")
    @PreAuthorize("hasPrivilege('COMPONENTS_WRITE')")
    public ResponseEntity<?> delete(@PathVariable String name, @PathVariable Long version) throws MetadataDoesNotExistException {
        componentService.deleteByNameAndVersion(name, version);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}