package com.blog.repository;

import com.blog.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
  Page<Restaurant> findAll(Pageable pageable);

  @Query("SELECT r FROM Restaurant r WHERE r.createdByUsername = :username")
  List<Restaurant> findByCreatedByUsername(@Param("username") String username);



  @Query("SELECT r FROM Restaurant r ORDER BY r.createdAt DESC")
  Page<Restaurant> findAllOrderByCreatedAtDesc(Pageable pageable);

  List<Restaurant> findTop10ByOrderByCreatedAtDesc();

  // 熱門餐廳：根據評論數和平均評分排序
  @Query("SELECT r FROM Restaurant r LEFT JOIN r.reviews rv GROUP BY r ORDER BY COUNT(rv) DESC, AVG(rv.rating) DESC")
  List<Restaurant> findPopularRestaurants();


  // 添加新的篩選方法
  @Query("SELECT r FROM Restaurant r WHERE " +
          "(:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
          "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
          "(:category IS NULL OR r.category = :category)")
  Page<Restaurant> findAllWithFilters(
          @Param("keyword") String keyword,
          @Param("category") String category,
          Pageable pageable
  );
}