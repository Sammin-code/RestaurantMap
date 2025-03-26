package com.blog.controller;

import com.blog.model.Review;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.blog.repository.ReviewRepository;
import com.blog.service.ReviewService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    public ReviewController(ReviewService reviewService, ReviewRepository reviewRepository) {
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
    }

    //新增評論
    @PostMapping("/{restaurantId}")
    public ResponseEntity<Review> createReview(@PathVariable Long restaurantId,@RequestBody Review review){
        return ResponseEntity.ok(reviewService.createReview(restaurantId, review));
    }
    //獲取某家餐廳的所有評論
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Review>> getAllReview(@PathVariable Long restaurantId){
        return ResponseEntity.ok(reviewService.getAllReview(restaurantId));
    }
    //刪除評論
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, @RequestParam Long userId) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
    // 按讚評論
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<Void> likeReview(@PathVariable Long userId, @RequestParam Long reviewId){
        reviewService.likeReview(userId, reviewId);
        return ResponseEntity.ok().build();
    }

    // 取消按讚
    @DeleteMapping("/{reviewId}/like")
    public ResponseEntity<Void> unlikeReview(@PathVariable Long userId, @RequestParam Long reviewId){
        reviewService.unlikeReview(userId, reviewId);
        return ResponseEntity.ok().build();
    }
    // 獲取評論的按讚數
    @GetMapping("/{reviewId}/like-count")
    public ResponseEntity<Long>getReviewLikeCount(@PathVariable Long reviewId){
        return ResponseEntity.ok(reviewService.getReviewLikeCount(reviewId));
    }
    //上傳圖片
    @PostMapping("/{restaurantId}/upload")
    public ResponseEntity<String> uploadReviewImage(@PathVariable Long restaurantId,
                                                    @RequestParam("image") MultipartFile image) {
        String imageUrl = reviewService.uploadReviewImage(image);
        return ResponseEntity.ok(imageUrl);
    }



}
