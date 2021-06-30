package io.metadew.iesi.server.rest.script.dto.label;

import io.metadew.iesi.metadata.definition.script.ScriptLabel;
import io.metadew.iesi.metadata.definition.script.key.ScriptKey;
import io.metadew.iesi.metadata.definition.script.key.ScriptLabelKey;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ScriptLabelDtoService implements IScriptLabelDtoService {

    public ScriptLabel convertToEntity(ScriptLabelDto scriptLabelDto, ScriptKey scriptKey) {
        return new ScriptLabel(new ScriptLabelKey(UUID.randomUUID().toString()),
                scriptKey, scriptLabelDto.getName(), scriptLabelDto.getValue());
    }

    public ScriptLabelDto convertToDto(ScriptLabel scriptLabel) {
        return new ScriptLabelDto(scriptLabel.getName(), scriptLabel.getValue());
    }

}
