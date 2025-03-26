package com.blog.repository;

import com.blog.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Class Name: QuestionRepository
 * Package: repository
 * Description:
 * author:
 * Create: 2025/1/6
 * Version: 1.0
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    //依餐廳名稱模糊搜尋
    List<Restaurant> findByNameContaining(String name);
    //依評分範圍搜尋
    List<Restaurant> findByAverageRatingBetween(Double minRating, Double maxRating);
    //依標籤搜尋
    @Query("SELECT r FROM Restaurant r JOIN r.tags t WHERE t.name IN :tags")
    List<Restaurant> findByTagsIn(@Param("tags") List<String> tags);

    List<Restaurant> findByCategory(String category);

}
