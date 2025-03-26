package com.blog.dto;

import com.blog.model.Role;

/**
 * Class Name: UserDTO
 * Package: dto
 * Description:
 * author:
 * Create: 2025/1/21
 * Version: 1.0
 */
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Role roles;

    public UserDTO(Long id, String username, String email, Role roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public UserDTO() {

    }

    public UserDTO(Long id, String username, String email, String profilePicture) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRoles() {
        return roles;
    }

    public void setRoles(Role roles) {
        this.roles = roles;
    }
}
