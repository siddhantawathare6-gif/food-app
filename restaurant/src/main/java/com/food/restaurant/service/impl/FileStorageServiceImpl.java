package com.food.restaurant.service.impl;

import com.food.restaurant.exception.RestaurantServiceException;
import com.food.restaurant.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.image.upload-dir:uploads/restaurants}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadDir);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory", e);
            throw new RestaurantServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initialize file storage");
        }
    }

    @Override
    public String storeFile(Integer entityId, MultipartFile file) {
        validateFile(file);

        String extension = extractExtension(file.getOriginalFilename());
        String fileName = entityId + extension;
        Path filePath = Paths.get(uploadDir, fileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored successfully: {}", fileName);
            return fileName;
        } catch (IOException e) {
            log.error("Failed to store file for id: {}", entityId, e);
            throw new RestaurantServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded file");
        }
    }

    @Override
    public byte[] readFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            if (!Files.exists(filePath)) {
                return new byte[0];
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to read file: {}", fileName, e);
            throw new RestaurantServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read stored file");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RestaurantServiceException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RestaurantServiceException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return ".jpg"; // sensible fallback
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
            log.info("Deleted old file: {}", fileName);
        } catch (IOException e) {
            // Don't fail the whole upload just because cleanup of the old file failed —
            // log it and move on, since the new upload can still succeed.
            log.warn("Failed to delete old file: {}", fileName, e);
        }
    }
}
