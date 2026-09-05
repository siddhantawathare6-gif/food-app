package com.food.userinfo.service.impl;

import com.food.userinfo.dto.UserDTO;
import com.food.userinfo.entity.User;
import com.food.userinfo.mapper.UserMapper;
import com.food.userinfo.repository.UserRepository;
import com.food.userinfo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO addUser(UserDTO userDTO) {
        log.info("Starting addUser operation");

        User user = UserMapper.INSTANCE.mapUserDTOToUser(userDTO);
        log.debug("UserDTO successfully mapped to User entity");

        User savedUser = userRepository.save(user);
        log.info("User saved successfully with userId: {}", savedUser.getId());

        UserDTO response = UserMapper.INSTANCE.mapUserToUserDTO(savedUser);
        log.info("addUser operation completed successfully");

        return response;
    }

    @Override
    public ResponseEntity<UserDTO> fetchUserDetailsById(Long userId) {
        log.info("Starting fetchUserDetailsById for userId: {}", userId);

        Optional<User> fetchedUser = userRepository.findById(userId);
        if (fetchedUser.isPresent()) {
            log.info("User found successfully for userId: {}", userId);
            return new ResponseEntity<>(UserMapper.INSTANCE.mapUserToUserDTO(fetchedUser.get()), HttpStatus.OK);
        }
        log.warn("User not found for userId: {}", userId);
        return new ResponseEntity<>((HttpHeaders) null, HttpStatus.NOT_FOUND);
    }
}
