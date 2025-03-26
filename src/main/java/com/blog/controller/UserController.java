package com.blog.controller;

import com.blog.dto.UserDTO;
import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.model.Restaurant;
import com.blog.model.Review;
import com.blog.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.blog.service.UserService;

import java.util.List;

/**
 * Class Name: UserController
 * Package: controller
 * Description:
 * author:
 * Create: 2025/1/3
 * Version: 1.0
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;

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
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            String token = userService.authenticateUser(userLoginDTO.getUsername(), userLoginDTO.getPassword());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登入失敗: " + e.getMessage());
        }
    }

    // 獲取當前用戶信息
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO userDTO = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(userDTO);
    }

    // 更新用戶資料
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUserProfile(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO) {
        UserDTO updatedUser = userService.updateUserProfile(id, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // 更改密碼
    @PostMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok("修改密碼成功");
    }

    // 查看個人資訊
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    // 查看收藏的餐廳
    @GetMapping("/{userId}/favorites")
    public ResponseEntity<List<Restaurant>> getUserFavoriteRestaurants(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserFavoriteRestaurants(userId));
    }

    // 查看用戶發表的評論
    @GetMapping("/{userId}/reviews")
    public ResponseEntity<List<Review>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getgetUserReviews(userId));
    }

}
