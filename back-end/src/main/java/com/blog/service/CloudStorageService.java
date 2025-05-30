package com.blog.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.blog.exception.ValidationException;

import java.io.IOException;
import java.util.UUID;

@Service
public class CloudStorageService {

  @Value("${GOOGLE_CLOUD_PROJECT}")
  private String projectId;

  @Value("${BUCKET_NAME}")
  private String bucketName;

  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
  private static final String[] ALLOWED_CONTENT_TYPES = {
      "image/jpeg",
      "image/png",
      "image/gif"
  };

  public String uploadImage(MultipartFile file) throws IOException {
    // 驗證文件
    validateFile(file);

    try {
      // 初始化 Storage 服務
      Storage storage = StorageOptions.newBuilder()
          .setProjectId(projectId)
          .build()
          .getService();

      // 生成唯一的文件名
      String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

      // 創建 BlobId 和 BlobInfo
      BlobId blobId = BlobId.of(bucketName, fileName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
          .setContentType(file.getContentType())
          .build();

      // 上傳文件到 Cloud Storage
      storage.create(blobInfo, file.getBytes());

      // 返回公開訪問的 URL
      return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    } catch (Exception e) {
      throw new ValidationException("圖片上傳失敗：" + e.getMessage());
    }
  }

  public void deleteImage(String imageUrl) {
    try {
      // 從 URL 中提取文件名
      String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

      // 初始化 Storage 服務
      Storage storage = StorageOptions.newBuilder()
          .setProjectId(projectId)
          .build()
          .getService();

      // 刪除文件
      BlobId blobId = BlobId.of(bucketName, fileName);
      storage.delete(blobId);
    } catch (Exception e) {
      throw new ValidationException("圖片刪除失敗：" + e.getMessage());
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ValidationException("請選擇要上傳的圖片");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new ValidationException("圖片大小不能超過5MB");
    }

    String contentType = file.getContentType();
    boolean isAllowedType = false;
    for (String allowedType : ALLOWED_CONTENT_TYPES) {
      if (allowedType.equals(contentType)) {
        isAllowedType = true;
        break;
      }
    }

    if (!isAllowedType) {
      throw new ValidationException("只允許上傳 JPG、PNG 或 GIF 格式的圖片");
    }
  }
}