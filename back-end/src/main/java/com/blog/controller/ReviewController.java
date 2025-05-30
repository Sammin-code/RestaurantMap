package com.blog.controller;

import com.blog.dto.ReviewDTO;
import com.blog.model.Review;
import com.blog.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.blog.repository.ReviewRepository;
import com.blog.service.ReviewService;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.blog.model.User;
import com.blog.repository.UserRepository;
import org.hibernate.Hibernate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class Name: AnswerController
 * Package: controller
 * Description:
 * author:
 * Create: 2025/1/3
 * Version: 1.0
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantService restaurantService;
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    public ReviewController(ReviewService reviewService, ReviewRepository reviewRepository,
                            UserRepository userRepository, RestaurantService restaurantService) {
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.restaurantService = restaurantService;
    }

    // 新增評論
    @PostMapping(value = "/{restaurantId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('REVIEWER')")
    public ResponseEntity<?> createReview(
            @PathVariable Long restaurantId,
            @RequestPart("review") String reviewJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication) {
        try {
            // 打印接收到的 JSON 字符串以便調試
            System.out.println("Received review JSON: " + reviewJson);

            // 創建 ObjectMapper 並配置忽略未知屬性
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            // 解析 JSON 字符串為 Review 對象
            Review review = objectMapper.readValue(reviewJson, Review.class);

            // 打印解析後的 Review 對象
            System.out.println("Parsed review object: " + review);

            // 從認證信息中獲取當前用戶
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUserName = userDetails.getUsername();

            // 創建評論
            Review createdReview = reviewService.createReview(restaurantId, review, image, currentUserName);
            return ResponseEntity.ok(createdReview);
        } catch (JsonProcessingException e) {
            System.err.println("JSON parsing error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid review data format: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Runtime error: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("An error occurred while creating the review: " + e.getMessage());
        }
    }
    //獲取所有評論
    @GetMapping("/restaurant/{restaurantId}/page")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    public ResponseEntity<?> getAllReview(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {

        try {
            // 檢查餐廳是否存在
            if (!restaurantService.existsById(restaurantId)) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = reviewService.getRestaurantReviewsWithPagination(
                    restaurantId, page, size, sort);

            // 確保返回的數據不為空
            if (response == null) {
                response = new HashMap<>();
                response.put("content", new ArrayList<>());
                response.put("totalElements", 0);
                response.put("currentPage", page);
                response.put("size", size);
                response.put("starDistribution", Map.of(5, 0, 4, 0, 3, 0, 2, 0, 1, 0));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getAllReview: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("An error occurred while fetching reviews: " + e.getMessage());
        }
    }

    // 刪除評論
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUserName = userDetails.getUsername();
        reviewService.deleteReview(reviewId, currentUserName);
        return ResponseEntity.noContent().build();
    }

    // 按讚評論
    @PostMapping("/{reviewId}/like")
    @PreAuthorize("hasAnyRole('REVIEWER')")
    public ResponseEntity<Void> likeReview(@PathVariable Long reviewId, Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("User {} attempting to like review {}", username, reviewId);

            // 檢查評論是否存在
            if (!reviewRepository.existsById(reviewId)) {
                logger.error("Review {} does not exist", reviewId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("找不到該用戶"));

            reviewService.likeReview(user.getId(), reviewId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Error liking review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // 取消按讚
    @DeleteMapping("/{reviewId}/like")
    @PreAuthorize("hasAnyRole('REVIEWER')")
    public ResponseEntity<Void> unlikeReview(@PathVariable Long reviewId, Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("User {} attempting to unlike review {}", username, reviewId);

            // 檢查評論是否存在
            if (!reviewRepository.existsById(reviewId)) {
                logger.error("Review {} does not exist", reviewId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("找不到該用戶"));

            reviewService.unlikeReview(user.getId(), reviewId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Error unliking review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 獲取評論的按讚數
    @GetMapping("/{reviewId}/like-count")
    public ResponseEntity<Long> getReviewLikeCount(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewLikeCount(reviewId));
    }

    // 上傳圖片
    @PostMapping("/{restaurantId}/upload")
    public ResponseEntity<String> uploadReviewImage(@PathVariable Long restaurantId,
            @RequestParam("image") MultipartFile image) {
        String imageUrl = reviewService.uploadReviewImage(image);
        return ResponseEntity.ok(imageUrl);
    }


    // 更新評論
    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('REVIEWER')")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestPart("review") String reviewJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication) {
        try {
            // 打印接收到的 JSON 字符串以便調試
            System.out.println("Received review JSON: " + reviewJson);

            // 創建 ObjectMapper 並配置忽略未知屬性
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // 解析 JSON 字符串為 Review 對象
            Review review = objectMapper.readValue(reviewJson, Review.class);

            // 打印解析後的 Review 對象
            System.out.println("Parsed review object: " + review);

            // 更新評論
            Review updatedReview = reviewService.updateReview(reviewId, review, image, authentication);
            return ResponseEntity.ok(updatedReview);
        } catch (JsonProcessingException e) {
            System.err.println("JSON parsing error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid review data format: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Runtime error: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("An error occurred while updating the review: " + e.getMessage());
        }
    }

}
