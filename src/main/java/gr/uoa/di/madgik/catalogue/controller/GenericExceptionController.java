/*
 * Copyright 2021-2026 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.uoa.di.madgik.catalogue.controller;

import gr.uoa.di.madgik.catalogue.exception.ServerError;
import gr.uoa.di.madgik.catalogue.exception.ValidationException;
import gr.uoa.di.madgik.registry.exception.ResourceAlreadyExistsException;
import gr.uoa.di.madgik.registry.exception.ResourceException;
import gr.uoa.di.madgik.registry.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.SQLException;

/**
 * Advice handling all thrown exceptions.
 */
@RestControllerAdvice
public class GenericExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionController.class);

    /**
     * Handles registry exceptions that already expose an HTTP status.
     *
     * @param req http servlet request
     * @param ex  the thrown exception
     * @return {@link ServerError}
     */
    @ExceptionHandler(value = ResourceException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleResourceException(HttpServletRequest req, ResourceException ex) {
        logger.info(ex.getMessage(), ex);
        return buildErrorResponse(req, ex.getStatus(), ex);
    }

    @ExceptionHandler(value = HttpClientErrorException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleHttpClientError(HttpServletRequest req, HttpClientErrorException ex) {
        logger.info(ex.getMessage(), ex);
        return buildErrorResponse(req, ex.getStatusCode(), ex);
    }

    @ExceptionHandler(value = AccessDeniedException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleAccessDenied(HttpServletRequest req, AccessDeniedException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(value = InsufficientAuthenticationException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleInsufficientAuthentication(HttpServletRequest req,
                                                                           InsufficientAuthenticationException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(value = ResourceAlreadyExistsException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleResourceAlreadyExists(HttpServletRequest req,
                                                                      ResourceAlreadyExistsException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleResourceNotFound(HttpServletRequest req, ResourceNotFoundException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleMethodArgumentNotValid(HttpServletRequest req,
                                                                       MethodArgumentNotValidException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        reportException(req, ex, ex.getStatusCode());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ServerError(ex.getStatusCode(), req, ex.getBody().getDetail()));
    }

    @ExceptionHandler(value = ValidationException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleValidation(HttpServletRequest req, ValidationException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(value = UnsupportedOperationException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleUnsupportedOperation(HttpServletRequest req,
                                                                     UnsupportedOperationException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.NOT_IMPLEMENTED, ex);
    }

    @ExceptionHandler(value = {SQLException.class, DataAccessException.class}, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handlePersistenceException(HttpServletRequest req, Exception ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ServerError(HttpStatus.UNPROCESSABLE_ENTITY, req, "Could not process request"));
    }

    /**
     * Transforms any uncaught exception to a generic internal server error response.
     *
     * @param req http servlet request
     * @param ex  the thrown exception
     * @return {@link ServerError}
     */
    @ExceptionHandler(value = Exception.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ServerError> handleException(HttpServletRequest req, Exception ex) {
        logger.error(ex.getMessage(), ex);
        HttpStatusCode status = getStatusFromException(ex);
        return buildErrorResponse(req, status, new RuntimeException("Internal Server Error", ex));
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

    private ResponseEntity<ServerError> buildErrorResponse(HttpServletRequest req, HttpStatusCode status, Exception ex) {
        reportException(req, ex, status);
        return ResponseEntity
                .status(status)
                .body(new ServerError(status, req, ex));
    }

    /**
     * Hook for subclasses that need to report handled exceptions to external systems.
     *
     * @param req    http servlet request
     * @param ex     the handled exception
     * @param status the HTTP status returned to the client
     */
    protected void reportException(HttpServletRequest req, Exception ex, HttpStatusCode status) {
    }
}
