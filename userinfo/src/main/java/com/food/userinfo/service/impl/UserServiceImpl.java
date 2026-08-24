package com.food.userinfo.service.impl;

import com.food.userinfo.dto.UserDTO;
import com.food.userinfo.entity.User;
import com.food.userinfo.mapper.UserMapper;
import com.food.userinfo.repository.UserRepository;
import com.food.userinfo.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO addUser(UserDTO userDTO) {
        User user = UserMapper.INSTANCE.mapUserDTOToUser(userDTO);
        User savedUser = userRepository.save(user);
        return UserMapper.INSTANCE.mapUserToUserDTO(savedUser);
    }

    @Override
    public ResponseEntity<UserDTO> fetchUserDetailsById(Long userId) {
        Optional<User> fetchedUser =  userRepository.findById(userId);
        if(fetchedUser.isPresent())
            return new ResponseEntity<>(UserMapper.INSTANCE.mapUserToUserDTO(fetchedUser.get()), HttpStatus.OK);
        return new ResponseEntity<>((HttpHeaders) null, HttpStatus.NOT_FOUND);
    }
}
