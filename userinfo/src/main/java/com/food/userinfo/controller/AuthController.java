package com.food.userinfo.controller;

import com.food.userinfo.dto.JwtAuthResponse;
import com.food.userinfo.dto.LoginDTO;
import com.food.userinfo.dto.RegisterDTO;
import com.food.userinfo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = {"/login", "/signin"})
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDTO login) {
        String response = authService.login(login);
        JwtAuthResponse jwtAuthResponse=new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(response);
        return ResponseEntity.ok(jwtAuthResponse);
    }

    @PostMapping(value = {"/register", "/signup"})
    public ResponseEntity<String> login(@RequestBody RegisterDTO register) {
        String response = authService.register(register);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
