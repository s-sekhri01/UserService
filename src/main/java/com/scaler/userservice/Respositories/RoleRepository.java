package com.scaler.userservice.Respositories;

import com.scaler.userservice.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}
