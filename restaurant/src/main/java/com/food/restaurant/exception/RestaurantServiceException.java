package com.food.restaurant.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RestaurantServiceException extends RuntimeException {

    private final HttpStatus status;

    public RestaurantServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
