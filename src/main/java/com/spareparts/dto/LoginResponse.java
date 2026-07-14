package com.spareparts.dto;

import com.spareparts.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String accessToken;   // renamed from token to match AuthController
    private String tokenType = "Bearer";
    private Long expiresIn;
}
