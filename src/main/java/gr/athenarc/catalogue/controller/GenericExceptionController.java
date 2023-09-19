package gr.athenarc.catalogue.controller;

import gr.athenarc.catalogue.exception.ResourceAlreadyExistsException;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.exception.ServerError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;

/**
 * Advice handling all thrown Exceptions.
 */
@ControllerAdvice
public class GenericExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionController.class);

    /**
     * Transforms every thrown exception to a {@link ServerError} response.
     *
     * @param req http servlet request
     * @param ex  the thrown exception
     * @return {@link ServerError}
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ServerError> handleException(HttpServletRequest req, Exception ex) {
        HttpStatus status = getStatusFromException(ex);

        if (ex instanceof ResourceException) {
            logger.info(ex.getMessage(), ex);
            status = ((ResourceException) ex).getStatus();
        } else if (ex instanceof HttpClientErrorException) {
            logger.info(ex.getMessage(), ex);
            status = ((HttpClientErrorException) ex).getStatusCode();
        } else if (ex instanceof AccessDeniedException) {
            logger.info(ex.getMessage());
            logger.debug(ex.getMessage(), ex);
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof InsufficientAuthenticationException) {
            logger.info(ex.getMessage());
            logger.debug(ex.getMessage(), ex);
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof ResourceAlreadyExistsException) {
            logger.info(ex.getMessage());
            logger.debug(ex.getMessage(), ex);
            status = HttpStatus.CONFLICT;
        } else if (ex instanceof ResourceNotFoundException || ex instanceof eu.openminted.registry.core.exception.ResourceNotFoundException) {
            logger.info(ex.getMessage());
            logger.debug(ex.getMessage(), ex);
            status = HttpStatus.NOT_FOUND;
        } else {
            logger.error(ex.getMessage(), ex);
        }
        return ResponseEntity
                .status(status)
                .body(new ServerError(status, req, ex));
    }

    /**
     * Get http status code from {@link ResponseStatus} annotation using reflection, if it exists, else return {@literal HttpStatus.INTERNAL_SERVER_ERROR}.
     *
     * @param exception thrown exception
     * @return {@link HttpStatus}
     */
    private HttpStatus getStatusFromException(Exception exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseStatus annotation = exception.getClass().getAnnotation(ResponseStatus.class);
        if (annotation != null) {
            status = annotation.value();
        }
        return status;
    }
}
