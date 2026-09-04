package com.food.userinfo.service;

import com.food.userinfo.dto.JwtAuthResponse;
import com.food.userinfo.dto.LoginDTO;
import com.food.userinfo.dto.RegisterDTO;
import com.food.userinfo.dto.UserDTO;

public interface AuthService {

    JwtAuthResponse login(LoginDTO login);

    String register(RegisterDTO registerDTO);

}
