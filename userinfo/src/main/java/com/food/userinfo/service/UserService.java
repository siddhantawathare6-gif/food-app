package com.food.userinfo.service;

import com.food.userinfo.dto.UpdateUserDTO;
import com.food.userinfo.dto.UserDTO;

public interface UserService {

    UserDTO addUser(UserDTO userDTO);

    UserDTO fetchUserDetailsById(Long userId);

    UserDTO updateUserProfile(String username, UpdateUserDTO updateUserDTO);

    UserDTO getUserProfileByUsername(String username);
}
