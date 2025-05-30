package com.blog.service;

import com.blog.model.Restaurant;
import com.blog.model.User;
import com.blog.model.UserRestaurant;
import com.blog.repository.UserRestaurantRepository;
import com.blog.exception.ValidationException;
import com.blog.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    @Transactional
    public void addRestaurantToFavorites(User user, Restaurant restaurant) {
        if (user == null) {
            throw new ValidationException("用戶不能為空");
        }
        if (restaurant == null) {
            throw new ValidationException("餐廳不能為空");
        }

        Optional<UserRestaurant> userRestaurant = userRestaurantRepository.findByUserAndRestaurant(user, restaurant);
        if (userRestaurant.isPresent()) {
            throw new ValidationException("該餐廳已經在收藏列表中");
        }

        UserRestaurant newUserRestaurant = new UserRestaurant();
        newUserRestaurant.setUser(user);
        newUserRestaurant.setRestaurant(restaurant);
        userRestaurantRepository.save(newUserRestaurant);
    }

    // 移除喜歡的餐廳
    @Transactional
    public void removeRestaurantFromFavorites(User user, Restaurant restaurant) {
        if (user == null) {
            throw new ValidationException("用戶不能為空");
        }
        if (restaurant == null) {
            throw new ValidationException("餐廳不能為空");
        }

        Optional<UserRestaurant> userRestaurant = userRestaurantRepository.findByUserAndRestaurant(user, restaurant);
        if (userRestaurant.isEmpty()) {
            throw new ResourceNotFoundException("該餐廳不在收藏列表中");
        }

        userRestaurantRepository.delete(userRestaurant.get());
    }

    // 檢查餐廳是否在用戶的收藏中
    public boolean isRestaurantInFavorites(User user, Restaurant restaurant) {
        if (user == null) {
            throw new ValidationException("用戶不能為空");
        }
        if (restaurant == null) {
            throw new ValidationException("餐廳不能為空");
        }

        Optional<UserRestaurant> userRestaurant = userRestaurantRepository.findByUserAndRestaurant(user, restaurant);
        return userRestaurant.isPresent();
    }
}
