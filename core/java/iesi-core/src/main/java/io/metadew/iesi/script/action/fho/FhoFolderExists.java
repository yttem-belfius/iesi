package io.metadew.iesi.script.action.fho;

import io.metadew.iesi.connection.FileConnection;
import io.metadew.iesi.connection.HostConnection;
import io.metadew.iesi.connection.operation.ConnectionOperation;
import io.metadew.iesi.connection.tools.FolderTools;
import io.metadew.iesi.connection.tools.HostConnectionTools;
import io.metadew.iesi.connection.tools.fho.FileConnectionTools;
import io.metadew.iesi.datatypes.DataType;
import io.metadew.iesi.datatypes.text.Text;
import io.metadew.iesi.metadata.configuration.connection.ConnectionConfiguration;
import io.metadew.iesi.metadata.definition.connection.Connection;
import io.metadew.iesi.metadata.definition.connection.key.ConnectionKey;
import io.metadew.iesi.script.action.ActionTypeExecution;
import io.metadew.iesi.script.execution.ActionExecution;
import io.metadew.iesi.script.execution.ExecutionControl;
import io.metadew.iesi.script.execution.ScriptExecution;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

/**
 * Action type to verify if a folder exists.
 *
 * @author peter.billen
 */
public class FhoFolderExists extends ActionTypeExecution {

    private static final String FOLDER_PATH_KEY = "path";
    private static final String FOLDER_NAME_KEY = "folder";
    private static final String CONNECTION_NAME_KEY = "connection";
    private static final Logger LOGGER = LogManager.getLogger();

    public FhoFolderExists(ExecutionControl executionControl,
                           ScriptExecution scriptExecution, ActionExecution actionExecution) {
        super(executionControl, scriptExecution, actionExecution);
    }

    public void prepareAction() { }

    protected boolean executeAction() throws InterruptedException {
        String path = convertPath(getParameterResolvedValue(FOLDER_PATH_KEY));
        String folder = convertFolder(getParameterResolvedValue(FOLDER_NAME_KEY));
        String connectionName = convertConnectionName(getParameterResolvedValue(CONNECTION_NAME_KEY));
        boolean isOnLocalhost = HostConnectionTools.isOnLocalhost(
                connectionName, this.getExecutionControl().getEnvName());

        String subjectFolderPath = "";
        if (path.isEmpty()) {
            if (isOnLocalhost) {
                subjectFolderPath = FilenameUtils.normalize(folder);
            } else {
                subjectFolderPath = folder;
            }
        } else {
            if (isOnLocalhost) {
                subjectFolderPath = FilenameUtils.normalize(
                        path + File.separator + folder);
            } else {
                subjectFolderPath = path+ "/" + folder;
            }
        }
        File file = new File(subjectFolderPath);

        boolean result = false;
        if (isOnLocalhost) {

            List<FileConnection> fileConnections = FolderTools.getFilesInFolder(file.getParent(), file.getName());
            for (FileConnection fileConnection : fileConnections) {
                if (fileConnection.isDirectory()
                        && fileConnection.getFilePath().equalsIgnoreCase(subjectFolderPath)) {
                    this.setScope(fileConnection.getFilePath());
                    result = true;
                    this.setSuccess();
                }
            }
        } else {
            Connection connection = ConnectionConfiguration.getInstance()
                    .get(new ConnectionKey(connectionName, this.getExecutionControl().getEnvName()))
                    .get();
            HostConnection hostConnection = ConnectionOperation.getInstance().getHostConnection(connection);

            for (FileConnection fileConnection : FileConnectionTools.getFileConnections(hostConnection,
                    FilenameUtils.separatorsToUnix(file.getParent()), FilenameUtils.separatorsToUnix(file.getName()), true)) {
                if (fileConnection.isDirectory()
                        && fileConnection.getFilePath().equalsIgnoreCase(subjectFolderPath)) {
                    this.setScope(fileConnection.getFilePath());
                    result = true;
                    this.setSuccess();
                }
            }
        }

        if (!result) {
            this.setError("notfound");
        }

        return true;
    }

    @Override
    protected String getKeyword() {
        return "fho.folderExists";
    }


    private String convertConnectionName(DataType connectionName) {
        if (connectionName instanceof Text) {
            return connectionName.toString();
        } else {
            LOGGER.warn(MessageFormat.format(this.getActionExecution().getAction().getType() + " does not accept {0} as type for connectionName",
                    connectionName.getClass()));
            return connectionName.toString();
        }
    }

    private String convertFolder(DataType folderName) {
        if (folderName instanceof Text) {
            return folderName.toString();
        } else {
            LOGGER.warn(MessageFormat.format(this.getActionExecution().getAction().getType() + " does not accept {0} as type for folderName",
                    folderName.getClass()));
            return folderName.toString();
        }
    }

    private String convertPath(DataType folderName) {
        if (folderName instanceof Text) {
            return folderName.toString();
        } else {
            LOGGER.warn(MessageFormat.format(this.getActionExecution().getAction().getType() + " does not accept {0} as type for folderName",
                    folderName.getClass()));
            return folderName.toString();
        }
    }

    private void setScope(String input) {
        this.getActionExecution().getActionControl().logOutput("folder.exists", input);
    }

    private void setError(String input) {
        this.getActionExecution().getActionControl().logOutput("folder.exists.error", input);
        this.getActionExecution().getActionControl().increaseErrorCount();
    }

    private void setSuccess() {
        this.getActionExecution().getActionControl().logOutput("folder.exists.success", "confirmed");
        this.getActionExecution().getActionControl().increaseSuccessCount();
    }
}