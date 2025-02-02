package io.metadew.iesi.server.rest.component.dto;

import io.metadew.iesi.common.configuration.metadata.repository.MetadataRepositoryConfiguration;
import io.metadew.iesi.metadata.configuration.component.ComponentConfiguration;
import io.metadew.iesi.metadata.definition.component.Component;
import io.metadew.iesi.metadata.definition.component.ComponentParameter;
import io.metadew.iesi.metadata.definition.component.ComponentVersion;
import io.metadew.iesi.metadata.definition.component.key.ComponentKey;
import io.metadew.iesi.metadata.definition.component.key.ComponentParameterKey;
import io.metadew.iesi.metadata.definition.component.key.ComponentVersionKey;
import io.metadew.iesi.server.rest.Application;
import io.metadew.iesi.server.rest.component.ComponentFilter;
import io.metadew.iesi.server.rest.component.ComponentFilterOption;
import io.metadew.iesi.server.rest.configuration.TestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles("test")
@DirtiesContext
class ComponentRepositoryDtoTest {

    @Autowired
    private ComponentDtoRepository componentDtoRepository;
    @Autowired
    private ComponentConfiguration componentConfiguration;
    @Autowired
    private MetadataRepositoryConfiguration metadataRepositoryConfiguration;
    @Autowired
    private ComponentDtoResourceAssembler componentDtoResourceAssembler;

    @AfterEach
    void cleanup() {
        metadataRepositoryConfiguration.clearAllTables();
    }

    @Test
    void getAllNoResultsTest() {
        assertThat(componentDtoRepository.getAll(Pageable.unpaged(), new ArrayList<>()))
                .isEmpty();
    }

    @Test
    void getAllTest() {
        Component component1 = createComponent("component1", "1");
        Component component2 = createComponent("component2", "2");
        componentConfiguration.insert(component1);
        componentConfiguration.insert(component2);
        ComponentDto component1Dto = componentDtoResourceAssembler.toModel(component1);
        ComponentDto component2Dto = componentDtoResourceAssembler.toModel(component2);
        assertThat(componentDtoRepository.getAll(Pageable.unpaged(), new ArrayList<>())).containsOnly(
                component1Dto,
                component2Dto
        );
    }

    @Test
    void getAllPaginatedAllInclusiveTest() {
        Component component1 = createComponent("component1", "1");
        Component component2 = createComponent("component2", "2");
        Component component3 = createComponent("component3", "3");
        componentConfiguration.insert(component1);
        componentConfiguration.insert(component2);
        componentConfiguration.insert(component3);
        ComponentDto component1Dto = componentDtoResourceAssembler.toModel(component1);
        ComponentDto component2Dto = componentDtoResourceAssembler.toModel(component2);
        ComponentDto component3Dto = componentDtoResourceAssembler.toModel(component3);

        Pageable page1 = PageRequest.of(0, 2);
        Pageable page2 = PageRequest.of(1, 2);
        assertThat(componentDtoRepository.getAll(page1, new ArrayList<>())).containsOnly(
                component1Dto,
                component2Dto
        );
        assertThat(componentDtoRepository.getAll(page2, new ArrayList<>())).containsOnly(
                component3Dto
        );
    }

    @Test
    void getAllPaginatedSortedAscTest() {
        Component component1 = createComponent("component1", "1");
        Component component2 = createComponent("component2", "2");
        Component component3 = createComponent("component3", "3");
        Component component4 = createComponent("component4", "4");
        Component component5 = createComponent("component5", "5");
        componentConfiguration.insert(component1);
        componentConfiguration.insert(component2);
        componentConfiguration.insert(component3);
        componentConfiguration.insert(component4);
        componentConfiguration.insert(component5);
        ComponentDto component1Dto = componentDtoResourceAssembler.toModel(component1);
        ComponentDto component2Dto = componentDtoResourceAssembler.toModel(component2);
        ComponentDto component3Dto = componentDtoResourceAssembler.toModel(component3);
        ComponentDto component4Dto = componentDtoResourceAssembler.toModel(component4);
        ComponentDto component5Dto = componentDtoResourceAssembler.toModel(component5);

        PageRequest pageable1 = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "name"));
        PageRequest pageable2 = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "name"));
        PageRequest pageable3 = PageRequest.of(2, 2, Sort.by(Sort.Direction.ASC, "name"));

        Page<ComponentDto> page1 = componentDtoRepository.getAll(pageable1, new ArrayList<>());
        Page<ComponentDto> page2 = componentDtoRepository.getAll(pageable2, new ArrayList<>());
        Page<ComponentDto> page3 = componentDtoRepository.getAll(pageable3, new ArrayList<>());
        assertThat(page1).containsExactly(
                component1Dto,
                component2Dto
        );
        assertThat(page2).containsExactly(
                component3Dto,
                component4Dto
        );
        assertThat(page3).containsExactly(
                component5Dto
        );
        assertThat(page1.getNumberOfElements()).isEqualTo(2);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getTotalPages()).isEqualTo(3);

    }

    @Test
    void getAllPaginatedSortedDescTest() {
        Component component1 = createComponent("component1", "1");
        Component component2 = createComponent("component2", "2");
        Component component3 = createComponent("component3", "3");
        Component component4 = createComponent("component4", "4");
        Component component5 = createComponent("component5", "5");
        componentConfiguration.insert(component1);
        componentConfiguration.insert(component2);
        componentConfiguration.insert(component3);
        componentConfiguration.insert(component4);
        componentConfiguration.insert(component5);
        ComponentDto component1Dto = componentDtoResourceAssembler.toModel(component1);
        ComponentDto component2Dto = componentDtoResourceAssembler.toModel(component2);
        ComponentDto component3Dto = componentDtoResourceAssembler.toModel(component3);
        ComponentDto component4Dto = componentDtoResourceAssembler.toModel(component4);
        ComponentDto component5Dto = componentDtoResourceAssembler.toModel(component5);

        PageRequest pageable1 = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "name"));
        PageRequest pageable2 = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "name"));
        PageRequest pageable3 = PageRequest.of(2, 2, Sort.by(Sort.Direction.DESC, "name"));

        Page<ComponentDto> page1 = componentDtoRepository.getAll(pageable1, new ArrayList<>());
        Page<ComponentDto> page2 = componentDtoRepository.getAll(pageable2, new ArrayList<>());
        Page<ComponentDto> page3 = componentDtoRepository.getAll(pageable3, new ArrayList<>());
        assertThat(page1).containsExactly(
                component5Dto,
                component4Dto
        );
        assertThat(page2).containsExactly(
                component3Dto,
                component2Dto
        );
        assertThat(page3).containsExactly(
                component1Dto
        );
        assertThat(page1.getNumberOfElements()).isEqualTo(2);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getTotalPages()).isEqualTo(3);
    }

    @Test
    void getAllFilteredOnNameTest() {
        Component component1 = createComponent("component1", "1");
        Component component2 = createComponent("component2", "2");
        componentConfiguration.insert(component1);
        componentConfiguration.insert(component2);
        ComponentDto component1Dto = componentDtoResourceAssembler.toModel(component1);
        ComponentDto component2Dto = componentDtoResourceAssembler.toModel(component2);

        assertThat(componentDtoRepository.getAll(Pageable.unpaged(),
                Stream.of(
                        new ComponentFilter(ComponentFilterOption.NAME, "nent", false)
                ).collect(Collectors.toList())
        )).containsOnly(component2Dto, component1Dto);
        assertThat(componentDtoRepository.getAll(Pageable.unpaged(),
                Stream.of(
                        new ComponentFilter(ComponentFilterOption.NAME, "nent1", false)
                ).collect(Collectors.toList())
        )).containsExactly(component1Dto);
        assertThat(componentDtoRepository.getAll(Pageable.unpaged(),
                Stream.of(
                        new ComponentFilter(ComponentFilterOption.NAME, "nent2", false)
                ).collect(Collectors.toList())
        )).containsExactly(component2Dto);
    }

    @Test
    void getAllByVersionTest() {
        Component component1 = createComponent("component1", "1");
        Component component2 = createComponent("component2", "2");
        componentConfiguration.insert(component1);
        componentConfiguration.insert(component2);
        ComponentDto component1Dto = componentDtoResourceAssembler.toModel(component1);
        ComponentDto component2Dto = componentDtoResourceAssembler.toModel(component2);

        assertThat(componentDtoRepository.getAll(Pageable.unpaged(),
                Stream.of(
                        new ComponentFilter(ComponentFilterOption.VERSION, "0", false)
                ).collect(Collectors.toList())
        )).isEmpty();
        assertThat(componentDtoRepository.getAll(Pageable.unpaged(),
                Stream.of(
                        new ComponentFilter(ComponentFilterOption.VERSION, "1", false)
                ).collect(Collectors.toList())
        )).containsOnly(component1Dto, component2Dto);
    }

    @Test
    void getByNameTest() {
        Component component1 = createComponent("component1", "1");
        Component component2 = createComponent("component2", "2");
        componentConfiguration.insert(component1);
        componentConfiguration.insert(component2);
        ComponentDto component1Dto = componentDtoResourceAssembler.toModel(component1);
        ComponentDto component2Dto = componentDtoResourceAssembler.toModel(component2);

        assertThat(componentDtoRepository.getByName(Pageable.unpaged(), "component1"))
                .containsOnly(component1Dto);
        assertThat(componentDtoRepository.getByName(Pageable.unpaged(), "component2"))
                .containsOnly(component2Dto);
    }

    @Test
    void getByNameNoResultsTest() {
        Component component1 = createComponent("component1", "1");
        Component component2 = createComponent("component2", "2");
        componentConfiguration.insert(component1);
        componentConfiguration.insert(component2);

        assertThat(componentDtoRepository.getByName(Pageable.unpaged(), "component3"))
                .isEmpty();
    }

    @Test
    void getByNameAndVersionNoResultsTest() {
        Component component1 = createComponent("component1", "1");
        Component component2 = createComponent("component2", "2");
        componentConfiguration.insert(component1);
        componentConfiguration.insert(component2);

        assertThat(componentDtoRepository.getByNameAndVersion("component3", 1L))
                .isEmpty();
        assertThat(componentDtoRepository.getByNameAndVersion("component1", 2L))
                .isEmpty();
    }


    Component createComponent(String name, String id) {
        ComponentKey componentKey = new ComponentKey(id, 1L);
        return Component.builder()
                .componentKey(componentKey)
                .type("http.request")
                .name(name)
                .description("component description")
                .version(componentVersion(componentKey))
                .parameters(
                        Stream.of(
                                componentParameter(componentKey, "connection", "connection"),
                                componentParameter(componentKey, "endpoint", "/users"),
                                componentParameter(componentKey, "type", "GET")
                        ).collect(Collectors.toList())
                )
                .attributes(new ArrayList<>())
                .build();
    }


    ComponentVersion componentVersion(ComponentKey componentKey) {
        return new ComponentVersion(
                new ComponentVersionKey(componentKey),
                "Component description"
        );
    }

    ComponentParameter componentParameter(ComponentKey componentKey, String name, String value) {
        return new ComponentParameter(
                new ComponentParameterKey(
                        componentKey,
                        name
                ),
                value
        );
    }

}
