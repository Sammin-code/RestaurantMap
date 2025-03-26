package com.blog.service;

import com.blog.dto.UserDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.model.*;
import com.blog.repository.ReviewRepository;
import com.blog.repository.UserRestaurantRepository;
import com.blog.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import com.blog.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class Name: UserService
 * Package: service
 * Description:
 * author:
 * Create: 2025/1/3
 * Version: 1.0
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRestaurantRepository userRestaurantRepository;
    private final ReviewRepository reviewRepository;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            UserRestaurantRepository userRestaurantRepository,
            ReviewRepository reviewRepository,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRestaurantRepository = userRestaurantRepository;
        this.reviewRepository = reviewRepository;
        this.authenticationManager = authenticationManager;
    }

    // 用戶註冊
    public UserDTO registerUser(UserRegisterDTO userRegisterDTO) {
        if (userRepository.findByUsername(userRegisterDTO.getUsername()).isPresent()) {
            throw new RuntimeException("用戶名已存在");
        }

        if (userRegisterDTO.getPassword() == null || userRegisterDTO.getPassword().length() < 6) {
            throw new RuntimeException("密碼長度必須至少為6位");
        }

        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        user.setEmail(userRegisterDTO.getEmail());
        user.setRole(Role.REVIEWER);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    // **轉換方法**
    private UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        return userDTO;
    }

    // 識別用戶
    public String authenticateUser(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            User user = (User) authentication.getPrincipal();
            return jwtTokenProvider.generateToken(user);
        } catch (Exception e) {
            throw new RuntimeException("用戶名或密碼錯誤");
        }
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("找不到該用戶，帳號：" + username));

        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    // 更新用戶資料
    public UserDTO updateUserProfile(Long userId, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到該用戶"));

        user.setEmail(userUpdateDTO.getEmail());
        user.setProfilePicture(userUpdateDTO.getProfilePicture());

        User updatedUser = userRepository.save(user);

        // 將 User 轉換成 UserDTO
        return new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                updatedUser.getProfilePicture());
    }

    // 用戶變換密碼
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到該用戶"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("舊的密碼不正確");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 獲取用戶資訊
    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到該用戶"));
    }

    // 取得用戶喜歡的餐廳
    public List<Restaurant> getUserFavoriteRestaurants(Long userId) {
        return userRestaurantRepository.findByUserId(userId)
                .stream()
                .map(UserRestaurant::getRestaurant)
                .collect(Collectors.toList());
    }

    // 獲取用戶評論
    public List<Review> getgetUserReviews(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
}
