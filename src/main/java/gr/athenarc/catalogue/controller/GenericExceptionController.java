package gr.athenarc.catalogue.controller;

import eu.openminted.registry.core.controllers.GenericController;
import eu.openminted.registry.core.exception.ServerError;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GenericExceptionController extends GenericController {

    private static final Logger logger = LogManager.getLogger(GenericExceptionController.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    ServerError handleNotFound(HttpServletRequest req, Exception ex) {
        logger.info(ex);
        return new ServerError(req.getRequestURL().toString(), ex);
    }

}
