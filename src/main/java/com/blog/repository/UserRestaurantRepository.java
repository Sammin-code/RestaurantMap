package com.blog.repository;

import com.blog.model.Restaurant;
import com.blog.model.User;
import com.blog.model.UserRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Class Name: UserRestaurantRepository
 * Package: com.blog.repository
 * Description:
 * author:
 * Create: 2025/3/4
 * Version: 1.0
 */
@Repository
public interface UserRestaurantRepository extends JpaRepository<UserRestaurant, Long> {


    Optional<UserRestaurant> findByUserAndRestaurant(User user, Restaurant restaurant);

    void delete(UserRestaurant userRestaurant);

    List<UserRestaurant> findByUserId(Long userId);
}
