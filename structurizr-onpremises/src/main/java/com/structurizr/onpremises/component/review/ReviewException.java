package com.structurizr.onpremises.component.review;

public class ReviewException extends RuntimeException {

    ReviewException(String message) {
        super(message);
    }

    ReviewException(String message, Throwable cause) {
        super(message, cause);
    }

}