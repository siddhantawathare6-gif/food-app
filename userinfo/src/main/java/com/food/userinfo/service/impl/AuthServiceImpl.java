package com.food.userinfo.service.impl;

import com.food.userinfo.dto.JwtAuthResponse;
import com.food.userinfo.dto.LoginDTO;
import com.food.userinfo.dto.RegisterDTO;
import com.food.userinfo.dto.UserDTO;
import com.food.userinfo.entity.Role;
import com.food.userinfo.entity.User;
import com.food.userinfo.exception.UserAlreadyRegisterException;
import com.food.userinfo.exception.UserinfoApiException;
import com.food.userinfo.repository.RoleRepository;
import com.food.userinfo.repository.UserRepository;
import com.food.userinfo.service.AuthService;
import com.food.userinfo.util.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public JwtAuthResponse login(LoginDTO login) {

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getEmailOrUsername(),
                login.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        UserDetails principal = (UserDetails) authenticate.getPrincipal();
        String token = jwtUtils.generatToken(principal);
        //return "user successfully logged in...";

        // Fetch the actual User entity to get the ID
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseGet(() -> userRepository.findByEmail(principal.getUsername())
                        .orElseThrow(() -> new UserinfoApiException(HttpStatus.NOT_FOUND, "User not found")));

        JwtAuthResponse response = new JwtAuthResponse();
        response.setAccessToken(token);
        response.setUserId(user.getId());
        return response;
    }

    @Override
    public String register(RegisterDTO registerDTO) {

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new UserAlreadyRegisterException(HttpStatus.BAD_REQUEST, "email already exists!.");
        }

        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new UserAlreadyRegisterException(HttpStatus.BAD_REQUEST, "Username already exists!.");
        }

        User user = new User();
        user.setName(registerDTO.getName());
        user.setEmail(registerDTO.getEmail());
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role roleUser = roleRepository.findByName("ROLE_USER").get();
        roles.add(roleUser);
        user.setRoles(roles);

        userRepository.save(user);

        return "user register successfully";
    }
}
