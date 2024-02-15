package com.scaler.userservice.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ValidateRequestDTO {
    private UUID userId;
    private String token;
}
