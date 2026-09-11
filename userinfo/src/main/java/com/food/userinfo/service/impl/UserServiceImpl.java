package com.food.userinfo.service.impl;

import com.food.userinfo.dto.UpdateUserDTO;
import com.food.userinfo.dto.UserDTO;
import com.food.userinfo.entity.Address;
import com.food.userinfo.entity.User;
import com.food.userinfo.exception.UserinfoApiException;
import com.food.userinfo.mapper.UserMapper;
import com.food.userinfo.repository.UserRepository;
import com.food.userinfo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Value("${app.image.upload-dir:uploads/users}")
    private String uploadDir;

    @Value("${app.image.base-url:/user/image}")
    private String baseImageUrl;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO addUser(UserDTO userDTO) {
        log.info("Starting addUser operation");

        // Check if username exists
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new UserinfoApiException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        // Check if email exists
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserinfoApiException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = UserMapper.INSTANCE.mapUserDTOToUser(userDTO);
        log.debug("UserDTO successfully mapped to User entity");

        User savedUser = userRepository.save(user);
        log.info("User saved successfully with userId: {}", savedUser.getId());

        UserDTO response = UserMapper.INSTANCE.mapUserToUserDTO(savedUser);
        log.info("addUser operation completed successfully");
        if (user.getImageUrl() != null) {
            response.setImageUrl(baseImageUrl + "/" + user.getId());
        } else {
            response.setImageUrl(baseImageUrl + "/" + user.getId()); // still points to /user/image/{id}, backend returns avatar.jpg
        }
        return response;
    }

    @Override
    @Cacheable(value = "user", key = "#userId")
    public UserDTO fetchUserDetailsById(Long userId) {
        log.info("Starting fetchUserDetailsById for userId: {}", userId);

        User fetchedUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for userId: {}", userId);
                    return new UserinfoApiException(HttpStatus.NOT_FOUND, "User not found with id: " + userId);
                });

        log.info("User found successfully for userId: {}", userId);
        return UserMapper.INSTANCE.mapUserToUserDTO(fetchedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "user", key = "#result.id")
    public UserDTO updateUserProfile(String username, UpdateUserDTO updateUserDTO) {
        log.info("Updating user profile for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for username: {}", username);
                    return new UserinfoApiException(HttpStatus.NOT_FOUND, "User not found with username: " + username);
                });

        if (updateUserDTO.getName() != null) {
            user.setName(updateUserDTO.getName());
        }
        // ===== Update alternate mobile number with uniqueness check =====
        if (updateUserDTO.getAlternateMobileNumber() != null) {
            String newAltMobile = updateUserDTO.getAlternateMobileNumber().trim();

            // Check if it's not empty
            if (!newAltMobile.isEmpty()) {
                // Check if this alternate mobile number is already used by another user
                Optional<User> existingUser = userRepository.findByAlternateMobileNumber(newAltMobile);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                    throw new UserinfoApiException(HttpStatus.BAD_REQUEST,
                            "Alternate mobile number is already registered to another user.");
                }
                user.setAlternateMobileNumber(newAltMobile);
            } else {
                // If empty string is sent, set to null
                user.setAlternateMobileNumber(null);
            }
        }
        // Update Address
        if (updateUserDTO.getAddress() != null) {
            Address address = user.getAddress();
            if (address == null) {
                address = new Address();
            }
            if (updateUserDTO.getAddress().getAddressLine1() != null) {
                address.setAddressLine1(updateUserDTO.getAddress().getAddressLine1());
            }
            if (updateUserDTO.getAddress().getAddressLine2() != null) {
                address.setAddressLine2(updateUserDTO.getAddress().getAddressLine2());
            }
            if (updateUserDTO.getAddress().getCity() != null) {
                address.setCity(updateUserDTO.getAddress().getCity());
            }
            if (updateUserDTO.getAddress().getState() != null) {
                address.setState(updateUserDTO.getAddress().getState());
            }
            if (updateUserDTO.getAddress().getPincode() != null) {
                address.setPincode(updateUserDTO.getAddress().getPincode());
            }
            if (updateUserDTO.getAddress().getCountry() != null) {
                address.setCountry(updateUserDTO.getAddress().getCountry());
            }
            user.setAddress(address);
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for username: {}", username);
        UserDTO response = UserMapper.INSTANCE.mapUserToUserDTO(updatedUser);
        if (user.getImageUrl() != null) {
            response.setImageUrl(baseImageUrl + "/" + user.getId());
        } else {
            response.setImageUrl(baseImageUrl + "/" + user.getId()); // still points to /user/image/{id}, backend returns
            // avatar.jpg
        }
        return response;
    }

    @Override
    public UserDTO getUserProfileByUsername(String username) {
        log.info("Fetching user profile for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for username: {}", username);
                    return new UserinfoApiException(HttpStatus.NOT_FOUND, "User not found with username: " + username);
                });

        log.info("User profile fetched: {}", user.getUsername());
        UserDTO response = UserMapper.INSTANCE.mapUserToUserDTO(user);
        if (user.getImageUrl() != null) {
            response.setImageUrl(baseImageUrl + "/" + user.getId());
        } else {
            response.setImageUrl(baseImageUrl + "/" + user.getId()); // still points to /user/image/{id}, backend returns
            // avatar.jpg
        }
        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public String uploadUserImage(Long userId, MultipartFile file) {
        log.info("Uploading profile image for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserinfoApiException(HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId));
        // Validate
        if (file == null || file.isEmpty()) {
            throw new UserinfoApiException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UserinfoApiException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        try {
            // Ensure directory exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Determine extension
            String extension = ".jpg";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            // File name pattern: <userId>.<ext>  (e.g. 12.jpg)
            String fileName = userId + extension;
            Path filePath = uploadPath.resolve(fileName);

            // Delete existing file if extension changed (e.g. .png replaced by .jpg)
            if (user.getImageUrl() != null && !user.getImageUrl().equals(fileName)) {
                Files.deleteIfExists(uploadPath.resolve(user.getImageUrl()));
            }

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Persist filename on user
            user.setImageUrl(fileName);
            userRepository.save(user);

            log.info("Profile image uploaded for userId: {}, fileName: {}", userId, fileName);
            return baseImageUrl + "/" + userId;

        } catch (IOException e) {
            log.error("Failed to store profile image for userId: {}", userId, e);
            throw new UserinfoApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded file");
        }
    }

    @Override
    public byte[] getUserImage(Long userId) {
        log.debug("Retrieving profile image for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserinfoApiException(HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId));

        try {
            Path uploadPath = Paths.get(uploadDir);

            // If user has an uploaded image → return it
            if (user.getImageUrl() != null) {
                Path filePath = uploadPath.resolve(user.getImageUrl());
                if (Files.exists(filePath)) {
                    return Files.readAllBytes(filePath);
                }
                log.warn("User {} has imageUrl={} but file is missing", userId, user.getImageUrl());
            }

            // Fallback → avatar.jpg
            Path defaultFile = uploadPath.resolve("avatar.jpg");
            if (Files.exists(defaultFile)) {
                return Files.readAllBytes(defaultFile);
            }

            log.warn("Default avatar.jpg not found in {}", uploadDir);
            return new byte[0];

        } catch (IOException e) {
            log.error("Failed to read profile image for userId: {}", userId, e);
            throw new UserinfoApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read stored file");
        }
    }
}
