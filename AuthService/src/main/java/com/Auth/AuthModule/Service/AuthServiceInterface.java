package com.Auth.AuthModule.Service;

import com.Auth.AuthModule.DTO.Request.LoginRequest;
import com.Auth.AuthModule.DTO.Request.RefreshTokenRequest;
import com.Auth.AuthModule.DTO.Request.RegisterRequest;
import com.Auth.AuthModule.DTO.Response.RegisterResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthServiceInterface {

    public RegisterResponse register(RegisterRequest registerRequest);

    public RegisterResponse login(LoginRequest loginRequest);

    RegisterResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(RefreshTokenRequest refreshTokenRequest);
}
