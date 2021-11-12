package gr.athenarc.catalogue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Resource Not Found");
    }

    public ResourceNotFoundException(String id) {
        super(String.format("Resource with id [%s] was not found", id));
    }

    public ResourceNotFoundException(String id, String resourceType) {
        super(String.format("ResourceType [%s] with id [%s] was not found",resourceType, id));
    }
}
