package com.blog.repository;

import com.blog.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Class Name: AnswerRepository
 * Package: repository
 * Description:
 * author:
 * Create: 2025/1/6
 * Version: 1.0
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurantId(Long restaurantId);

    List<Review> findByUserId(Long userId);
}
