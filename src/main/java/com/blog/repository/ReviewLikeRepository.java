package com.blog.repository;

import com.blog.model.Review;
import com.blog.model.ReviewLike;
import com.blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Class Name: ReviewLikeRepository
 * Package: com.blog.repository
 * Description:
 * author:
 * Create: 2025/3/10
 * Version: 1.0
 */
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByUserAndReview(User user, Review review);
    void deleteByUserAndReview(User user, Review review);
    long countByReview(Review review);
}
