package io.metadew.iesi.data.generation.output.control;

import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.data.generation.execution.GenerationControlExecution;
import io.metadew.iesi.data.generation.execution.GenerationControlRuleExecution;
import io.metadew.iesi.framework.execution.FrameworkExecution;
import io.metadew.iesi.metadata.definition.generation.GenerationControlRule;
import io.metadew.iesi.script.execution.ExecutionControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Header {

    private GenerationControlExecution generationControlExecution;
    private FrameworkExecution frameworkExecution;
    private ExecutionControl executionControl;
    private String generationControlTypeName = "header";
    private static final Logger LOGGER = LogManager.getLogger();

    // Parameters

    // Constructors
    public Header(FrameworkExecution frameworkExecution, ExecutionControl executionControl,
                  GenerationControlExecution generationControlExecution) {
        this.setFrameworkExecution(frameworkExecution);
        this.setExecutionControl(executionControl);
        this.setGenerationControlExecution(generationControlExecution);
    }

    //
    public boolean execute(String fullFileName) {
        LOGGER.info("generation.control.type=" + this.generationControlTypeName);
        try {
            // Reset Parameters

            // Get Parameters

            // Run the generation control
            StringBuilder header = new StringBuilder();

            for (GenerationControlRule generationControlRule : this.getGenerationControlExecution()
                    .getGenerationControl().getRules()) {
                GenerationControlRuleExecution generationControlRuleExecution = new GenerationControlRuleExecution(
                        this.getFrameworkExecution(), this.getExecutionControl(),
                        this.getGenerationControlExecution().getGenerationExecution(), generationControlRule);
                generationControlRuleExecution.execute();
                header.append(generationControlRuleExecution.getOutput());
            }

            // Create new file
            String tempFullFileName = fullFileName + ".tmp";
            FileTools.delete(tempFullFileName);
            FileTools.appendToFile(tempFullFileName, "",
                    header.toString());

            // Copy file contents
            File file = new File(fullFileName);
            @SuppressWarnings("resource")
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String readLine = "";
            while ((readLine = bufferedReader.readLine()) != null) {
                FileTools.appendToFile(tempFullFileName, "",
                        readLine);
            }

            // copy file to orginal one
            FileTools.copyFromFileToFile(tempFullFileName, fullFileName);

            // delete temporary file
            FileTools.delete(tempFullFileName);

            bufferedReader.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Getters and Setters
    public String getGenerationControlTypeName() {
        return generationControlTypeName;
    }

    public void setGenerationControlTypeName(String generationControlTypeName) {
        this.generationControlTypeName = generationControlTypeName;
    }

    public ExecutionControl getExecutionControl() {
        return executionControl;
    }

    public void setExecutionControl(ExecutionControl executionControl) {
        this.executionControl = executionControl;
    }

    public GenerationControlExecution getGenerationControlExecution() {
        return generationControlExecution;
    }

    public void setGenerationControlExecution(GenerationControlExecution generationControlExecution) {
        this.generationControlExecution = generationControlExecution;
    }

    public FrameworkExecution getFrameworkExecution() {
        return frameworkExecution;
    }

    public void setFrameworkExecution(FrameworkExecution frameworkExecution) {
        this.frameworkExecution = frameworkExecution;
    }

}