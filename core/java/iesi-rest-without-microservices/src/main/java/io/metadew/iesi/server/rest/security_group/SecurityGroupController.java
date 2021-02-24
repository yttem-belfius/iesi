package io.metadew.iesi.server.rest.security_group;

import io.metadew.iesi.metadata.definition.security.SecurityGroup;
import io.metadew.iesi.metadata.definition.security.SecurityGroupKey;
import io.metadew.iesi.metadata.definition.user.TeamKey;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/security-groups")
@CrossOrigin
@Log4j2
public class SecurityGroupController {

    public static final String PUBLIC_GROUP_NAME = "PUBLIC";

    private final ISecurityGroupService securityGroupService;

    public SecurityGroupController(ISecurityGroupService securityGroupService) {
        this.securityGroupService = securityGroupService;
    }

    @PostConstruct
    void checkPublicSecurityGroup() {
        if (!securityGroupService.get(PUBLIC_GROUP_NAME).isPresent()) {
            log.warn(String.format("Creating %s security group.", PUBLIC_GROUP_NAME));
            SecurityGroup publicSecurityGroup = SecurityGroup.builder()
                    .metadataKey(new SecurityGroupKey(UUID.randomUUID()))
                    .name(PUBLIC_GROUP_NAME)
                    .teamKeys(new HashSet<>())
                    .securedObjects(new HashSet<>())
                    .build();
            securityGroupService.addSecurityGroup(publicSecurityGroup);
        }
    }

    @PostMapping("")
    @PreAuthorize("hasPrivilege('GROUPS_WRITE')")
    public ResponseEntity<SecurityGroupDto> create(@RequestBody SecurityGroupPostDto securityGroupPostDto) {
        SecurityGroup securityGroup = SecurityGroup.builder()
                .metadataKey(new SecurityGroupKey(securityGroupPostDto.getId()))
                .name(securityGroupPostDto.getName())
                .teamKeys(new HashSet<>())
                .securedObjects(new HashSet<>())
                .build();
        securityGroupService.addSecurityGroup(securityGroup);
        return ResponseEntity.of(securityGroupService.get(securityGroup.getMetadataKey().getUuid()));
    }

    @PostMapping("/{uuid}/teams")
    @PreAuthorize("hasPrivilege('GROUPS_WRITE')")
    public ResponseEntity<SecurityGroupDto> addTeam(@PathVariable UUID uuid, @RequestBody SecurityGroupTeamPutDto securityGroupTeamPutDto) {
        if (securityGroupService.exists(new SecurityGroupKey(uuid))) {
            securityGroupService.addTeam(new SecurityGroupKey(uuid), new TeamKey(securityGroupTeamPutDto.getId()));
            return ResponseEntity.of(securityGroupService.get(uuid));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{security-group-uuid}/teams/{team-uuid}")
    @PreAuthorize("hasPrivilege('GROUPS_WRITE')")
    public ResponseEntity<SecurityGroupDto> deleteTeam(@PathVariable("security-group-uuid") UUID securityGroupUuid, @PathVariable("team-uuid") UUID teamUuid) {
        if (securityGroupService.exists(new SecurityGroupKey(securityGroupUuid))) {
            securityGroupService.deleteTeam(new SecurityGroupKey(securityGroupUuid), new TeamKey(teamUuid));
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasPrivilege('GROUPS_READ')")
    public ResponseEntity<SecurityGroupDto> fetch(@PathVariable UUID uuid) {
        return ResponseEntity
                .of(securityGroupService.get(uuid));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasPrivilege('GROUPS_WRITE')")
    public ResponseEntity<SecurityGroupDto> update(@PathVariable UUID uuid, @RequestBody SecurityGroupPutDto securityGroupPutDto) {
        SecurityGroup securityGroup = SecurityGroup.builder()
                .metadataKey(new SecurityGroupKey(securityGroupPutDto.getId()))
                .name(securityGroupPutDto.getName())
                .securedObjects(new HashSet<>())
                .teamKeys(securityGroupPutDto.getTeams().stream()
                        .map(securityGroupTeamPutDto -> new TeamKey(securityGroupPutDto.getId()))
                        .collect(Collectors.toSet()))
                .build();
        securityGroupService.update(securityGroup);
        return ResponseEntity
                .of(securityGroupService.get(uuid));
    }

    @GetMapping("")
    @PreAuthorize("hasPrivilege('GROUPS_READ')")
    public Set<SecurityGroupDto> fetchAll() {
        return securityGroupService.getAll();
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasPrivilege('GROUPS_WRITE')")
    public ResponseEntity<Object> deleteById(@PathVariable UUID uuid) {
        securityGroupService.delete(new SecurityGroupKey(uuid));
        return ResponseEntity.noContent().build();
    }

}
