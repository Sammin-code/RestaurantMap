package com.blog.dto;

import com.blog.model.Review;
import com.blog.model.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
  private Long id;
  private String content;
  private Integer rating;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime created_At;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updated_At;

  private String imageUrl;  // 保留單張圖片

  private Long userId;
  private String username;
  private Role userRole;  // 新增：用戶角色

  private Long restaurantId;
  private String restaurantName;

  private Integer likeCount;  // 新增：點讚數
  private Boolean isLiked;    // 新增：當前用戶是否已點讚
  private Boolean isEdited;

  public ReviewDTO(Long id, String content, Integer rating, LocalDateTime created_At, LocalDateTime updated_At, String imageUrl, Long userId, String username, Role userRole, String restaurantName, Long restaurantId) {
    this.id = id;
    this.content = content;
    this.rating = rating;
    this.created_At = created_At;
    this.updated_At = updated_At;
    this.imageUrl = imageUrl;
    this.userId = userId;
    this.username = username;
    this.userRole = userRole;
    this.restaurantName = restaurantName;
    this.restaurantId = restaurantId;
  }

  public ReviewDTO() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  public LocalDateTime getCreated_At() {
    return created_At;
  }

  public void setCreated_At(LocalDateTime created_At) {
    this.created_At = created_At;
  }

  public LocalDateTime getUpdated_At() {
    return updated_At;
  }

  public void setUpdated_At(LocalDateTime updated_At) {
    this.updated_At = updated_At;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Role getUserRole() {
    return userRole;
  }

  public void setUserRole(Role userRole) {
    this.userRole = userRole;
  }

  public Long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(Long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public String getRestaurantName() {
    return restaurantName;
  }

  public void setRestaurantName(String restaurantName) {
    this.restaurantName = restaurantName;
  }

  public Integer getLikeCount() {
    return likeCount;
  }

  public void setLikeCount(Integer likeCount) {
    this.likeCount = likeCount;
  }

  public Boolean getLiked() {
    return isLiked;
  }

  public void setLiked(Boolean liked) {
    isLiked = liked;
  }

  public Boolean getEdited() {
    return isEdited;
  }

  public void setEdited(Boolean edited) {
    isEdited = edited;
  }
}