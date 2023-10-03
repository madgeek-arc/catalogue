package gr.athenarc.catalogue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a resource could not be found.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a ResourceNotFoundException with default message.
     */
    public ResourceNotFoundException() {
        super("Resource Not Found");
    }

    /**
     * Constructs a ResourceNotFoundException displaying the searched id.
     */
    public ResourceNotFoundException(String id) {
        super(String.format("Resource with [id=%s] was not found", id));
    }

    /**
     * Constructs a ResourceNotFoundException displaying the resourceType and the searched id.
     */
    public ResourceNotFoundException(String id, String resourceType) {
        super(String.format("[resourceType=%s] with [id=%s] was not found", resourceType, id));
    }
}
