package com.food.restaurant.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RestaurantNotFoundException extends RuntimeException {

    private final HttpStatus status;

    public RestaurantNotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }
}
