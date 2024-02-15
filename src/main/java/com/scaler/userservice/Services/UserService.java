package com.scaler.userservice.Services;

import com.scaler.userservice.DTOs.UserResponseDto;
import com.scaler.userservice.Models.Role;
import com.scaler.userservice.Models.User;
import com.scaler.userservice.Respositories.RoleRepository;
import com.scaler.userservice.Respositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public UserResponseDto getUserDetails(UUID userId) {
        UserResponseDto response = new UserResponseDto();
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) return null;
        User user = optUser.get();
        response.setEmail(user.getUsername());
        response.setRoles(user.getRoles());
        return response;
    }

    public UserResponseDto setUserRoles(UUID userId, List<UUID> roleIds) {
        Optional<User> optUser = userRepository.findById(userId);
        List<Role> rolesList = roleRepository.findAllById(roleIds);

        if (optUser.isEmpty()) return null;

        User user = optUser.get();
        Set<Role> roleSet = new HashSet<>();
        boolean b = roleSet.addAll(rolesList);
        if(b) user.setRoles(roleSet);

        User savedUser = userRepository.save(user);
        UserResponseDto response = new UserResponseDto();
        response.setEmail(savedUser.getUsername());
        response.setRoles(savedUser.getRoles());
        return response;
    }
}
