package com.blog.service;

import com.blog.model.Restaurant;
import com.blog.model.Review;
import com.blog.model.ReviewLike;
import com.blog.model.User;
import com.blog.repository.ReviewLikeRepository;
import com.blog.repository.ReviewRepository;
import com.blog.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Class Name: AnswerService
 * Package: service
 * Description:
 * author:
 * Create: 2025/1/3
 * Version: 1.0
 */
@Service
public class ReviewService {

    private final RestaurantService restaurantService;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;

    public ReviewService(RestaurantService restaurantService, ReviewRepository reviewRepository, ReviewLikeRepository reviewLikeRepository, UserRepository userRepository) {
        this.restaurantService = restaurantService;
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.userRepository = userRepository;
    }
    //新增評論
    public Review createReview(Long restaurantId, Review review){
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        review.setRestaurant(restaurant);
        Review savedReview = reviewRepository.save(review);

        restaurantService.calculateAverageRating(restaurantId);
        return savedReview;
    }
    //獲取某家餐廳的所有評論
    public List<Review> getAllReview(Long restaurantId){
        return reviewRepository.findByRestaurantId(restaurantId);
    }
    //刪除評論
    @PreAuthorize("hasRole('ADMIN') or @reviewService.isReviewOwner(#reviewId, authentication.name)")
    public void deleteReview(Long reviewId, Long userId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("找不到該評論"));
        if(!review.getUser().getId().equals(userId)){
            throw new RuntimeException("無權刪除此評論");
        }

        reviewRepository.deleteById(reviewId);

        Long restaurantId = review.getRestaurant().getId();
        restaurantService.calculateAverageRating(restaurantId);
    }
    public boolean isReviewOwner(Long reviewId, String username) {
        return reviewRepository.findById(reviewId)
                .map(review -> review.getUser().getUsername().equals(username))
                .orElse(false);
    }
    // 評論按讚
    public void likeReview(Long userId, Long reviewId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到該用戶"));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("找不到該評論"));
        if(reviewLikeRepository.existsByUserAndReview(user,review)){
            throw new RuntimeException("已經按讚過該評論");
        }
        ReviewLike like = new ReviewLike(user, review);
        reviewLikeRepository.save(like);

    }
    //按讚評論取消
    public void unlikeReview(Long userId, Long reviewId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到該用戶"));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("找不到該評論"));
        reviewLikeRepository.deleteByUserAndReview(user,review);
    }

    // 獲取評論的按讚數
    public Long getReviewLikeCount(Long reviewId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("找不到該評論"));
        return reviewLikeRepository.countByReview(review);
    }
    //上傳圖片
    public String uploadReviewImage(MultipartFile image){
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path filePath = Paths.get("uploads/" + fileName);
        try {
            Files.write(filePath, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("圖片上傳失敗", e);
        }
        return "/uploads/" + fileName;
    }

}
