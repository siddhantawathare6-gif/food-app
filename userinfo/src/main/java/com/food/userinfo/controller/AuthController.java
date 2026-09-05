package com.food.userinfo.controller;

import com.food.userinfo.dto.JwtAuthResponse;
import com.food.userinfo.dto.LoginDTO;
import com.food.userinfo.dto.RegisterDTO;
import com.food.userinfo.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.food.userinfo.util.DataMaskingUtils.*;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin (now that the gateway handles it centrally)
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = {"/login", "/signin"})
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDTO login) {
        log.info("Login attempt for user: {}", maskEmailOrUsername(login.getEmailOrUsername()));

        // Log full login request at DEBUG level (without password)
        log.debug("Login request - emailOrUsername: {}, password: [PROTECTED]",
                login.getEmailOrUsername());

        // Authenticate user
        JwtAuthResponse response = authService.login(login);

        // Log success (mask sensitive data)
        log.info("Login successful for user: {}", maskEmailOrUsername(login.getEmailOrUsername()));

        // Log token details at DEBUG level (only first few chars)
        if (response != null && response.getAccessToken() != null) {
            String tokenPreview = response.getAccessToken().substring(0, Math.min(20, response.getAccessToken().length())) + "...";
            log.debug("Token generated: {}", tokenPreview);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = {"/register", "/signup"})
    public ResponseEntity<String> login(@RequestBody RegisterDTO register) {

        // Log registration attempt
        log.info("Registration attempt for user: {}, email: {}", register.getUsername(), maskEmail(register.getEmail()));

        // Log full registration request at DEBUG level (without password)
        log.debug("Registration request - username: {}, email: {}, role: [PROTECTED]", register.getUsername(), register.getEmail());

        if (register.getUsername() == null || register.getUsername().trim().isEmpty()) {
            log.warn("Registration validation failed: username is null or empty");
            throw new IllegalArgumentException("Username is required");
        }

        if (register.getEmail() == null || register.getEmail().trim().isEmpty()) {
            log.warn("Registration validation failed: email is null or empty for user: {}", register.getUsername());
            throw new IllegalArgumentException("Email is required");
        }

        if (register.getPassword() == null || register.getPassword().trim().isEmpty()) {
            log.warn("Registration validation failed: password is null or empty for user: {}",
                    register.getUsername());
            throw new IllegalArgumentException("Password is required");
        }

        // Validate email format (basic check)
        if (!isValidEmail(register.getEmail())) {
            log.warn("Registration validation failed: invalid email format for user: {}",
                    register.getUsername());
            throw new IllegalArgumentException("Invalid email format");
        }

        String response = authService.register(register);

        log.info("Registration successful for user: {}, email: {}", register.getUsername(),
                maskEmail(register.getEmail()));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
