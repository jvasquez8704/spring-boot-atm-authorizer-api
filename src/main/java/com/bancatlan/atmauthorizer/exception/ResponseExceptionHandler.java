package com.bancatlan.atmauthorizer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@RestController
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {
    Logger LOG = LoggerFactory.getLogger(ResponseExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex, WebRequest request){
        ExceptionResponse er = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        LOG.error("INTERNAL_SERVER_ERROR  {}", er);
        //return new ResponseEntity<ExceptionResponse>(er, HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<ExceptionResponse>(er, HttpStatus.OK);
    }

    @ExceptionHandler(ModelCustomErrorException.class)
    public final ResponseEntity<ExceptionResponse> handleModelException(ModelCustomErrorException ex, WebRequest request){
        ExceptionResponse er = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(), request.getDescription(false), ex.getCustomError());
        LOG.error("ModelCustomErrorException  {}", er);
        //return new ResponseEntity<ExceptionResponse>(er, HttpStatus.PRECONDITION_REQUIRED);
        return new ResponseEntity<ExceptionResponse>(er, HttpStatus.OK);
    }

    @ExceptionHandler(ModelNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleNotFoundException(ModelNotFoundException ex, WebRequest request){
        ExceptionResponse er = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(), request.getDescription(false), ex.getCustomError());
        LOG.error("ModelNotFoundException  {}", er);
        //return new ResponseEntity<ExceptionResponse>(er, HttpStatus.NOT_FOUND);
        return new ResponseEntity<ExceptionResponse>(er, HttpStatus.OK);
    }

    @ExceptionHandler(ModelAtmErrorException.class)
    public final ResponseEntity<AtmExceptionResponse> handleAtmException(ModelAtmErrorException ex, WebRequest request){
        AtmExceptionResponse er = new AtmExceptionResponse(ex.getData(), ex.getCustomError());
        LOG.error("AtmErrorException  {}", er);
        return new ResponseEntity<>(er, HttpStatus.OK);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ExceptionResponse er = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        LOG.error("MethodArgumentNotValidException  {}", er);
        //return new ResponseEntity<Object>(er, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Object>(er, HttpStatus.OK);
    }
}
