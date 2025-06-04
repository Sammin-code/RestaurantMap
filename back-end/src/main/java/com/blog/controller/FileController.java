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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/images")
public class FileController {
  private static final Logger logger = LoggerFactory.getLogger(FileController.class);

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
    logger.info("=== FileController.getImage called with fileName: {} ===", fileName);
    try {
      logger.info("Attempting to get image: {}", fileName);
      logger.info("Project ID: {}, Bucket: {}", projectId, bucketName);

      Storage storage = StorageOptions.newBuilder()
          .setProjectId(projectId)
          .build()
          .getService();

      BlobId blobId = BlobId.of(bucketName, fileName);
      logger.info("Looking for blob: {}", blobId);

      byte[] content = storage.readAllBytes(blobId);
      logger.info("Successfully read image, size: {} bytes", content.length);

      // 根據檔案名稱判斷圖片類型
      MediaType mediaType = MediaType.IMAGE_JPEG;
      if (fileName.toLowerCase().endsWith(".png")) {
        mediaType = MediaType.IMAGE_PNG;
      } else if (fileName.toLowerCase().endsWith(".gif")) {
        mediaType = MediaType.IMAGE_GIF;
      }

      return ResponseEntity.ok()
          .contentType(mediaType)
          .body(content);
    } catch (Exception e) {
      logger.error("Error getting image: {}", fileName, e);
      return ResponseEntity.notFound().build();
    }
  }
}