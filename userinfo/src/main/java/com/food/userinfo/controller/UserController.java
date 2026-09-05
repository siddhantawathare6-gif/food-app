package com.food.userinfo.controller;

import com.food.userinfo.dto.UserDTO;
import com.food.userinfo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addUser")
    public ResponseEntity<UserDTO> addUser(@RequestBody UserDTO userDTO) {
        log.info("Received request to add new user");
        UserDTO user = userService.addUser(userDTO);
        log.info("User added successfully");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/fetchUserById/{userId}")
    public ResponseEntity<UserDTO> fetchUserDetailsById(@PathVariable Long userId) {
        log.info("Received request to fetch user details for userId: {}", userId);
        ResponseEntity<UserDTO> response = userService.fetchUserDetailsById(userId);
        log.info("Successfully fetched user details for userId: {}", userId);

        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> admin() {
        return ResponseEntity.ok("Admin only can access");
    }
}
