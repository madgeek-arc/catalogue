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

import gr.uoa.di.madgik.catalogue.exception.ValidationException;
import gr.uoa.di.madgik.registry.exception.ResourceAlreadyExistsException;
import gr.uoa.di.madgik.registry.exception.ResourceException;
import gr.uoa.di.madgik.registry.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.SQLException;
import java.net.URI;
import java.time.Instant;

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
     * @return {@link ProblemDetail}
     */
    @ExceptionHandler(value = ResourceException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleResourceException(HttpServletRequest req, ResourceException ex) {
        logger.info(ex.getMessage(), ex);
        return buildErrorResponse(req, ex.getStatus(), ex);
    }

    @ExceptionHandler(value = HttpClientErrorException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleHttpClientError(HttpServletRequest req, HttpClientErrorException ex) {
        logger.info(ex.getMessage(), ex);
        return buildErrorResponse(req, ex.getStatusCode(), ex);
    }

    @ExceptionHandler(value = AccessDeniedException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleAccessDenied(HttpServletRequest req, AccessDeniedException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(value = InsufficientAuthenticationException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleInsufficientAuthentication(HttpServletRequest req,
                                                                             InsufficientAuthenticationException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(value = ResourceAlreadyExistsException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleResourceAlreadyExists(HttpServletRequest req,
                                                                        ResourceAlreadyExistsException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleResourceNotFound(HttpServletRequest req, ResourceNotFoundException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(HttpServletRequest req,
                                                                         MethodArgumentNotValidException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        reportException(req, ex, ex.getStatusCode());
        return ResponseEntity.status(ex.getStatusCode()).body(buildProblemDetail(req, ex.getStatusCode(), ex.getBody().getDetail()));
    }

    @ExceptionHandler(value = ValidationException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleValidation(HttpServletRequest req, ValidationException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(value = UnsupportedOperationException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleUnsupportedOperation(HttpServletRequest req,
                                                                       UnsupportedOperationException ex) {
        logger.info(ex.getMessage());
        logger.debug(ex.getMessage(), ex);
        return buildErrorResponse(req, HttpStatus.NOT_IMPLEMENTED, ex);
    }

    @ExceptionHandler(value = {SQLException.class, DataAccessException.class}, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handlePersistenceException(HttpServletRequest req, Exception ex) {
        logger.error(ex.getMessage(), ex);
        reportException(req, ex, HttpStatus.UNPROCESSABLE_ENTITY);
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(buildProblemDetail(req, HttpStatus.UNPROCESSABLE_ENTITY, "Could not process request"));
    }

    /**
     * Transforms any uncaught exception to a generic internal server error response.
     *
     * @param req http servlet request
     * @param ex  the thrown exception
     * @return {@link ProblemDetail}
     */
    @ExceptionHandler(value = Exception.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ProblemDetail> handleException(HttpServletRequest req, Exception ex) {
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

    protected ResponseEntity<ProblemDetail> buildErrorResponse(HttpServletRequest req, HttpStatusCode status, Exception ex) {
        reportException(req, ex, status);
        return ResponseEntity
                .status(status)
                .body(buildProblemDetail(req, status, ex.getMessage()));
    }

    protected ProblemDetail buildProblemDetail(HttpServletRequest req, HttpStatusCode status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        if (status instanceof HttpStatus httpStatus) {
            problemDetail.setTitle(httpStatus.getReasonPhrase());
        }
        problemDetail.setInstance(getUriWithParams(req));
        problemDetail.setProperty("method", req.getMethod());
        problemDetail.setProperty("traceId", MDC.get("traceId"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
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

    public static URI getUriWithParams(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append('?');
            sb.append(request.getQueryString());
        }
        return URI.create(sb.toString());
    }
}
