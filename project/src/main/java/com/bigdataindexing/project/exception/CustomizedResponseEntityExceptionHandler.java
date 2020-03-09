package com.bigdataindexing.project.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(PlanAlreadyPresentException.class)
    @ResponseBody
    public final ResponseEntity<Object> handlePlanAlreadyPresentException(PlanAlreadyPresentException ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.ALREADY_REPORTED.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.ALREADY_REPORTED);
    }

    @ExceptionHandler(PlanNotFoundException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleRecipeNotFoundException(PlanNotFoundException ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.NOT_FOUND.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(InvalidInputException.class)
    @ResponseBody
    public final ResponseEntity<Object> InvalidInputException(InvalidInputException ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.BAD_REQUEST.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PlanCreated.class)
    @ResponseBody
    public final ResponseEntity<Object> PlanCreated(PlanCreated ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.CREATED.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.CREATED);
    }

    @ExceptionHandler(SuccessResponse.class)
    @ResponseBody
    public final ResponseEntity<Object> PlanCreated(SuccessResponse ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.OK.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.OK);
    }

    @ExceptionHandler(MissingEtagException.class)
    @ResponseBody
    public final ResponseEntity<Object> missingetag(MissingEtagException ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.PRECONDITION_FAILED.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.PRECONDITION_FAILED);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        Response exceptionResponse = new Response("Validation Failed",
                ex.getBindingResult().toString());
        return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if(headers.get("If-Match") == null){
            if(headers.get("If-None-Match") == null){
                Response exceptionResponse = new Response(HttpStatus.PRECONDITION_FAILED.toString(), "Etag headers missing");
                return new ResponseEntity(exceptionResponse.toString(), HttpStatus.PRECONDITION_FAILED);
            }
        }
        Response exceptionResponse = new Response(HttpStatus.BAD_REQUEST.toString(), "Invalid Request Body");
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.BAD_REQUEST);
    }


}
