package com.chedyProjects.TodoTeam.controller;

import com.chedyProjects.TodoTeam.dto.AuthDto;
import com.chedyProjects.TodoTeam.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthDto register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req.getEmail(), req.getPassword(), req.getUsername());
    }

    @PostMapping("/login")
    public AuthDto login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.getEmail(), req.getPassword());
    }

    @Data
    public static class RegisterRequest {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String username;
    }

    @Data
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }
}

