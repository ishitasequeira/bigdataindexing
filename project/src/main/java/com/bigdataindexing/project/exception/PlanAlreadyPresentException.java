package com.bigdataindexing.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PlanAlreadyPresentException extends RuntimeException {
    public PlanAlreadyPresentException(String message) {
        super(message);
    }
}