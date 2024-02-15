package com.scaler.userservice.DTOs;

import com.scaler.userservice.Models.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
public class UserResponseDto {
    private String email;
    private Set<Role> roles = new HashSet<>();
}
