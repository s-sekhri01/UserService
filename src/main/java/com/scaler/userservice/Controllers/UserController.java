package com.scaler.userservice.Controllers;

import com.scaler.userservice.DTOs.SetRolesRequestDto;
import com.scaler.userservice.DTOs.UserResponseDto;
import com.scaler.userservice.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDto> getUserDetails(@PathVariable("id") String userId) {
        UserResponseDto userResponseDto = userService.getUserDetails(UUID.fromString(userId));
        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }

    @PostMapping("/roles/{id}")
    public ResponseEntity<UserResponseDto> setUserRoles(@PathVariable("id") String userId, @RequestBody SetRolesRequestDto request) {
        UserResponseDto userResponseDto = userService.setUserRoles(UUID.fromString(userId), request.getRoleIds());
        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }
}
