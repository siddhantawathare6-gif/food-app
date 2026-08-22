package com.food.resturant;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(String msg) {
        super(msg);
    }

}
