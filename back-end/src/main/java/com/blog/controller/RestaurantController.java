package com.blog.controller;

import com.blog.dto.RestaurantResponse;
import com.blog.model.Restaurant;
import com.blog.model.User;
import com.blog.service.UserRestaurantService;
import com.blog.service.RestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import com.blog.repository.UserRepository;

import org.springframework.web.server.ResponseStatusException;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

  @Autowired
  private RestaurantService restaurantService;

  @Autowired
  private UserRestaurantService userRestaurantService;

  @Autowired
  private UserRepository userRepository;

  private static final Logger log = LoggerFactory.getLogger(RestaurantController.class);

  // 新增餐廳
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('REVIEWER')")
  public ResponseEntity<RestaurantResponse> createRestaurant(
          @RequestPart("restaurant") String restaurantJson,
          @RequestPart(value = "image", required = false) MultipartFile image,
          Authentication authentication) throws IOException {

    ObjectMapper objectMapper = new ObjectMapper();
    RestaurantResponse dto = objectMapper.readValue(restaurantJson, RestaurantResponse.class);

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String currentUserName = userDetails.getUsername();
    Long currentUserId = ((User) userDetails).getId();

    Restaurant created = restaurantService.createRestaurant(dto, currentUserName, image);
    RestaurantResponse response = restaurantService.toDto(created, currentUserId);

    System.out.println("Controller 回傳 DTO imageUrl: " + response.getImageUrl());
    System.out.println("Controller created.getImageUrl(): " + created.getImageUrl());
    return ResponseEntity.ok(response);
  }

  // 獲取所有餐廳+篩選搜尋
  @GetMapping
  public ResponseEntity<?> getAllRestaurants(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Double minRating) {
    try {
      Long currentUserId = getCurrentUserId();
      Page<Restaurant> restaurants = restaurantService.getAllRestaurants(
          page, size, sort, keyword, category, minRating);

      // 初始化評論並計算評分
      restaurants.getContent().forEach(restaurant -> {
        // 初始化評論
        Hibernate.initialize(restaurant.getReviews());

        // 設置評論數
        restaurant.setReviewCount(restaurant.getReviews().size());

        // 計算平均評分
        if (!restaurant.getReviews().isEmpty()) {
          double averageRating = restaurant.getReviews().stream()
              .mapToInt(review -> review.getRating())
              .average()
              .orElse(0.0);
          restaurant.setAverageRating(averageRating);
        } else {
          restaurant.setAverageRating(0.0);
        }
      });

      // 轉換成 DTO
      Page<RestaurantResponse> responsePage = restaurants
          .map(restaurant -> restaurantService.toDto(restaurant, currentUserId));

      return ResponseEntity.ok(responsePage);
    } catch (Exception e) {
      log.error("獲取餐廳列表時發生錯誤: {}", e.getMessage(), e);
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", "獲取餐廳列表失敗");
      errorResponse.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  // 獲取單個餐廳
  @GetMapping("/{id}")
  public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
    try {
      Long currentUserId = getCurrentUserId();
      RestaurantResponse response = restaurantService.getRestaurantById(id, currentUserId);
      return ResponseEntity.ok(response);
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      log.error("獲取餐廳詳情時發生錯誤", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "獲取餐廳詳情失敗", e);
    }
  }

  // 更新餐廳資訊
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('REVIEWER')")
  public ResponseEntity<RestaurantResponse> updateRestaurant(
          @PathVariable Long id,
          @RequestPart("restaurant") String restaurantJson,
          @RequestPart(value = "image", required = false) MultipartFile image,
          @RequestPart(value = "removeImage", required = false) String removeImage,
          Authentication authentication) throws IOException {

    ObjectMapper objectMapper = new ObjectMapper();
    RestaurantResponse dto = objectMapper.readValue(restaurantJson, RestaurantResponse.class);

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String currentUserName = userDetails.getUsername();
    Long currentUserId = ((User) userDetails).getId();

    Restaurant newRestaurant = new Restaurant();
    newRestaurant.setName(dto.getName());
    newRestaurant.setAddress(dto.getAddress());
    newRestaurant.setPhone(dto.getPhone());
    newRestaurant.setCategory(dto.getCategory());
    newRestaurant.setDescription(dto.getDescription());

    // 如果有 removeImage 標記，傳入 null 作為圖片
    MultipartFile imageToUse = "true".equals(removeImage) ? null : image;
    Restaurant updatedRestaurant = restaurantService.updateRestaurant(id, newRestaurant, currentUserName, imageToUse);
    RestaurantResponse response = restaurantService.toDto(updatedRestaurant, currentUserId);

    return ResponseEntity.ok(response);
  }

  // 刪除餐廳
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
  public ResponseEntity<Void> deleteRestaurant(@PathVariable("id") Long id, Authentication authentication) {
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String currentUserName = userDetails.getUsername();
    restaurantService.deleteRestaurant(id, currentUserName);
    return ResponseEntity.noContent().build();
  }

  // 計算餐廳平均評分
  @GetMapping("/{id}/rating")
  public ResponseEntity<Double> getAverageRate(@PathVariable Long id) {
    double averageRating = restaurantService.calculateAverageRating(id);
    return ResponseEntity.ok(averageRating);
  }

  // 收藏餐廳
  @PostMapping("/{restaurantId}/favorite")
  @PreAuthorize("hasAnyRole('REVIEWER')")
  public ResponseEntity<String> addRestaurantToFavorites(@PathVariable Long restaurantId,
      Authentication authentication) {
    log.info("Attempting to add favorite - restaurantId: {}, authentication: {}",
        restaurantId, authentication);
    log.info("User authorities: {}", authentication.getAuthorities());

    try {
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String currentUserName = userDetails.getUsername();
      Long currentUserId = ((User) userDetails).getId();
      log.info("Current user: {}", currentUserName);

      User user = userRepository.findByUsername(currentUserName)
          .orElseThrow(() -> new RuntimeException("找不到該用戶"));
      log.info("Found user: {}", user.getUsername());

      Restaurant restaurant = restaurantService.getRestaurantEntityById(restaurantId); // 使用新方法
      log.info("Found restaurant: {}", restaurant.getName());

      userRestaurantService.addRestaurantToFavorites(user, restaurant);
      log.info("Successfully added restaurant to favorites");

      return ResponseEntity.status(HttpStatus.CREATED).body("餐廳已加入收藏");
    } catch (Exception e) {
      log.error("Error adding restaurant to favorites", e);
      throw e;
    }
  }

  // 取消收藏餐廳
  @DeleteMapping("/{restaurantId}/favorite")
  @PreAuthorize("hasAnyRole('REVIEWER')")
  public ResponseEntity<String> removeRestaurantFromFavorites(@PathVariable Long restaurantId,
      Authentication authentication) {
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String currentUserName = userDetails.getUsername();
    Long currentUserId = ((User) userDetails).getId(); // 獲取用戶ID

    User user = userRepository.findByUsername(currentUserName)
        .orElseThrow(() -> new RuntimeException("找不到該用戶"));
    Restaurant restaurant = restaurantService.getRestaurantEntityById(restaurantId); // 使用新方法

    userRestaurantService.removeRestaurantFromFavorites(user, restaurant);
    return ResponseEntity.status(HttpStatus.CREATED).body("餐廳已移除收藏");
  }

  // 獲得用戶喜歡的餐廳
  @GetMapping("/favorites")
  @PreAuthorize("hasAnyRole('REVIEWER')")
  public ResponseEntity<List<RestaurantResponse>> getUserFavorites(Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    try {
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      Long currentUserId = ((User) userDetails).getId();

      List<RestaurantResponse> favorites = restaurantService.getUserFavorites(currentUserId);
      // 這裡直接回傳空陣列，不要回 204
      return ResponseEntity.ok(favorites);
    } catch (Exception e) {
      log.error("獲取收藏餐廳列表時出錯", e);
      // 這裡也建議回傳空陣列，除非你要讓前端顯示錯誤訊息
      return ResponseEntity.ok(new ArrayList<>());
    }
  }

  // 熱門餐廳
  @GetMapping("/popular")
  public List<RestaurantResponse> getPopularRestaurants() {
    // 從 SecurityContext 獲取當前用戶 ID
    Long currentUserId = getCurrentUserId();
    return restaurantService.getPopularRestaurants(currentUserId);
  }

  // 最新餐廳
  @GetMapping("/latest")
  public List<RestaurantResponse> getLatestRestaurants() {
    // 從 SecurityContext 獲取當前用戶 ID
    Long currentUserId = getCurrentUserId();
    return restaurantService.getLatestRestaurants(currentUserId);
  }

  // 檢查餐廳是否被收藏
  @GetMapping("/{restaurantId}/favorite/status")
  public ResponseEntity<Boolean> checkFavoriteStatus(@PathVariable Long restaurantId, Authentication authentication) {
    // 如果用戶未登錄，直接返回 false
    if (authentication == null) {
      return ResponseEntity.ok(false);
    }

    try {
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String currentUserName = userDetails.getUsername();
      Long currentUserId = ((User) userDetails).getId(); // 獲取用戶ID

      User user = userRepository.findByUsername(currentUserName)
          .orElseThrow(() -> new RuntimeException("找不到該用戶"));
      Restaurant restaurant = restaurantService.getRestaurantEntityById(restaurantId); // 使用新方法

      boolean isFavorite = userRestaurantService.isRestaurantInFavorites(user, restaurant);
      return ResponseEntity.ok(isFavorite);
    } catch (Exception e) {
      // 記錄錯誤並返回 false
      log.error("檢查收藏狀態時出錯", e);
      return ResponseEntity.ok(false);
    }
  }

  // 獲取當前用戶 ID 的輔助方法
  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      try {
        return ((User) authentication.getPrincipal()).getId(); // 直接使用 User 類別
      } catch (ClassCastException e) {
        // 如果轉型失敗，可能是未登入狀態
        return null;
      }
    }
    return null;
  }
}