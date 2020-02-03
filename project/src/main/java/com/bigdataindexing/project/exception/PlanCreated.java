package com.bigdataindexing.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CREATED)
public class PlanCreated extends RuntimeException{

    public PlanCreated(String message) {
        super(message);
    }
}