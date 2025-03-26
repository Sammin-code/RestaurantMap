package com.blog.service;

import com.blog.dto.RestaurantRequest;
import com.blog.model.Restaurant;
import com.blog.model.Review;
import com.blog.model.Tag;
import com.blog.model.UserRestaurant;
import com.blog.repository.RestaurantRepository;
import com.blog.repository.TagRepository;
import com.blog.repository.UserRestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private RestaurantRepository restaurantRepository;

    private final UserRestaurantRepository userRestaurantRepository;

    private final TagRepository tagRepository;

    public RestaurantService(UserRestaurantRepository userRestaurantRepository, TagRepository tagRepository) {
        this.userRestaurantRepository = userRestaurantRepository;
        this.tagRepository = tagRepository;
    }

    // 新增餐廳
    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    // 取得所有餐廳
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    // 透過 ID 取得特定餐廳
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該餐廳"));
    }

    // 刪除餐廳
    public void deleteRestaurant(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new RuntimeException("找不到該餐廳");
        }
        restaurantRepository.deleteById(id);
    }

    // 更新餐廳資訊
    public Restaurant updateRestaurant(Long id, Restaurant newRestaurantData) {
        return restaurantRepository.findById(id).map(restaurant -> {
            restaurant.setName(newRestaurantData.getName());
            restaurant.setAddress(newRestaurantData.getAddress());
            restaurant.setPhone(newRestaurantData.getPhone());
            restaurant.setCategory(newRestaurantData.getCategory());
            restaurant.setOpeningHours(newRestaurantData.getOpeningHours());
            return restaurantRepository.save(restaurant);
        }).orElseThrow(() -> new RuntimeException("找不到該餐廳"));
    }

    // 計算餐廳的平均評分
    public double calculateAverageRating(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該餐廳"));
        List<Review> reviews = restaurant.getReviews();
        if (reviews.isEmpty())
            return 0;

        double average = reviews.stream()
                .mapToInt(review -> review.getRating())
                .average()
                .orElse(0.0);
        restaurant.setAverageRating(average);
        restaurantRepository.save(restaurant);
        return average;
    }

    // 依名稱搜尋餐廳
    public List<Restaurant> searchRestaurantsByName(String name) {
        return restaurantRepository.findByNameContaining(name);
    }

    // 依評分範圍搜尋餐廳
    public List<Restaurant> searchRestaurantsByRating(Double minRating, Double maxRating) {
        return restaurantRepository.findByAverageRatingBetween(minRating, maxRating);
    }

    // 依標籤（料理類別）搜尋餐廳
    public List<Restaurant> searchRestaurantsByTag(List<String> tags) {
        return searchRestaurantsByTag(tags);
    }

    // 獲取使用者收藏餐廳
    public List<Restaurant> getUserFavorites(Long userId) {
        return userRestaurantRepository.findByUserId(userId)
                .stream()
                .map(UserRestaurant::getRestaurant)
                .collect(Collectors.toList());
    }

    // 獲取熱門餐廳
    public List<Restaurant> getPopularRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt((Restaurant r) -> r.getReviews().size())
                        .thenComparingDouble(r -> r.getAverageRating()).reversed())
                .collect(Collectors.toList());
    }

    // 為餐廳加上標籤
    public Restaurant addTagsToRestaurant(Long restaurantId, List<String> tagNames) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("找不到餐廳"));

        List<Tag> tags = tagNames.stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName))))
                .collect(Collectors.toList());
        restaurant.getTags().addAll(tags);
        return restaurantRepository.save(restaurant);
    }

    // 依標籤搜尋餐廳
    public List<Restaurant> getRestaurantsByTag(String tagName) {
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new RuntimeException("找不到標籤"));
        return tag.getRestaurants();

    }
}
