package io.metadew.iesi.server.rest.error;

import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.openapi.SwaggerParserException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MetadataDoesNotExistException.class)
    public void handleMetadataDoesNotExistException(HttpServletResponse response) {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MetadataAlreadyExistsException.class)
    public void handleMetadataAlreadyExistsException(HttpServletResponse response) {
    }

    @ExceptionHandler(DataBadRequestException.class)
    public void HandleBadRequest(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SwaggerParserException.class)
    @ResponseBody
    public Map<String, String> handleSwaggerParserException(SwaggerParserException e) {
        Map<String, String> errMessages = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : e.getMessages()) {
            stringBuilder.append(String.format("- %s %n", message));
        }

        errMessages.put("error", "Something is wrong in your documentation");
        errMessages.put("errorCode", "400");
        errMessages.put("message", String.format("We found the following errors : %n %s", stringBuilder));
        return errMessages;
    }

    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleException(MethodArgumentTypeMismatchException e) {
        Map<String, String> errMessages = new HashMap<>();
        errMessages.put("error", "Wrong query parameter for '" + e.getName() + "'");
        errMessages.put("query_parameter", e.getName());
        errMessages.put("errorCode", e.getErrorCode() + " , " + e.getRootCause().toString());
        errMessages.put("message", e.getMessage());
        return errMessages;
    }
}