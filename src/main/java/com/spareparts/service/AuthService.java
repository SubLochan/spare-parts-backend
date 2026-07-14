package com.spareparts.service;

import com.spareparts.dto.LoginRequest;
import com.spareparts.dto.LoginResponse;
import com.spareparts.entity.User;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    User register(LoginRequest registerRequest);
    String refreshToken(String token);
    User getCurrentUser();
    void logout();
}
