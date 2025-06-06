package com.blog.service;

import com.blog.dto.*;
import com.blog.model.*;
import com.blog.repository.RestaurantRepository;
import com.blog.repository.ReviewRepository;
import com.blog.repository.UserRestaurantRepository;
import com.blog.security.JwtTokenProvider;
import com.blog.exception.ResourceNotFoundException;
import com.blog.exception.ValidationException;
import com.blog.exception.UnauthorizedException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
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
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRestaurantRepository userRestaurantRepository;
    private final ReviewRepository reviewRepository;
    private final AuthenticationManager authenticationManager;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private final ReviewService reviewService;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            UserRestaurantRepository userRestaurantRepository,
            ReviewRepository reviewRepository,
            AuthenticationManager authenticationManager, RestaurantRepository restaurantRepository,
            RestaurantService restaurantService, ReviewService reviewService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRestaurantRepository = userRestaurantRepository;
        this.reviewRepository = reviewRepository;
        this.authenticationManager = authenticationManager;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
    }

    // 用戶註冊
    public UserDTO registerUser(UserRegisterDTO userRegisterDTO) {
        logger.info("嘗試註冊新用戶: {}", userRegisterDTO.getUsername());

        if (userRepository.findByUsername(userRegisterDTO.getUsername()).isPresent()) {
            logger.warn("用戶名已存在: {}", userRegisterDTO.getUsername());
            throw new ValidationException("用戶名已存在");
        }

        if (userRegisterDTO.getPassword() == null || userRegisterDTO.getPassword().length() < 6) {
            logger.warn("密碼長度不足: {}", userRegisterDTO.getUsername());
            throw new ValidationException("密碼長度必須至少為6位");
        }

        if (userRegisterDTO.getEmail() == null || !userRegisterDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            logger.warn("無效的電子郵件地址: {}", userRegisterDTO.getEmail());
            throw new ValidationException("請輸入有效的電子郵件地址");
        }

        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        user.setEmail(userRegisterDTO.getEmail());
        user.setRole(Role.REVIEWER);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        logger.info("用戶註冊成功: {}", user.getUsername());
        return convertToDto(savedUser);
    }

    // 識別用戶
    public String authenticateUser(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            User user = (User) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(user);
            return token;
        } catch (BadCredentialsException e) {
            throw new ValidationException("用戶名或密碼錯誤");
        } catch (Exception e) {
            throw new ValidationException("登錄失敗：" + e.getMessage());
        }
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該用戶，帳號：" + username));
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    // 更新用戶資料
    public UserDTO updateUserProfile(Long userId, UserUpdateDTO userUpdateDTO) {
        logger.info("嘗試更新用戶資料: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該用戶"));

        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            logger.warn("無效的電子郵件地址: {}", userUpdateDTO.getEmail());
            throw new ValidationException("請輸入有效的電子郵件地址");
        }

        user.setEmail(userUpdateDTO.getEmail());
        user.setProfilePicture(userUpdateDTO.getProfilePicture());

        User updatedUser = userRepository.save(user);
        logger.info("用戶資料更新成功: {}", userId);
        return new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                updatedUser.getProfilePicture());
    }

    // 用戶變換密碼
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        logger.info("嘗試修改用戶密碼: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該用戶"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.warn("舊密碼不正確: {}", userId);
            throw new ValidationException("舊的密碼不正確");
        }

        if (newPassword == null || newPassword.length() < 6) {
            logger.warn("新密碼長度不足: {}", userId);
            throw new ValidationException("新密碼長度必須至少為6位");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("密碼修改成功: {}", userId);
    }

    // 獲取用戶資訊
    public User getUserProfile(Long userId) {
        logger.debug("獲取用戶資料: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該用戶"));
    }

    // 獲取用戶收藏餐廳
    public List<RestaurantResponse> getUserFavoriteRestaurants(Long userId) {
        logger.debug("獲取用戶收藏的餐廳: {}", userId);
        List<Restaurant> restaurants = userRestaurantRepository.findByUserId(userId)
                .stream()
                .map(UserRestaurant::getRestaurant)
                .collect(Collectors.toList());

        return restaurants.stream().map(restaurant -> {
            RestaurantResponse response = new RestaurantResponse();
            response.setId(restaurant.getId());
            response.setName(restaurant.getName());
            response.setAddress(restaurant.getAddress());
            response.setPhone(restaurant.getPhone());
            response.setCategory(restaurant.getCategory());
            response.setDescription(restaurant.getDescription());
            response.setCreatedByUsername(restaurant.getCreatedByUsername());
            response.setImageUrl(restaurant.getImageUrl());

            // 計算平均評分
            response.setAverageRating(restaurant.getReviews().isEmpty() ? 0.0
                    : restaurant.getReviews().stream().mapToInt(Review::getRating).average().orElse(0.0));

            // 設定評論數
            response.setReviewCount(restaurant.getReviews() == null ? 0 : restaurant.getReviews().size());

            // 設定評論列表 - 轉換成 ReviewDTO
            response.setReviews(restaurant.getReviews().stream()
                    .map(review -> toReviewDTO(review, userId)) // 使用 toReviewDTO 方法轉換
                    .collect(Collectors.toList()));

            return response;
        }).collect(Collectors.toList());
    }

    // 獲取用戶評論
    public List<ReviewDTO> getUserReviews(Long userId) {
        logger.debug("獲取用戶的評論: {}", userId);
        return reviewRepository.findReviewsWithDetailsByUserId(userId); // 使用相同的 userId 作為 currentUserId
    }

    // 獲取用戶創建的餐廳
    public List<RestaurantResponse> getUserCreatedRestaurants(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<Restaurant> userRestaurants = restaurantRepository.findByCreatedByUsername(user.getUsername());

        return userRestaurants.stream().map(restaurant -> {
            RestaurantResponse response = new RestaurantResponse();
            response.setId(restaurant.getId());
            response.setName(restaurant.getName());
            response.setAddress(restaurant.getAddress());
            response.setPhone(restaurant.getPhone());
            response.setCategory(restaurant.getCategory());
            response.setDescription(restaurant.getDescription());
            response.setCreatedByUsername(restaurant.getCreatedByUsername());
            response.setImageUrl(restaurant.getImageUrl());

            // 計算平均評分
            response.setAverageRating(restaurant.getReviews().isEmpty() ? 0.0
                    : restaurant.getReviews().stream().mapToInt(Review::getRating).average().orElse(0.0));

            // 設定評論數
            response.setReviewCount(restaurant.getReviews() == null ? 0 : restaurant.getReviews().size());

            // 設定評論列表 - 轉換成 ReviewDTO
            response.setReviews(restaurant.getReviews().stream()
                    .map(review -> toReviewDTO(review, userId)) // 使用 toReviewDTO 方法轉換
                    .collect(Collectors.toList()));

            return response;
        }).collect(Collectors.toList());
    }

    // **轉換方法**
    private UserDTO convertToDto(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    private ReviewDTO toReviewDTO(Review review, Long currentUserId) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setCreated_At(review.getCreated_At());
        dto.setUpdated_At(review.getUpdated_At());
        dto.setImageUrl(review.getImageUrl());

        // 設置用戶資訊
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setUserRole(review.getUser().getRole());

        // 設置餐廳資訊
        dto.setRestaurantId(review.getRestaurant().getId());
        dto.setRestaurantName(review.getRestaurant().getName());

        // 設置點讚資訊
        dto.setLikeCount(review.getLikes().size());
        dto.setIsLiked(review.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(currentUserId)));

        return dto;
    }
}
