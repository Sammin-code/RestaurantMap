package com.blog.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class Name: Question
 * Package: model
 * Description:
 * author:
 * Create: 2024/12/31
 * Version: 1.0
 */

@Entity
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String category;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();
    private Double averageRating;

    @ManyToMany
    @JoinTable(
            name = "restaurant_tag",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    private LocalDateTime created_At;

    @OneToMany(mappedBy = "restaurant")
    private List<UserRestaurant> userRestaurants;

    @ElementCollection
    @CollectionTable(name = "restaurant_hours", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "opening_hours")
    private List<String> openingHours = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreated_At() {
        return created_At;
    }

    public void setCreated_At(LocalDateTime created_At) {
        this.created_At = created_At;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public List<UserRestaurant> getUserRestaurants() {
        return userRestaurants;
    }

    public void setUserRestaurants(List<UserRestaurant> userRestaurants) {
        this.userRestaurants = userRestaurants;
    }

    public List<String> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(List<String> openingHours) {
        this.openingHours = openingHours;
    }
}
