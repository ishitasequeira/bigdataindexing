package com.bigdataindexing.project.exception;

import java.util.Date;

public class Response {


    private String message;
    private String details;
    private Date timestamp;

    public Response(String message, String details) {
        super();
        this.message = message;
        this.details = details;
        this.timestamp = new Date();
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "{\n" +
                "\t\"message\":\"" + message + "\",\n" +
                "\t\"details\":\"" + details + "\",\n" +
                "\t\"timestamp\":\"" + timestamp + "\"\n" +
                '}';
    }
}

