package gr.athenarc.catalogue.controller;

import eu.openminted.registry.core.controllers.GenericController;
import gr.athenarc.catalogue.exception.ResourceAlreadyExistsException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.exception.ServerError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GenericExceptionController extends GenericController {

    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionController.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    ServerError handleNotFound(HttpServletRequest req, Exception ex) {
        logger.info(ex.getMessage(), ex);
        return new ServerError(HttpStatus.NOT_FOUND, req, ex);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseBody
    ServerError handleAlreadyExists(HttpServletRequest req, Exception ex) {
        logger.info(ex.getMessage(), ex);
        return new ServerError(HttpStatus.CONFLICT, req, ex);
    }
}
