package com.food.foodcatalogue.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FoodCatalogueServiceException extends RuntimeException {

    private final HttpStatus status;

    public FoodCatalogueServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
