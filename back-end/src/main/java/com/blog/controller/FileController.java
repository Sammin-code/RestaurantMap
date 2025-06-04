package com.blog.controller;

import com.blog.service.ImageService;
import com.blog.service.CloudStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/images")
public class FileController {

  @Autowired
  private ImageService imageService;

  @Autowired
  private CloudStorageService cloudStorageService;

  @Value("${GOOGLE_CLOUD_PROJECT}")
  private String projectId;

  @Value("${BUCKET_NAME}")
  private String bucketName;

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

  @GetMapping("/{fileName}")
  public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
    try {
      Storage storage = StorageOptions.newBuilder()
          .setProjectId(projectId)
          .build()
          .getService();

      BlobId blobId = BlobId.of(bucketName, fileName);
      byte[] content = storage.readAllBytes(blobId);

      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG) // 或根據實際圖片類型設定
          .body(content);
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }
}