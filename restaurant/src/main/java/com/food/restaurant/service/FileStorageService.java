package com.food.restaurant.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(Integer entityId, MultipartFile file);

    byte[] readFile(String fileName);

    void deleteFile(String fileName);
}
