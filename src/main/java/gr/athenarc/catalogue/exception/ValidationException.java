package gr.athenarc.catalogue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a resource fails validation.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    /**
     * Constructs a ValidationException with default message.
     */
    public ValidationException() {
        super("Resource Not Valid");
    }

    /**
     * Constructs a ValidationException with the specified message.
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }

}
