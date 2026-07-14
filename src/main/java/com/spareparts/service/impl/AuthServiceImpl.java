package com.spareparts.service.impl;

import com.spareparts.dto.LoginRequest;
import com.spareparts.dto.LoginResponse;
import com.spareparts.entity.User;
import com.spareparts.exception.ResourceNotFoundException;
import com.spareparts.repository.UserRepository;
import com.spareparts.security.JwtUtil;
import com.spareparts.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String jwt = jwtUtil.generateToken(user);

            LoginResponse response = new LoginResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole());
            response.setAccessToken(jwt);    // fixed: was setToken()
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtUtil.getExpirationTime());

            log.info("User {} logged in successfully", user.getEmail());
            return response;
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    @Override
    public User register(LoginRequest registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());
        return savedUser;
    }

    @Override
    public String refreshToken(String token) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String username = jwtUtil.extractUsername(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return jwtUtil.generateToken(user);
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("User logged out");
    }
}
