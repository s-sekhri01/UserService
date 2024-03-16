package com.scaler.userservice.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scaler.userservice.DTOs.*;
import com.scaler.userservice.Models.SessionStatus;
import com.scaler.userservice.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("login")
    private ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/logout")
    private ResponseEntity<Void> logout(@RequestBody LogoutRequestDTO request) {
        authService.logout(request.getToken(), request.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    private ResponseEntity<SignupResponseDTO> signup(@RequestBody SignupRequestDTO request) throws JsonProcessingException {
        return authService.signup(request.getEmail(), request.getPassword());
    }

    @PostMapping("validate")
    private ResponseEntity<SessionStatus> validateToken(@RequestBody ValidateRequestDTO request) {
        SessionStatus sessionStatus = authService.validate(request.getUserId(), request.getToken());
        return new ResponseEntity<>(sessionStatus, HttpStatus.OK);
    }
}
