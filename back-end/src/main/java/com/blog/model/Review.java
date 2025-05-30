package com.blog.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Class Name: Answer
 * Package: model
 * Description:
 * author:
 * Create: 2025/1/2
 * Version: 1.0
 */

@Data
@Entity
@Table(name = "reviews")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_At;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated_At;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-reviews")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonBackReference(value = "restaurant-reviews")
    private Restaurant restaurant;


    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "review" })
    private Set<ReviewLike> likes = new HashSet<>();

    @JsonGetter("user")
    public User getUser() {
        return user;
    }

    @JsonSetter("user")
    public void setUser(User user) {
        this.user = user;
    }

    @JsonGetter("userId")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    @JsonGetter("restaurant")
    public Restaurant getRestaurant() {
        return restaurant;
    }

    @JsonSetter("restaurant")
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    @PrePersist
    protected void onCreate() {
        created_At = LocalDateTime.now();
        updated_At = LocalDateTime.now();  // 創建時，updated_At 也設為當前時間
    }

    @PreUpdate
    protected void onUpdate() {
        updated_At = LocalDateTime.now();  // 更新時，只更新 updated_At
    }
}
