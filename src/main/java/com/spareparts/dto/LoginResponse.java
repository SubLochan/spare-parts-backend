package com.spareparts.dto;

import com.spareparts.entity.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
}
