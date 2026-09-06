package com.food.order.service;

import com.food.order.dto.UserDTO;
import com.food.order.exception.OrderServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    RestTemplate restTemplate;

    @Retry(name = "userServiceRetry", fallbackMethod = "fetchUserDetailsFallback")
    public UserDTO fetchUserDetailsFromUserId(Integer userId) {
        log.debug("Calling User Service for userId: {}", userId);
        UserDTO userDTO = restTemplate.getForObject("http://USER-SERVICE/user/fetchUserById/" + userId, UserDTO.class);

        if (userDTO != null) {
            log.debug("User Service response - userId: {}, username: {}", userDTO.getId(), userDTO.getName());
        } else {
            log.warn("User Service returned null for userId: {}", userId);
        }
        return userDTO;
    }

    private UserDTO fetchUserDetailsFallback(Integer userId, Exception ex) {
        log.error("All retries exhausted fetching user {}: {}", userId, ex.getMessage());
        throw new OrderServiceException(HttpStatus.SERVICE_UNAVAILABLE, "User service is currently unavailable. Please" +
                " try again shortly.");
    }
}
