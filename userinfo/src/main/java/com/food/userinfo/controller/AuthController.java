package com.food.userinfo.controller;

import com.food.userinfo.dto.JwtAuthResponse;
import com.food.userinfo.dto.LoginDTO;
import com.food.userinfo.dto.RegisterDTO;
import com.food.userinfo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin (now that the gateway handles it centrally)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = {"/login", "/signin"})
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDTO login) {
        return ResponseEntity.ok(authService.login(login));
    }

    @PostMapping(value = {"/register", "/signup"})
    public ResponseEntity<String> login(@RequestBody RegisterDTO register) {
        String response = authService.register(register);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
