package com.blog.repository;

import com.blog.dto.ReviewDTO;
import com.blog.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 獲取用戶的評論，包含餐廳信息
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.restaurant WHERE r.user.id = :userId")
    List<Review> findByUser_Id(@Param("userId") Long userId);

    // 獲取餐廳的評論，包含用戶和餐廳信息
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.restaurant WHERE r.restaurant.id = :restaurantId")
    List<Review> findByRestaurantId(@Param("restaurantId") Long restaurantId);
    // 新增使用 DTO 的查詢方法
    @Query("SELECT new com.blog.dto.ReviewDTO(" +
            "r.id, r.content, r.rating, r.created_At, r.updated_At, r.imageUrl, " +
            "r.user.id, r.user.username, r.user.role, " +
            "r.restaurant.name, r.restaurant.id" +  // 注意順序：先 name 後 id
            ") " +
            "FROM Review r " +
            "WHERE r.user.id = :userId")
    List<ReviewDTO> findReviewsWithDetailsByUserId(@Param("userId") Long userId);
}
