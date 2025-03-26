package com.blog.service;

import com.blog.model.Restaurant;
import com.blog.model.User;
import com.blog.model.UserRestaurant;
import com.blog.repository.UserRestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class Name: UserRestaurantService
 * Package: com.blog.service
 * Description:
 * author:
 * Create: 2025/3/4
 * Version: 1.0
 */
@Service
public class UserRestaurantService {

    @Autowired
    private UserRestaurantRepository userRestaurantRepository;

    // 添加喜歡的餐廳
    public void addRestaurantToFavorites(User user, Restaurant restaurant) {
        UserRestaurant userRestaurant = new UserRestaurant();
        userRestaurant.setUser(user);
        userRestaurant.setRestaurant(restaurant);

        userRestaurantRepository.save(userRestaurant);
    }

    // 移除喜歡的餐廳
    public void removeRestaurantFromFavorites(User user, Restaurant restaurant) {
        Optional<UserRestaurant> userRestaurant = userRestaurantRepository.findByUserAndRestaurant(user, restaurant);
        userRestaurant.ifPresent(userRestaurantRepository::delete);
    }
}
