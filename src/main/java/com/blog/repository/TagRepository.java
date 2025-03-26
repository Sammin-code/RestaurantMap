package com.blog.repository;

import com.blog.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Class Name: TagRepository
 * Package: repository
 * Description:
 * author:
 * Create: 2025/1/6
 * Version: 1.0
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}
