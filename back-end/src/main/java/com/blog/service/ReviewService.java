package com.blog.service;

import com.blog.dto.ReviewDTO;
import com.blog.exception.ResourceNotFoundException;
import com.blog.exception.UnauthorizedException;
import com.blog.exception.ValidationException;
import com.blog.model.*;
import com.blog.repository.ReviewLikeRepository;
import com.blog.repository.ReviewRepository;
import com.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ImageService imageService;
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    public ReviewService(RestaurantService restaurantService, ReviewRepository reviewRepository,
            ReviewLikeRepository reviewLikeRepository, UserRepository userRepository,
            ImageService imageService) {
        this.restaurantService = restaurantService;
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    // 新增評論
    public Review createReview(Long restaurantId, Review review, MultipartFile image, String currentUserName) {
        // 獲取當前用戶
        User user = userRepository.findByUsername(currentUserName)
                .orElseThrow(() -> new RuntimeException("找不到當前用戶"));
        Long currentUserId = user.getId(); // 獲取用戶ID

        // 獲取餐廳
        Restaurant restaurant = restaurantService.getRestaurantEntityById(restaurantId); // 使用新方法
        review.setRestaurant(restaurant);

        // 設置用戶
        review.setUser(user);

        // 處理圖片上傳
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = imageService.uploadImage(image);
                review.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("圖片上傳失敗", e);
            }
        }

        // 設置創建時間
        review.setCreated_At(LocalDateTime.now());

        // 保存評論
        Review savedReview = reviewRepository.save(review);
        updateRestaurantRating(restaurantId);
        return savedReview;
    }

    // 獲取餐廳評論（帶分頁、排序和統計）
    public Map<String, Object> getRestaurantReviewsWithPagination(
            Long restaurantId,
            int page,
            int size,
            String sort) {

        // 獲取所有評論
        List<ReviewDTO> allReviews = getAllReview(restaurantId);

        // 根據排序參數排序
        if ("likes".equals(sort)) {
            allReviews = sortReviewsByLikes(allReviews);
        }

        // 計算總評論數
        int totalElements = allReviews.size();

        // 應用分頁
        List<ReviewDTO> pagedReviews = applyPagination(allReviews, page, size);

        // 計算星等分布
        Map<Integer, Long> starDistribution = calculateStarDistribution(allReviews);

        // 計算平均評分
        double averageRating = calculateAverageRating(allReviews);

        // 構建響應
        return buildPaginationResponse(
                pagedReviews,
                totalElements,
                page,
                size,
                starDistribution,
                averageRating);
    }

    public List<ReviewDTO> getAllReview(Long restaurantId) {
        // 從數據庫獲取評論並轉換為 DTO
        return reviewRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 將 Review 實體轉換為 ReviewDTO
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();

        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setCreated_At(review.getCreated_At());
        dto.setUpdated_At(review.getUpdated_At());
        dto.setImageUrl(review.getImageUrl());

        if (review.getUser() != null) {
            dto.setUserId(review.getUserId()); // 使用實體中的 getUserId 方法
            dto.setUsername(review.getUser().getUsername());
            dto.setUserRole(review.getUser().getRole());
        }

        if (review.getRestaurant() != null) {
            dto.setRestaurantId(review.getRestaurant().getId());
            dto.setRestaurantName(review.getRestaurant().getName());
        }

        dto.setLikeCount(review.getLikes().size()); // 直接使用 likes 集合的大小
        dto.setIsLiked(false); // 默認值，需要根據當前用戶設置

        dto.setIsEdited(review.getCreated_At() != null &&
                review.getUpdated_At() != null &&
                review.getCreated_At().isBefore(review.getUpdated_At()));

        return dto;
    }

    // 根據點讚數排序
    private List<ReviewDTO> sortReviewsByLikes(List<ReviewDTO> reviews) {
        return reviews.stream()
                .sorted((r1, r2) -> r2.getLikeCount().compareTo(r1.getLikeCount())) // 降序排序
                .collect(Collectors.toList());
    }

    // 構建分頁響應
    private Map<String, Object> buildPaginationResponse(
            List<ReviewDTO> content,
            int totalElements,
            int page,
            int size,
            Map<Integer, Long> starDistribution,
            double averageRating) {

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalElements", totalElements);
        response.put("totalPages", (int) Math.ceil((double) totalElements / size));
        response.put("currentPage", page);
        response.put("size", size);
        response.put("starDistribution", starDistribution);
        response.put("averageRating", averageRating);

        return response;
    }

    // 應用分頁
    private List<ReviewDTO> applyPagination(List<ReviewDTO> reviews, int page, int size) {
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, reviews.size());
        return (fromIndex < reviews.size()) ? reviews.subList(fromIndex, toIndex) : List.of();
    }

    // 計算星等分布
    private Map<Integer, Long> calculateStarDistribution(List<ReviewDTO> reviews) {
        Map<Integer, Long> distribution = new HashMap<>();

        for (ReviewDTO review : reviews) {
            int rating = review.getRating();
            distribution.put(rating, distribution.getOrDefault(rating, 0L) + 1);
        }

        for (int i = 1; i <= 5; i++) {
            if (!distribution.containsKey(i)) {
                distribution.put(i, 0L);
            }
        }
        return distribution;
    }

    // 計算平均評分
    private double calculateAverageRating(List<ReviewDTO> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        // 計算總分
        int totalRating = 0;
        for (ReviewDTO review : reviews) {
            totalRating += review.getRating();
        }

        // 計算平均分
        return (double) totalRating / reviews.size();
    }

    // 刪除評論
    @PreAuthorize("hasRole('ADMIN') or @reviewService.isReviewOwner(#reviewId, authentication.name)")
    public void deleteReview(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該評論"));

        // 檢查用戶是否是評論作者或管理員
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到該用戶"));

        if (!review.getUser().getUsername().equals(username) && currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("無權刪除此評論");
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
    public void likeReview(Long userId, Long reviewId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到該用戶"));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("找不到該評論"));
        if (reviewLikeRepository.existsByUserAndReview(user, review)) {
            throw new ValidationException("已經按讚過該評論");
        }
        ReviewLike like = new ReviewLike(user, review);
        reviewLikeRepository.save(like);
    }

    // 按讚評論取消
    public void unlikeReview(Long userId, Long reviewId) {
        logger.info("Attempting to unlike review {} by user {}", reviewId, userId);

        try {
            // 先檢查評論是否存在
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new ResourceNotFoundException("找不到該評論"));

            // 再檢查用戶是否存在
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("找不到該用戶"));

            // 檢查是否已經點讚
            if (!reviewLikeRepository.existsByUserAndReview(user, review)) {
                logger.warn("User {} has not liked review {}", userId, reviewId);
                throw new ValidationException("尚未按讚該評論");
            }

            // 查找具體的點讚記錄
            ReviewLike like = reviewLikeRepository.findByUserAndReview(user, review)
                    .orElseThrow(() -> new ResourceNotFoundException("找不到點讚記錄"));

            logger.info("Deleting like for review {} by user {}", reviewId, userId);
            reviewLikeRepository.delete(like);

            logger.info("Successfully unliked review {} by user {}", reviewId, userId);
        } catch (Exception e) {
            logger.error("Error unliking review: {}", e.getMessage());
            throw new ValidationException("取消按讚失敗：" + e.getMessage());
        }
    }

    // 獲取評論的按讚數
    public Long getReviewLikeCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("找不到該評論"));
        return reviewLikeRepository.countByReview(review);
    }

    // 更新評論
    public Review updateReview(Long reviewId, Review review, MultipartFile image, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("找不到該評論"));

        // 檢查權限
        if (!existingReview.getUser().getId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            throw new RuntimeException("沒有權限更新該評論");
        }

        // 更新評論內容
        existingReview.setContent(review.getContent());
        existingReview.setRating(review.getRating());

        // 處理圖片上傳
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = imageService.uploadImage(image);
                existingReview.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("圖片上傳失敗", e);
            }
        } else if (review.getImageUrl() != null) {
            existingReview.setImageUrl(review.getImageUrl());
        }

        Review updatedReview = reviewRepository.save(existingReview);
        updateRestaurantRating(existingReview.getRestaurant().getId());
        return updatedReview;
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到ID為 " + id + " 的評論"));
    }

    public Review createReview(Review review) {
        if (review.getRestaurant() == null) {
            throw new ValidationException("評論必須關聯到一個餐廳");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new ValidationException("評分必須在1到5之間");
        }
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            throw new ValidationException("評論內容不能為空");
        }

        Review savedReview = reviewRepository.save(review);
        updateRestaurantRating(review.getRestaurant().getId());
        return savedReview;
    }

    private void updateRestaurantRating(Long restaurantId) {
        restaurantService.calculateAverageRating(restaurantId);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            return user != null ? user.getId() : null;
        }
        return null;
    }

}
