package com.blog.repository;

import com.blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Class Name: UserRepository
 * Package: repository
 * Description:
 * author:
 * Create: 2025/1/6
 * Version: 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
