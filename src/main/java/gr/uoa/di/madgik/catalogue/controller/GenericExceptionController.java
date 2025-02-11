/**
 * Copyright 2021-2025 OpenAIRE AMKE
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.SQLException;

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
    protected ResponseEntity<ServerError> handleException(HttpServletRequest req, Exception ex) {
        HttpStatusCode status = getStatusFromException(ex);

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
        } else if (ex instanceof ResourceNotFoundException) {
            logger.info(ex.getMessage());
            logger.debug(ex.getMessage(), ex);
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof ValidationException) {
            logger.info(ex.getMessage());
            logger.debug(ex.getMessage(), ex);
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof SQLException || ex instanceof DataAccessException) {
            logger.error(ex.getMessage(), ex);
            status = HttpStatus.UNPROCESSABLE_ENTITY;
            return ResponseEntity.status(status).body(new ServerError(status, req, "Could not process request"));
        } else {
            logger.error(ex.getMessage(), ex);
            ex = new RuntimeException("Internal Server Error", ex); // wrap exception to hide unknown error message.
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
