package io.metadew.iesi.metadata.definition.script;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import io.metadew.iesi.metadata.definition.MetadataJsonComponent;
import io.metadew.iesi.metadata.definition.action.Action;
import io.metadew.iesi.metadata.definition.action.ActionJsonComponent;
import io.metadew.iesi.metadata.definition.action.ActionParameter;
import io.metadew.iesi.metadata.definition.action.ActionParameterJsonComponent;
import io.metadew.iesi.metadata.definition.action.key.ActionKey;
import io.metadew.iesi.metadata.definition.action.key.ActionParameterKey;
import io.metadew.iesi.metadata.definition.script.key.ScriptParameterKey;
import io.metadew.iesi.metadata.definition.script.key.ScriptVersionKey;
import io.metadew.iesi.metadata.tools.IdentifierTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScriptJsonComponent {

    public enum Field {
        TYPE("script"),
        ID_KEY("id"),
        TYPE_KEY("type"),
        NAME_KEY("name"),
        DESCRIPTION_KEY("description"),
        VERSION_KEY("version"),
        PARAMETERS_KEY("parameters"),
        ACTIONS_KEY("actions"),
        VALUE_KEY("value");


        private final String label;

        Field(String label) {
            this.label = label;
        }

        public String value() {
            return label;
        }
    }

    public static class Deserializer extends JsonDeserializer<Script> {
        @Override
        public Script deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String scriptId = IdentifierTools.getScriptIdentifier(node.get(Field.NAME_KEY.value()).asText());
            ScriptVersion scriptVersion;

            JsonNode versionNode = node.get(Field.VERSION_KEY.value());
            long versionNumber;
            if (versionNode != null) {
                versionNumber = versionNode.get(ScriptVersionJsonComponent.Field.NUMBER_KEY.value()).asLong();
                scriptVersion = new ScriptVersion(
                        new ScriptVersionKey(scriptId, versionNumber),
                        versionNode.get(ScriptVersionJsonComponent.Field.DESCRIPTION_KEY.value()).asText()
                );
            } else {
                versionNumber = 0L;
                scriptVersion = new ScriptVersion(new ScriptVersionKey(scriptId, versionNumber), "default version");
            }

            //version

            //script parameters
            List<ScriptParameter> scriptParameters = new ArrayList<>();
            if (node.get(ScriptJsonComponent.Field.PARAMETERS_KEY.value()) != null) {
                for (JsonNode scriptParameterNode : node.get(ScriptJsonComponent.Field.PARAMETERS_KEY.value())) {
                    scriptParameters.add(new ScriptParameter(new ScriptParameterKey(scriptId, versionNumber,
                            scriptParameterNode.get(ScriptParameterJsonComponent.Field.PARAMETER_NAME_KEY.value()).asText()),
                            scriptParameterNode.get(ScriptParameterJsonComponent.Field.PARAMETER_VALUE_KEY.value()).asText()));
                }
            }

            //script actions
            List<Action> scriptActions = new ArrayList<>();
            for (JsonNode scriptActionNode : node.get(Field.ACTIONS_KEY.value())) {
                String action_id = IdentifierTools.getActionIdentifier(scriptActionNode.get(ActionJsonComponent.Field.NAME_KEY.value()).asText());

                // action parameters
                List<ActionParameter> actionParameters = new ArrayList<>();
                for (JsonNode scriptActionParNode : scriptActionNode.get(ActionJsonComponent.Field.PARAMETERS_KEY.value())) {
                    actionParameters.add(new ActionParameter(
                            new ActionParameterKey(
                                    scriptId, versionNumber, action_id,
                                    scriptActionParNode.get(ActionParameterJsonComponent.Field.PARAMETER_NAME_KEY.value()).asText()
                            ),
                            scriptActionParNode.get(ActionParameterJsonComponent.Field.PARAMETER_VALUE_KEY.value()).asText()
                    ));
                }

                scriptActions.add(new Action(new ActionKey(
                        scriptId,
                        versionNumber,
                        action_id),
                        scriptActionNode.get(ActionJsonComponent.Field.NUMBER_KEY.value()).asLong(),
                        scriptActionNode.get(ActionJsonComponent.Field.TYPE_KEY.value()).asText(),
                        scriptActionNode.get(ActionJsonComponent.Field.NAME_KEY.value()).asText(),
                        scriptActionNode.get(ActionJsonComponent.Field.DESCRIPTION_KEY.value()).asText(),
                        scriptActionNode.get(ActionJsonComponent.Field.COMPONENT_KEY.value()) == null ? "" : scriptActionNode.get(ActionJsonComponent.Field.COMPONENT_KEY.value()).asText(),
                        scriptActionNode.get(ActionJsonComponent.Field.CONDITION_KEY.value()) == null ? "" : scriptActionNode.get(ActionJsonComponent.Field.CONDITION_KEY.value()).asText(),
                        scriptActionNode.get(ActionJsonComponent.Field.ITERATION_KEY.value()) == null ? "" : scriptActionNode.get(ActionJsonComponent.Field.ITERATION_KEY.value()).asText(),
                        scriptActionNode.get(ActionJsonComponent.Field.ERROR_EXPECTED_KEY.value()).asText(),
                        scriptActionNode.get(ActionJsonComponent.Field.ERROR_STOP_KEY.value()).asText(),
                        scriptActionNode.get(ActionJsonComponent.Field.RETRIES_KEY.value()) == null ? "0" : scriptActionNode.get(ActionJsonComponent.Field.RETRIES_KEY.value()).asText(),
                        actionParameters));
            }

            return new Script(scriptId,
                    node.get(Field.NAME_KEY.value()).asText(),
                    node.get(Field.DESCRIPTION_KEY.value()).asText(),
                    scriptVersion,
                    scriptParameters,
                    scriptActions);
        }
    }

    public static class Serializer extends JsonSerializer<Script> {
        @Override
        public void serialize(Script script, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(MetadataJsonComponent.Field.TYPE_KEY.value(), ScriptJsonComponent.Field.TYPE.value());

            jsonGenerator.writeObjectFieldStart(MetadataJsonComponent.Field.DATA_KEY.value());

            jsonGenerator.writeStringField(Field.ID_KEY.value(), script.getId());
            jsonGenerator.writeStringField(Field.NAME_KEY.value(), script.getName());
            jsonGenerator.writeStringField(Field.DESCRIPTION_KEY.value(), script.getDescription());

            // write version
            ScriptVersion scriptVersion = script.getVersion();
            jsonGenerator.writeObjectFieldStart(Field.VERSION_KEY.value());
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField(ScriptVersionJsonComponent.Field.NUMBER_KEY.value(), scriptVersion.getNumber());
            jsonGenerator.writeStringField(ScriptVersionJsonComponent.Field.DESCRIPTION_KEY.value(), scriptVersion.getDescription());
            jsonGenerator.writeEndObject();

            // write parameters
            jsonGenerator.writeArrayFieldStart(Field.PARAMETERS_KEY.value());
            for (ScriptParameter scriptParameter : script.getParameters()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(ScriptParameterJsonComponent.Field.PARAMETER_NAME_KEY.value(), scriptParameter.getName());
                jsonGenerator.writeStringField(ScriptParameterJsonComponent.Field.PARAMETER_VALUE_KEY.value(), scriptParameter.getValue());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();

            // write actions
            jsonGenerator.writeArrayFieldStart(Field.ACTIONS_KEY.value());
            for (Action scriptAction : script.getActions()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(ActionJsonComponent.Field.ID_KEY.value(), scriptAction.getMetadataKey().getActionId());
                jsonGenerator.writeNumberField(ActionJsonComponent.Field.NUMBER_KEY.value(), scriptAction.getNumber());
                jsonGenerator.writeStringField(ActionJsonComponent.Field.TYPE_KEY.value(), scriptAction.getType());
                jsonGenerator.writeStringField(ActionJsonComponent.Field.NAME_KEY.value(), scriptAction.getName());
                jsonGenerator.writeStringField(ActionJsonComponent.Field.DESCRIPTION_KEY.value(), scriptAction.getDescription());
                jsonGenerator.writeStringField(ActionJsonComponent.Field.COMPONENT_KEY.value(), scriptAction.getComponent());
                jsonGenerator.writeStringField(ActionJsonComponent.Field.CONDITION_KEY.value(), scriptAction.getCondition());
                jsonGenerator.writeStringField(ActionJsonComponent.Field.ITERATION_KEY.value(), scriptAction.getIteration());
                jsonGenerator.writeStringField(ActionJsonComponent.Field.ERROR_EXPECTED_KEY.value(), scriptAction.getErrorExpected() ? "Y" : "N");
                jsonGenerator.writeStringField(ActionJsonComponent.Field.ERROR_STOP_KEY.value(), scriptAction.getErrorStop() ? "Y" : "N");
                jsonGenerator.writeNumberField(ActionJsonComponent.Field.RETRIES_KEY.value(), scriptAction.getRetries());

                // write action parameters
                for (ActionParameter actionParameter : scriptAction.getParameters()) {
                    jsonGenerator.writeStringField(ActionParameterJsonComponent.Field.PARAMETER_NAME_KEY.value(), actionParameter.getMetadataKey().getParameterName());
                    jsonGenerator.writeStringField(ActionParameterJsonComponent.Field.PARAMETER_VALUE_KEY.value(), actionParameter.getValue());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
            jsonGenerator.writeEndObject();
        }
    }
}
