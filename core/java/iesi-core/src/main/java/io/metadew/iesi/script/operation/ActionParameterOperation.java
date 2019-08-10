package io.metadew.iesi.script.operation;

import io.metadew.iesi.datatypes.DataType;
import io.metadew.iesi.datatypes.DataTypeService;
import io.metadew.iesi.datatypes.text.Text;
import io.metadew.iesi.framework.crypto.FrameworkCrypto;
import io.metadew.iesi.metadata.configuration.type.ActionTypeParameterConfiguration;
import io.metadew.iesi.metadata.definition.action.ActionTypeParameter;
import io.metadew.iesi.runtime.subroutine.ShellCommandSubroutine;
import io.metadew.iesi.runtime.subroutine.SqlStatementSubroutine;
import io.metadew.iesi.script.execution.ActionExecution;
import io.metadew.iesi.script.execution.ExecutionControl;
import org.apache.logging.log4j.Level;

/**
 * Manage all operations for action parameters
 *
 * @author peter.billen
 */
public class ActionParameterOperation {

    private final DataTypeService dataTypeService;
    private ExecutionControl executionControl;
    private ActionExecution actionExecution;
    private String actionTypeName;
    private String name;
    private DataType value;
    private String inputValue = "";

    private ActionTypeParameter actionTypeParameter;
    private SubroutineOperation subroutineOperation;

    // Constructors
    public ActionParameterOperation(ExecutionControl executionControl,
                                    ActionExecution actionExecution, String actionTypeName, String name) {
        this.setExecutionControl(executionControl);
        this.setActionExecution(actionExecution);
        this.setActionTypeName(actionTypeName);
        this.setName(name);
        this.lookupActionTypeParameter();
        this.dataTypeService = new DataTypeService(executionControl.getExecutionRuntime());
    }

    public ActionParameterOperation(ExecutionControl executionControl,
                                    String actionTypeName, String name, String value) {
        this.setExecutionControl(executionControl);
        this.setActionTypeName(actionTypeName);
        this.setName(name);
        this.lookupActionTypeParameter();
        this.setInputValue(value);
        this.dataTypeService = new DataTypeService(executionControl.getExecutionRuntime());
    }

    // Methods
    private void lookupActionTypeParameter() {
        ActionTypeParameterConfiguration actionTypeParameterConfiguration = new ActionTypeParameterConfiguration();
        this.setActionTypeParameter(
                actionTypeParameterConfiguration.getActionTypeParameter(this.getActionTypeName(), this.getName()));
    }


    private String lookupSubroutine(String input) {
        if (this.getActionTypeParameter().getSubroutine() == null
                || this.getActionTypeParameter().getSubroutine().equalsIgnoreCase(""))
            return input;
        this.setSubroutineOperation(new SubroutineOperation(input));
        if (this.getSubroutineOperation().isValid()) {
            if (this.getSubroutineOperation().getSubroutine().getType().equalsIgnoreCase("query")) {
                return new SqlStatementSubroutine(this.getSubroutineOperation().getSubroutine()).getValue();
            } else if (this.getSubroutineOperation().getSubroutine().getType().equalsIgnoreCase("command")) {
                return new ShellCommandSubroutine(this.getSubroutineOperation().getSubroutine()).getValue();
            } else {
                return input;
            }
        } else {
            return input;
        }
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getValue() {
        return value;
    }

    public void setValue(DataType value) {
        // TODO: list -> resolvement to a data type?
        this.value = value;
    }

    public String getActionTypeName() {
        return actionTypeName;
    }

    public void setActionTypeName(String actionTypeName) {
        this.actionTypeName = actionTypeName;
    }

    public String getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        if (inputValue == null) inputValue = "";
        // TODO: list resolvement to a data type
        // Keep input value with orginal entry
    	this.inputValue = inputValue;

    	// Start manipulation with lookups
    	// Look up inside action perimeter
        inputValue = this.getActionExecution().getActionControl().getActionRuntime().resolveRuntimeVariables(inputValue);
		
		// TODO centralize lookup logic here (get inside the execution controls / runtime)
        String resolvedInputValue = this.getExecutionControl().getExecutionRuntime().resolveVariables(this.getActionExecution(), inputValue);
        
        // TODO verify if still needed
        value = new Text(inputValue);
        
        resolvedInputValue = lookupSubroutine(resolvedInputValue);
        this.getExecutionControl().logMessage(this.getActionExecution(),
                "action.param=" + this.getName() + ":" + resolvedInputValue, Level.DEBUG);
        resolvedInputValue = this.getExecutionControl().getExecutionRuntime().resolveConceptLookup(this.getExecutionControl(),
                resolvedInputValue, true).getValue();
        
        // perform lookup again after cross concept lookup
        resolvedInputValue = this.getExecutionControl().getExecutionRuntime().resolveVariables(this.getActionExecution(), resolvedInputValue);
        
        String decryptedInputValue = FrameworkCrypto.getInstance().resolve(resolvedInputValue);

        // Impersonate
        if (this.getActionTypeParameter().getImpersonate().trim().equalsIgnoreCase("y")) {
            String impersonatedConnectionName = this.getExecutionControl().getExecutionRuntime()
                    .getImpersonationOperation().getImpersonatedConnection(decryptedInputValue);
            if (!impersonatedConnectionName.equalsIgnoreCase("")) {
                this.getExecutionControl().logMessage(this.getActionExecution(), "action." + this.getName()
                        + ".impersonate=" + this.getValue() + ":" + impersonatedConnectionName, Level.DEBUG);
                resolvedInputValue = impersonatedConnectionName;
            }
        }

        // Resolve to data type
        value = dataTypeService.resolve(resolvedInputValue);
    }

    public ExecutionControl getExecutionControl() {
        return executionControl;
    }

    public void setExecutionControl(ExecutionControl executionControl) {
        this.executionControl = executionControl;
    }

    public ActionTypeParameter getActionTypeParameter() {
        return actionTypeParameter;
    }

    public void setActionTypeParameter(ActionTypeParameter actionTypeParameter) {
        this.actionTypeParameter = actionTypeParameter;
    }

    public SubroutineOperation getSubroutineOperation() {
        return subroutineOperation;
    }

    public void setSubroutineOperation(SubroutineOperation subroutineOperation) {
        this.subroutineOperation = subroutineOperation;
    }

    public ActionExecution getActionExecution() {
        return actionExecution;
    }

    public void setActionExecution(ActionExecution actionExecution) {
        this.actionExecution = actionExecution;
    }

}