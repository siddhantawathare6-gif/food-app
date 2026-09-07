package com.food.order.service;

import com.food.order.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedUserService {

    private static final Logger log = LoggerFactory.getLogger(CachedUserService.class);

    @Autowired
    UserService userService;

    @Cacheable(value = "orderServiceUser", key = "#userId")
    public UserDTO getUserDetails(Integer userId) {
        log.info("Cache miss - calling UserService (with retry) for userId: {}", userId);
        return userService.fetchUserDetailsFromUserId(userId);
    }

}
