package com.scaler.userservice.Services;

import com.scaler.userservice.DTOs.LoginResponseDTO;
import com.scaler.userservice.DTOs.SignupResponseDTO;
import com.scaler.userservice.Models.Session;
import com.scaler.userservice.Models.SessionStatus;
import com.scaler.userservice.Models.User;
import com.scaler.userservice.Respositories.SessionRepository;
import com.scaler.userservice.Respositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;

    @Autowired
    public AuthService(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }


    public ResponseEntity<LoginResponseDTO> login(String email, String password) {
        Optional<User> optUser = userRepository.findByUsername(email);
        if (optUser.isEmpty()) return null;

        User user = optUser.get();
        String token = RandomStringUtils.randomAlphanumeric(30);

        Session session = new Session();
        session.setStatus(SessionStatus.ACTIVE);
        session.setUser(user);
        session.setToken(token);

        sessionRepository.save(session);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setUserId(user.getUuid());
        response.setEmail(user.getUsername());
        response.setRoles(user.getRoles());

        MultiValueMap<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token" + token);

        return new ResponseEntity<LoginResponseDTO>(response, headers, HttpStatus.OK);
    }

    public void logout(String token, UUID userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if(optUser.isEmpty()) return;
        Optional<Session> optSession = sessionRepository.findByToken(token);
        if (optSession.isEmpty()) return;
        Session session = optSession.get();
        session.setStatus(SessionStatus.ENDED);
        session.setExpiryAt(new Date());
        sessionRepository.save(session);
    }

    public ResponseEntity<SignupResponseDTO> signup(String email, String password) {
        User user = new User();
        user.setUsername(email);
        user.setPassword(password);
        User savedUser = userRepository.save(user);
        SignupResponseDTO response = new SignupResponseDTO();
        response.setUserId(user.getUuid());
        response.setEmail(user.getUsername());
        response.setRoles(user.getRoles());
        return new ResponseEntity<SignupResponseDTO>(response, HttpStatus.OK);
    }

    public SessionStatus validate(UUID userId, String token) {
        Optional<User> optUser = userRepository.findById(userId);
        if(optUser.isEmpty()) return SessionStatus.ENDED;
        Optional<Session> optSession = sessionRepository.findByToken(token);
        if (optSession.isEmpty()) return SessionStatus.ENDED;
        Session session = optSession.get();
        return session.getStatus();
    }
}
