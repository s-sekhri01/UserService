package com.scaler.userservice.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SetRolesRequestDto {
    private List<UUID> roleIds;
}
