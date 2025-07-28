package com.project.aptflow.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OrderCreationException extends RuntimeException{
    public OrderCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
