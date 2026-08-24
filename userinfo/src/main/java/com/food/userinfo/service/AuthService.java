package com.food.userinfo.service;

import com.food.userinfo.dto.LoginDTO;
import com.food.userinfo.dto.RegisterDTO;
import com.food.userinfo.dto.UserDTO;

public interface AuthService {

    String login(LoginDTO login);

    String register(RegisterDTO registerDTO);

}
