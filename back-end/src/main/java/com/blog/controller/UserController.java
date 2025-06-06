package com.blog.controller;

import com.blog.dto.*;
import com.blog.model.Restaurant;
import com.blog.model.Review;
import com.blog.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.blog.service.UserService;
import com.blog.service.RestaurantService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;
import lombok.extern.slf4j.Slf4j;

/**
 * Class Name: UserController
 * Package: controller
 * Description:
 * author:
 * Create: 2025/1/3
 * Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    com.blog.service.RestaurantService restaurantService;

    // 註冊用戶
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserRegisterDTO userRegisterDTO) {
        try {
            UserDTO registeredUser = userService.registerUser(userRegisterDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    // 登入
    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            System.out.println("收到登入請求 - 用戶名: " + userLoginDTO.getUsername());
            String token = userService.authenticateUser(userLoginDTO.getUsername(), userLoginDTO.getPassword());
            System.out.println("登入成功 - 生成 token");
            return ok(token);
        } catch (Exception e) {
            System.out.println("登入失敗: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登入失敗: " + e.getMessage());
        }
    }

    // 獲取當前用戶信息
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO userDTO = userService.getUserByUsername(userDetails.getUsername());
        return ok(userDTO);
    }

    // 查看個人資訊
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('REVIEWER')")
    public ResponseEntity<User> getUserProfile(
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    // 查看收藏的餐廳
    @GetMapping("/{userId}/favorites")
    @PreAuthorize("hasAnyRole('REVIEWER')")
    public ResponseEntity<List<RestaurantResponse>> getUserFavoriteRestaurants(@PathVariable Long userId) {
        List<RestaurantResponse> responseList = userService.getUserFavoriteRestaurants(userId);

        if (responseList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(responseList);
    }

    // 查看用戶發表的評論
    @GetMapping("/{userId}/reviews")
    @PreAuthorize("hasAnyRole('REVIEWER')")
    public ResponseEntity<List<ReviewDTO>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserReviews(userId));
    }

    // 查看用戶創建的餐廳
    @GetMapping("/{userId}/restaurants")
    @PreAuthorize("hasAnyRole('REVIEWER')")
    public ResponseEntity<List<RestaurantResponse>> getUserCreatedRestaurants(@PathVariable Long userId) {
        List<RestaurantResponse> responseList = userService.getUserCreatedRestaurants(userId);
        responseList.forEach(restaurant -> {
            // 已移除詳細日誌
        });
        if (responseList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(responseList);
    }

}
