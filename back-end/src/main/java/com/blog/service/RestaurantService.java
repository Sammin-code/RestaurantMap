package com.blog.service;

import com.blog.dto.RestaurantResponse;
import com.blog.exception.ResourceNotFoundException;
import com.blog.exception.ValidationException;
import com.blog.model.*;
import com.blog.repository.RestaurantRepository;
import com.blog.repository.UserRepository;
import com.blog.repository.UserRestaurantRepository;
import com.blog.repository.ReviewLikeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.blog.dto.ReviewDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.blog.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class Name: QuestionService
 * Package: service
 * Description:
 * author:
 * Create: 2025/1/3
 * Version: 1.0
 */
@Service
public class RestaurantService {
    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    @Autowired
    private final RestaurantRepository restaurantRepository;

    private final UserRestaurantRepository userRestaurantRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ReviewLikeRepository reviewLikeRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
            UserRestaurantRepository userRestaurantRepository,
            UserRepository userRepository, ImageService imageService,
            ReviewLikeRepository reviewLikeRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRestaurantRepository = userRestaurantRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.reviewLikeRepository = reviewLikeRepository;
    }

    // 新增餐廳
    @Transactional
    public Restaurant createRestaurant(RestaurantResponse dto, String currentUserName, MultipartFile image) {
        User user = userRepository.findByUsername(currentUserName)
                .orElseThrow(() -> new RuntimeException("用戶未找到"));

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = imageService.uploadImage(image);
                System.out.println("Service imageService.uploadImage(image): " + imageUrl);
            } catch (IOException e) {
                System.out.println("Service imageService.uploadImage(image) 發生錯誤: " + e.getMessage());
                throw new RuntimeException("圖片上傳失敗", e);
            }
        } else {
            System.out.println("Service imageService.uploadImage(image): no image");
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        restaurant.setCategory(dto.getCategory());
        restaurant.setDescription(dto.getDescription());
        restaurant.setCreatedByUsername(currentUserName);
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setImageUrl(imageUrl);

        System.out.println("Service restaurant.getImageUrl(): " + restaurant.getImageUrl());
        System.out.println("Service image != null: " + (image != null));
        System.out.println("Service image.isEmpty(): " + (image != null ? image.isEmpty() : "image is null"));

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return savedRestaurant;
    }

    // 取得所有餐廳
    public Page<Restaurant> getAllRestaurants(
            int page,
            int size,
            String sort,
            String keyword,
            String category,
            Double minRating) {

        // 創建分頁和排序
        Sort sortObj;
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length > 1) {
                sortObj = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            } else {
                sortObj = Sort.by(sortParams[0]);
            }
        } else {
            sortObj = Sort.by("id").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        // 先獲取基本篩選結果
        Page<Restaurant> restaurants = restaurantRepository.findAllWithFilters(
                keyword, category, pageable);

        // 如果有最低評分要求，在內存中進行篩選
        if (minRating != null) {
            List<Restaurant> filteredContent = restaurants.getContent().stream()
                    .filter(r -> {
                        double avgRating = r.getReviews().stream()
                                .mapToInt(review -> review.getRating())
                                .average()
                                .orElse(0.0);
                        return avgRating >= minRating;
                    })
                    .collect(Collectors.toList());

            // 創建新的 Page 對象
            return new PageImpl<>(
                    filteredContent,
                    pageable,
                    filteredContent.size()
            );
        }

        return restaurants;
    }

    // 透過 ID 取得特定餐廳
    public RestaurantResponse getRestaurantById(Long id, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到ID為 " + id + " 的餐廳"));
        return toDto(restaurant, currentUserId);
    }

    // 更新餐廳資訊
    @Transactional
    public Restaurant updateRestaurant(Long id, Restaurant newRestaurantData, String currentUserName,
                                       MultipartFile image) {
        try {
            // 檢查餐廳是否存在
            Restaurant restaurant = restaurantRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("找不到ID為 " + id + " 的餐廳"));

            // 檢查用戶是否是餐廳的創建者
            if (!restaurant.getCreatedByUsername().equals(currentUserName)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "使用者 " + currentUserName + " 無權更新餐廳 " + id);
            }

            // 更新餐廳資訊，但保留不應由前端修改的欄位
            restaurant.setName(newRestaurantData.getName());
            restaurant.setAddress(newRestaurantData.getAddress());
            restaurant.setPhone(newRestaurantData.getPhone());
            restaurant.setCategory(newRestaurantData.getCategory());
            restaurant.setDescription(newRestaurantData.getDescription());

            // 處理圖片
            if (image == null) {
                // 如果沒有新圖片，刪除舊圖片
                if (restaurant.getImageUrl() != null) {
                    imageService.deleteImage(restaurant.getImageUrl());
                    restaurant.setImageUrl(null);
                }
            } else if (!image.isEmpty()) {
                // 如果有新圖片，上傳新圖片
                try {
                    String imageUrl = imageService.uploadImage(image);
                    restaurant.setImageUrl(imageUrl);
                } catch (IOException e) {
                    throw new ValidationException("圖片上傳失敗");
                }
            }

            System.out.println("餐廳成功更新：" + id);
            return restaurantRepository.save(restaurant);
        } catch (Exception e) {
            System.err.println("更新餐廳時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 刪除餐廳
    @Transactional
    public void deleteRestaurant(Long id, String currentUserName) {
        try {
            // 檢查餐廳是否存在
            Restaurant restaurant = restaurantRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("找不到ID為 " + id + " 的餐廳"));

            // 檢查用戶是否存在
            User user = userRepository.findByUsername(currentUserName)
                    .orElseThrow(() -> new EntityNotFoundException("找不到使用者：" + currentUserName));

            if (user.getRole()  ==  Role.ADMIN) {
                // 直接進行刪除（下面的刪除邏輯）
            } else if (!restaurant.getCreatedByUsername().equals(currentUserName)) {
                // 不是創建者也不是 ADMIN，禁止
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "使用者 " + currentUserName + " 無權刪除餐廳 " + id);
            }

            // 先刪除與餐廳相關的用戶收藏關係
            userRestaurantRepository.deleteByRestaurantId(id);

            // 獲取餐廳所有評論的 ID
            List<Long> reviewIds = restaurant.getReviews().stream()
                    .map(Review::getId)
                    .collect(Collectors.toList());

            // 刪除所有評論的點贊記錄
            for (Long reviewId : reviewIds) {
                reviewLikeRepository.deleteByReviewId(reviewId);
            }

            // 保存更新後的餐廳（清除了標籤關聯）
            restaurantRepository.save(restaurant);

            // 最後刪除餐廳
            restaurantRepository.delete(restaurant);

            System.out.println("餐廳成功刪除：" + id);
        } catch (Exception e) {
            // 記錄錯誤
            System.err.println("刪除餐廳時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            // 重新拋出異常
            throw e;
        }
    }

    // 獲取使用者收藏餐廳
    public List<RestaurantResponse> getUserFavorites(Long userId) {
        List<Restaurant> restaurants = userRestaurantRepository.findByUserId(userId)
                .stream()
                .map(UserRestaurant::getRestaurant)
                .collect(Collectors.toList());

        // 轉換成 DTO
        return restaurants.stream()
                .map(restaurant -> toDto(restaurant, userId))  // 使用 toDto 方法轉換
                .collect(Collectors.toList());
    }

    // 獲取用戶創建的餐廳
    public List<Restaurant> getRestaurantsByCreator(String username) {
        return restaurantRepository.findByCreatedByUsername(username);
    }

    // 熱門餐廳
    public List<RestaurantResponse> getPopularRestaurants(Long currentUserId) {
        List<Restaurant> restaurants = restaurantRepository.findPopularRestaurants();
        return restaurants.stream().map(restaurant -> {
            RestaurantResponse response = new RestaurantResponse();
            response.setId(restaurant.getId());
            response.setName(restaurant.getName());
            response.setAddress(restaurant.getAddress());
            response.setPhone(restaurant.getPhone());
            response.setCategory(restaurant.getCategory());
            response.setDescription(restaurant.getDescription());
            response.setCreatedByUsername(restaurant.getCreatedByUsername());
            response.setImageUrl(restaurant.getImageUrl());
            // 計算平均評分
            response.setAverageRating(restaurant.getReviews().isEmpty() ? 0.0
                    : restaurant.getReviews().stream().mapToInt(Review::getRating).average().orElse(0.0));
            // 設定評論數
            response.setReviewCount(restaurant.getReviews() == null ? 0 : restaurant.getReviews().size());
            // 設定評論列表
            response.setReviews(restaurant.getReviews().stream()
                    .map(review -> toReviewDTO(review, currentUserId))
                    .collect(Collectors.toList()));
            return response;
        }).collect(Collectors.toList());
    }

    // 最新餐廳
    public List<RestaurantResponse> getLatestRestaurants(Long currentUserId) {
        List<Restaurant> restaurants = restaurantRepository.findTop10ByOrderByCreatedAtDesc();
        return restaurants.stream().map(restaurant -> {
            RestaurantResponse response = new RestaurantResponse();
            response.setId(restaurant.getId());
            response.setName(restaurant.getName());
            response.setAddress(restaurant.getAddress());
            response.setPhone(restaurant.getPhone());
            response.setCategory(restaurant.getCategory());
            response.setDescription(restaurant.getDescription());
            response.setCreatedByUsername(restaurant.getCreatedByUsername());
            response.setImageUrl(restaurant.getImageUrl());
            // 計算平均評分
            response.setAverageRating(restaurant.getReviews().isEmpty() ? 0.0
                    : restaurant.getReviews().stream().mapToInt(Review::getRating).average().orElse(0.0));
            // 設定評論數
            response.setReviewCount(restaurant.getReviews() == null ? 0 : restaurant.getReviews().size());
            // 設定評論列表
            response.setReviews(restaurant.getReviews().stream()
                    .map(review -> toReviewDTO(review, currentUserId))
                    .collect(Collectors.toList()));
            return response;
        }).collect(Collectors.toList());
    }

    // 計算餐廳平均評分
    public double calculateAverageRating(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("找不到餐廳"));

        double averageRating = restaurant.getReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        restaurant.setAverageRating(averageRating);
        restaurantRepository.save(restaurant);

        return averageRating;
    }

    // 將 Restaurant 轉換成 RestaurantResponse
    public RestaurantResponse toDto(Restaurant restaurant, Long currentUserId) {  // 回傳型別是 RestaurantResponse
        RestaurantResponse dto = new RestaurantResponse();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setPhone(restaurant.getPhone());
        dto.setCategory(restaurant.getCategory());
        dto.setDescription(restaurant.getDescription());
        dto.setCreatedByUsername(restaurant.getCreatedByUsername());
        System.out.println("toDto: restaurant.getImageUrl() = " + restaurant.getImageUrl());
        dto.setImageUrl(restaurant.getImageUrl());

        // 計算平均評分
        dto.setAverageRating(restaurant.getReviews().isEmpty() ? 0.0
                : restaurant.getReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0));

        // 設定評論數
        dto.setReviewCount(restaurant.getReviews().size());

        // 轉換評論為 DTO
        dto.setReviews(restaurant.getReviews().stream()
                .map(review -> toReviewDTO(review, currentUserId))
                .collect(Collectors.toList()));
        System.out.println("toDto 結尾 DTO imageUrl: " + dto.getImageUrl());
        return dto;

    }

    // 轉換列表的 Restaurant 物件為 DTO 列表
    public List<RestaurantResponse> toDtoList(List<Restaurant> restaurants, Long currentUserId) {
        return restaurants.stream()
                .map(restaurant -> toDto(restaurant, currentUserId))
                .collect(Collectors.toList());
    }

    public boolean existsById(Long restaurantId) {
        return restaurantRepository.existsById(restaurantId);
    }

    //Review 轉 DTO 的方法
    private ReviewDTO toReviewDTO(Review review, Long currentUserId) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setCreated_At(review.getCreated_At());
        dto.setUpdated_At(review.getUpdated_At());
        dto.setImageUrl(review.getImageUrl());

        // 設置用戶資訊
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setUserRole(review.getUser().getRole());

        // 設置餐廳資訊
        dto.setRestaurantId(review.getRestaurant().getId());
        dto.setRestaurantName(review.getRestaurant().getName());

        // 設置點讚資訊
        dto.setLikeCount(review.getLikes().size());
        dto.setIsLiked(review.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(currentUserId)));

        return dto;
    }

    public Restaurant getRestaurantEntityById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到ID為 " + id + " 的餐廳"));
    }
}
