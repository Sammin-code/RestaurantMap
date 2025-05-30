package com.blog.dto;

/**
 * Class Name: UserUpdateDTO
 * Package: dto
 * Description:
 * author:
 * Create: 2025/2/8
 * Version: 1.0
 */
public class UserUpdateDTO {
    private String email;
    private String profilePicture;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
