package com.blog.repository;

import com.blog.model.Review;
import com.blog.model.ReviewLike;
import com.blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    // 根據評論ID刪除所有點贊記錄
    @Modifying
    @Transactional
    @Query("DELETE FROM ReviewLike rl WHERE rl.review.id = :reviewId")
    void deleteByReviewId(Long reviewId);


    Optional<ReviewLike> findByUserAndReview(User user, Review review);

}
