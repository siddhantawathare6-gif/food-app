package com.food.userinfo.controller;

import com.food.userinfo.dto.UpdateUserDTO;
import com.food.userinfo.dto.UserDTO;
import com.food.userinfo.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        UserDTO response = userService.fetchUserDetailsById(userId);
        log.info("Successfully fetched user details for userId: {}", userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getCurrentUserProfile(Authentication authentication) {
        log.info("Received request to fetch current user profile");
        String username = authentication.getName();
        log.info("Fetching profile for username: {}", username);

        UserDTO response = userService.getUserProfileByUsername(username);
        log.info("Successfully fetched profile for user: {}", username);
        return ResponseEntity.ok(response);
    }

    // ===== NEW: Update current user profile =====
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserDTO updateUserDTO,
            Authentication authentication) {
        log.info("Received request to update current user profile");
        String username = authentication.getName();
        log.info("Updating profile for username: {}", username);

        UserDTO response = userService.updateUserProfile(username, updateUserDTO);
        log.info("Successfully updated profile for user: {}", username);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> admin() {
        return ResponseEntity.ok("Admin only can access");
    }

    @PostMapping("/uploadImage/{id}")
    public ResponseEntity<String> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) {
        log.info("POST /user/uploadImage/{} - Uploading profile image", id);
        String imageUrl = userService.uploadUserImage(id, file);
        return ResponseEntity.ok(imageUrl);
    }

    @GetMapping(value = "/image/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        log.debug("GET /user/image/{} - Retrieving profile image", id);
        byte[] imageData = userService.getUserImage(id);
        if (imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(imageData);
    }
}
