package com.blog.service;

import com.blog.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImageService {

    @Autowired
    private CloudStorageService cloudStorageService;

    public String uploadImage(MultipartFile file) throws IOException {
        return cloudStorageService.uploadImage(file);
    }

    public void deleteImage(String imageUrl) {
        cloudStorageService.deleteImage(imageUrl);
    }
}
