package com.jobboard.admin.dto;

import com.jobboard.user.Role;
import com.jobboard.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

    public static UserResponse from(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
