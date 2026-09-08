package com.food.restaurant.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleRestaurantNotFoundException(RestaurantNotFoundException exception,
                                                                          WebRequest webRequest) {
        log.warn("RestaurantNotFoundException: {}", exception.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(),
                webRequest.getDescription(false));
        return new ResponseEntity<>(errorDetails, exception.getStatus());
    }

    //MethodArgumentTypeMismatchException — wrong type in a path/query param
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleTypeMismatchException(MethodArgumentTypeMismatchException exception,
                                                                    WebRequest webRequest) {
        String message = "Invalid value for parameter '" + exception.getName() + "': " + exception.getValue();
        log.warn(message);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), message, webRequest.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    //HttpMessageNotReadableException — malformed/missing JSON body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleMessageNotReadableException(HttpMessageNotReadableException exception,
                                                                          WebRequest webRequest) {
        log.warn("Malformed request body: {}", exception.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), "Request body is missing or malformed.",
                webRequest.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    //NoResourceFoundException (Spring Boot 3.2+) — hitting a URL that doesn't exist
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDetails> handleNoResourceFoundException(NoResourceFoundException exception,
                                                                       WebRequest webRequest) {
        log.warn("No handler found: {}", exception.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), "The requested resource was not found.",
                webRequest.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RestaurantServiceException.class)
    public ResponseEntity<ErrorDetails> handleRestaurantServiceException(RestaurantServiceException exception,
                                                                         WebRequest webRequest) {
        log.warn("RestaurantServiceException: {}", exception.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(),
                webRequest.getDescription(false));
        return new ResponseEntity<>(errorDetails, exception.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception,
                                                              WebRequest webRequest) {
        log.error("Unhandled exception occurred", exception);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), "An unexpected error occurred. Please try again later.",
                webRequest.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
