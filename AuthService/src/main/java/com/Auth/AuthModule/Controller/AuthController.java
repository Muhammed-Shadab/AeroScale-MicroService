package com.Auth.AuthModule.Controller;



import com.Auth.AuthModule.DTO.Request.LoginRequest;
import com.Auth.AuthModule.DTO.Request.RefreshTokenRequest;
import com.Auth.AuthModule.DTO.Request.RegisterRequest;
import com.Auth.AuthModule.DTO.Response.RegisterResponse;
import com.Auth.AuthModule.Service.AuthServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceInterface authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<RegisterResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        RegisterResponse registerResponse = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<RegisterResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        RegisterResponse registerResponse = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        authService.logout(refreshTokenRequest);
        return ResponseEntity.noContent().build();
    }
}
