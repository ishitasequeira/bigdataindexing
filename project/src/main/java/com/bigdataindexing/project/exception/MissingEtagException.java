package com.bigdataindexing.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class MissingEtagException extends RuntimeException {
    public MissingEtagException(String message) {
        super(message);
    }
}