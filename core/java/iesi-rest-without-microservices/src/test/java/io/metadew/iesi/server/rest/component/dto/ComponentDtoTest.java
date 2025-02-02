package io.metadew.iesi.server.rest.component.dto;

import io.metadew.iesi.metadata.definition.component.Component;
import io.metadew.iesi.metadata.definition.component.ComponentAttribute;
import io.metadew.iesi.metadata.definition.component.ComponentParameter;
import io.metadew.iesi.metadata.definition.component.ComponentVersion;
import io.metadew.iesi.metadata.definition.component.key.ComponentAttributeKey;
import io.metadew.iesi.metadata.definition.component.key.ComponentKey;
import io.metadew.iesi.metadata.definition.component.key.ComponentParameterKey;
import io.metadew.iesi.metadata.definition.component.key.ComponentVersionKey;
import io.metadew.iesi.metadata.definition.environment.key.EnvironmentKey;
import io.metadew.iesi.metadata.service.security.SecurityGroupService;
import io.metadew.iesi.metadata.tools.IdentifierTools;
import io.metadew.iesi.server.rest.Application;
import io.metadew.iesi.server.rest.security_group.SecurityGroupController;
import io.metadew.iesi.server.rest.user.UserController;
import io.metadew.iesi.server.rest.user.team.TeamsController;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(classes = {Application.class, TestConfiguration.class},
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles({"http", "test"})
@DirtiesContext
class ComponentDtoTest {

    @MockBean
    private SecurityGroupController securityGroupController;

    @MockBean
    private SecurityGroupService securityGroupService;

    @MockBean
    private TeamsController teamsController;

    @MockBean
    private UserController userController;

    @Test
    void convertToEntityTest() {
        Component component = new Component(new ComponentKey(IdentifierTools.getComponentIdentifier("name"), 1L),
                "type",
                "name",
                "description",
                new ComponentVersion(new ComponentVersionKey(IdentifierTools.getComponentIdentifier("name"), 1L), "descriptions"),
                Stream.of(new ComponentParameter(new ComponentParameterKey(new ComponentKey(IdentifierTools.getComponentIdentifier("name"), 1L), "name1"), "value1"))
                        .collect(Collectors.toList()),
                Stream.of(new ComponentAttribute(new ComponentAttributeKey(new ComponentKey(IdentifierTools.getComponentIdentifier("name"), 1L), new EnvironmentKey("tst"), "name1"), "value1"),
                        new ComponentAttribute(new ComponentAttributeKey(new ComponentKey(IdentifierTools.getComponentIdentifier("name"), 1L), new EnvironmentKey("tst"), "name2"), "value2"))
                        .collect(Collectors.toList()));


        ComponentDto componentDto = new ComponentDto("type", "name", "description",
                new ComponentVersionDto(1L, "descriptions"),
                Stream.of(new ComponentParameterDto("name1", "value1"))
                        .collect(Collectors.toSet()),
                Stream.of(new ComponentAttributeDto("tst", "name1", "value1"),
                        new ComponentAttributeDto("tst", "name2", "value2"))
                        .collect(Collectors.toSet()));
        assertThat(componentDto.convertToEntity()).isEqualTo(component);
    }
}