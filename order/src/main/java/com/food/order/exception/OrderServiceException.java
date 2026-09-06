package com.food.order.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OrderServiceException extends RuntimeException {

    private final HttpStatus status;

    public OrderServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
