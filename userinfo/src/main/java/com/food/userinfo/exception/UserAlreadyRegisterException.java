package com.food.userinfo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserAlreadyRegisterException extends RuntimeException {

    private HttpStatus status;
    private String message;

    public UserAlreadyRegisterException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public UserAlreadyRegisterException(String message, HttpStatus status, String message1) {
        super(message);
        this.status = status;
        this.message = message1;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
