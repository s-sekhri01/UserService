package com.scaler.userservice.DTOs;

import com.scaler.userservice.Models.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class SignupResponseDTO {
    private UUID userId;
    private String email;
    private Set<Role> roles = new HashSet<>();
}
