package com.blog.controller;

import com.blog.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class FileController {

  @Autowired
  private ImageService imageService;

  @PostMapping("/upload")
  public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
    String imageUrl = imageService.uploadImage(file);
    return ResponseEntity.ok(imageUrl);
  }

  @DeleteMapping("/{imageUrl}")
  public ResponseEntity<Void> deleteImage(@PathVariable String imageUrl) {
    imageService.deleteImage(imageUrl);
    return ResponseEntity.ok().build();
  }
}