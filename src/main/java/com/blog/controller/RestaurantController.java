package com.blog.controller;

import com.blog.dto.RestaurantRequest;
import com.blog.model.Restaurant;
import com.blog.model.User;
import com.blog.service.UserRestaurantService;
import com.blog.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.blog.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Class Name: QuestionController
 * Package: controller
 * Description:
 * author:
 * Create: 2025/1/3
 * Version: 1.0
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private UserRestaurantService userRestaurantService;

    // 新增餐廳
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody Restaurant restaurant) {
        return ResponseEntity.ok(restaurantService.createRestaurant(restaurant));
    }

    // 獲取所有餐廳
    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    // 獲取單個餐廳
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    // 更新餐廳資訊
    @PutMapping("/{id}")
    private ResponseEntity<Restaurant> updateRestaurant(@PathVariable Long id, @RequestBody Restaurant newRestaurant) {
        return ResponseEntity.ok((restaurantService.updateRestaurant(id, newRestaurant)));
    }

    // 刪除餐廳
    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteRestaurant(@PathVariable("id") Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    // 計算餐廳平均評分
    @GetMapping("/{id}/rating")
    private ResponseEntity<Double> getAverageRate(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.calculateAverageRating(id));
    }

    // 依名稱搜尋餐廳
    @GetMapping("/search")
    public ResponseEntity<List<Restaurant>> searchRestaurantsByName(@RequestParam String name) {
        return ResponseEntity.ok(restaurantService.searchRestaurantsByName(name));
    }

    // 依評分範圍搜尋餐廳
    @GetMapping("/search/rating")
    public ResponseEntity<List<Restaurant>> searchRestaurantsByRating(@RequestParam Double minRating,
            @RequestParam Double maxRating) {
        return ResponseEntity.ok(restaurantService.searchRestaurantsByRating(minRating, maxRating));
    }

    // 依標籤（料理類別）搜尋餐廳
    @GetMapping("/search/tags")
    public ResponseEntity<List<Restaurant>> searchRestaurantsByTags(@RequestParam List<String> tags) {
        return ResponseEntity.ok(restaurantService.searchRestaurantsByTag(tags));
    }

    // 收藏餐廳
    @PostMapping("/{restaurantId}/favorite")
    public ResponseEntity<String> addRestaurantToFavorites(@RequestParam Long userId, @PathVariable Long restaurantId) {
        User user = new User();
        user.setId(userId);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        userRestaurantService.addRestaurantToFavorites(user, restaurant);
        return ResponseEntity.status(HttpStatus.CREATED).body("餐廳已加入收藏");
    }

    // 移除用戶喜歡的餐廳
    @DeleteMapping("/{restaurantId}/favorite")
    public ResponseEntity<String> removeRestaurantFromFavorites(@RequestParam Long userId,
            @PathVariable Long restaurantId) {
        User user = new User();
        user.setId(userId);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        userRestaurantService.removeRestaurantFromFavorites(user, restaurant);
        return ResponseEntity.status(HttpStatus.CREATED).body("餐廳已移除收藏");
    }

    // 獲得用戶喜歡的餐廳
    @GetMapping("/favorites")
    public ResponseEntity<List<Restaurant>> getUserFavorites(@RequestParam Long userId) {
        List<Restaurant> favorites = restaurantService.getUserFavorites(userId);

        if (favorites.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }

        return ResponseEntity.ok(favorites); // 200 OK
    }

    // 獲取熱門的餐廳
    @GetMapping("/popular")
    public ResponseEntity<List<Restaurant>> getPopularRestaurants() {
        return ResponseEntity.ok(restaurantService.getPopularRestaurants());
    }

    // 為餐廳加上標籤
    @PostMapping("/{restaurantId}/tags")
    public ResponseEntity<Restaurant> addTagsToRestaurant(@PathVariable Long restaurantId,
            @RequestBody List<String> tags) {
        return ResponseEntity.ok(restaurantService.addTagsToRestaurant(restaurantId, tags));
    }

    // 依標籤搜尋餐廳
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<List<Restaurant>> getRestaurantsByTag(@PathVariable String tagName) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByTag(tagName));
    }

}
