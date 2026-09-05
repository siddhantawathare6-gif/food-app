package com.food.userinfo.service.impl;

import com.food.userinfo.dto.JwtAuthResponse;
import com.food.userinfo.dto.LoginDTO;
import com.food.userinfo.dto.RegisterDTO;
import com.food.userinfo.entity.Role;
import com.food.userinfo.entity.User;
import com.food.userinfo.exception.UserAlreadyRegisterException;
import com.food.userinfo.exception.UserinfoApiException;
import com.food.userinfo.repository.RoleRepository;
import com.food.userinfo.repository.UserRepository;
import com.food.userinfo.service.AuthService;
import com.food.userinfo.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static com.food.userinfo.util.DataMaskingUtils.maskEmail;
import static com.food.userinfo.util.DataMaskingUtils.maskEmailOrUsername;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

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

        log.info("Processing login for user: {}", maskEmailOrUsername(login.getEmailOrUsername()));

        log.debug("Authenticating user: {}", maskEmailOrUsername(login.getEmailOrUsername()));
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getEmailOrUsername(),
                login.getPassword()));
        log.debug("Authentication successful for user: {}", maskEmailOrUsername(login.getEmailOrUsername()));

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        log.debug("Security context updated for user: {}", maskEmailOrUsername(login.getEmailOrUsername()));

        UserDetails principal = (UserDetails) authenticate.getPrincipal();
        log.debug("UserDetails loaded for: {}", principal.getUsername());

        log.debug("Generating JWT token for user: {}", principal.getUsername());
        String token = jwtUtils.generatToken(principal);
        log.debug("JWT token generated successfully for user: {}", principal.getUsername());

        //return "user successfully logged in...";

        log.debug("Fetching user entity for ID: {}", principal.getUsername());
        // Fetch the actual User entity to get the ID
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseGet(() -> userRepository.findByEmail(principal.getUsername())
                        .orElseThrow(() -> {
                            log.error("User not found after authentication: {}", principal.getUsername());
                            return new UserinfoApiException(HttpStatus.NOT_FOUND, "User not found");
                        }));
        log.debug("User found with ID: {}, Username: {}", user.getId(), user.getUsername());

        JwtAuthResponse response = new JwtAuthResponse();
        response.setAccessToken(token);
        response.setUserId(user.getId());

        log.info("Login successful for user: {} (ID: {})", maskEmailOrUsername(user.getUsername()), user.getId());

        return response;
    }

    @Override
    public String register(RegisterDTO registerDTO) {

        log.info("Processing registration for user: {}, email: {}", registerDTO.getUsername(), maskEmail(registerDTO.getEmail()));

        log.debug("Checking if email exists: {}", maskEmail(registerDTO.getEmail()));
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            log.warn("Registration failed: Email already exists: {}", maskEmail(registerDTO.getEmail()));
            throw new UserAlreadyRegisterException(HttpStatus.BAD_REQUEST, "email already exists!.");
        }

        log.debug("Checking if username exists: {}", registerDTO.getUsername());
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            log.warn("Registration failed: Username already exists: {}", registerDTO.getUsername());
            throw new UserAlreadyRegisterException(HttpStatus.BAD_REQUEST, "Username already exists!.");
        }

        log.debug("Creating user entity for: {}", registerDTO.getUsername());
        User user = new User();
        user.setName(registerDTO.getName());
        user.setEmail(registerDTO.getEmail());
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role roleUser = roleRepository.findByName("ROLE_USER").get();
        roles.add(roleUser);
        user.setRoles(roles);

        User savedUser =userRepository.save(user);

        log.info("Registration successful for user: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        return "user register successfully";
    }
}
