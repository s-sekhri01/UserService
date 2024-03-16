package com.scaler.userservice.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.userservice.Clients.KafkaProducerClient;
import com.scaler.userservice.DTOs.LoginResponseDTO;
import com.scaler.userservice.DTOs.SendEmailMessageDTO;
import com.scaler.userservice.DTOs.SignupResponseDTO;
import com.scaler.userservice.Models.Session;
import com.scaler.userservice.Models.SessionStatus;
import com.scaler.userservice.Models.User;
import com.scaler.userservice.Respositories.SessionRepository;
import com.scaler.userservice.Respositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private SecretKey secretKey;
    private KafkaProducerClient kafkaProducerClient;
    private ObjectMapper objectMapper;

    @Autowired
    public AuthService(UserRepository userRepository,
                       SessionRepository sessionRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       KafkaProducerClient kafkaProducerClient,
                       ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.kafkaProducerClient = kafkaProducerClient;
        this.objectMapper = objectMapper;
        secretKey = Jwts.SIG.HS256.key().build();
    }


    public ResponseEntity<LoginResponseDTO> login(String email, String password) {
        Optional<User> optUser = userRepository.findByUsername(email);
        if (optUser.isEmpty()) return null;

        User user = optUser.get();
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Password / username does not match");
        }
//        String token = RandomStringUtils.randomAlphanumeric(30);

        Map<String, Object> jwtData = new HashMap<>();
        jwtData.put("email", email);
        jwtData.put("createdAt", new Date());
        jwtData.put("expiryAt", new Date(LocalDate.now().plusDays(3).toEpochDay()));

        String token = Jwts
                .builder()
                .claims(jwtData)
                .signWith(secretKey)
                .compact();

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
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + token);

        return new ResponseEntity<LoginResponseDTO>(response, headers, HttpStatus.OK);
    }

    public void logout(String token, UUID userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) return;
        Optional<Session> optSession = sessionRepository.findByToken(token);
        if (optSession.isEmpty()) return;
        Session session = optSession.get();
        session.setStatus(SessionStatus.ENDED);
        session.setExpiryAt(new Date());
        sessionRepository.save(session);
    }

    public ResponseEntity<SignupResponseDTO> signup(String email, String password) throws JsonProcessingException {
        User user = new User();
        user.setUsername(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        User savedUser = userRepository.save(user);
        SendEmailMessageDTO sendEmailMessageDTO = new SendEmailMessageDTO();
        sendEmailMessageDTO.setTo(savedUser.getUsername());
        sendEmailMessageDTO.setFrom("saranshsekhri@gmail.com");
        sendEmailMessageDTO.setSubject("Welcome");
        sendEmailMessageDTO.setBody("Welcome to Ecommerce Platform");
        kafkaProducerClient.sendMessage("sendEmail",
                objectMapper.writeValueAsString(sendEmailMessageDTO));

        SignupResponseDTO response = new SignupResponseDTO();
        response.setUserId(user.getUuid());
        response.setEmail(user.getUsername());
        response.setRoles(user.getRoles());
        return new ResponseEntity<SignupResponseDTO>(response, HttpStatus.OK);
    }

    public SessionStatus validate(UUID userId, String token) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) return SessionStatus.ENDED;
        Optional<Session> optSession = sessionRepository.findByToken(token);
        if (optSession.isEmpty()) return SessionStatus.ENDED;
        Session session = optSession.get();

        //Write logic for JWT verification
        Jws<Claims> claimsJws = Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);

        // if a signature exception has occured then do not trust the token and return the session has ended.
        // also if the current date is greated than expiry at. return ended.
        // while calling from productservice session status would not be enough, rather return whole session object and JWT token with roles and other details
        return session.getStatus();
    }
}
