package com.food.userinfo.service;

import com.food.userinfo.dto.UserDTO;
import org.springframework.http.ResponseEntity;

public interface UserService {
    UserDTO addUser(UserDTO userDTO);

    ResponseEntity<UserDTO> fetchUserDetailsById(Long userId);
}
