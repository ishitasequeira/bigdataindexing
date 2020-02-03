package com.bigdataindexing.project.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SuccessResponse extends RuntimeException {
    public SuccessResponse(String message) {
        super(message);
    }

}
