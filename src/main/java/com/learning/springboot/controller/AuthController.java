package com.learning.springboot.controller;

import com.learning.springboot.exceptions.PasswordMisMatchException;
import com.learning.springboot.model.*;
import com.learning.springboot.service.CustomUserDetailsService;
import com.learning.springboot.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles login, registration, and token generation
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );
            
            // Reset failed login attempts on successful login
            userDetailsService.resetFailedAttempts(authRequest.getUsername());
            
            // Load user details
            final UserDetails userDetails = userDetailsService
                    .loadUserByUsername(authRequest.getUsername());

            // Generate JWT token
            final String jwt = jwtUtil.generateToken(userDetails);

            // Create auth response
            AuthResponse authResponse = new AuthResponse(
                    jwt,
                    authRequest.getUsername(),
                    "Login successful"
            );
            return ResponseEntity.ok(
                    ApiResponse.success(authResponse, "Login successful")
            );
        } catch (Exception e) {
            if (e.getMessage().equals("locked")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Account is locked due to too many failed login attempts. Please try again later.", HttpStatus.UNAUTHORIZED.value()));
            } else {
                // Record failed login attempt
                userDetailsService.recordFailedAttempt(authRequest.getUsername());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid username or password", HttpStatus.UNAUTHORIZED.value()));
            }
        }
    }

    /**
     * POST /api/auth/register
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest registerRequest) {

        try {
            // Check if username already exists
            if (userDetailsService.userExists(registerRequest.getUsername())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Username already exists", 409));
            }

            // Check if email already exists
            if (userDetailsService.emailExists(registerRequest.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Email already exists", 409));
            }

            if (registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Password can't be empty", 400));
            }

            if (registerRequest.getName() == null || registerRequest.getName().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Name can't be empty", 400));
            }

            // Register user with all fields
            userDetailsService.registerUser(
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.getEmail(),
                    registerRequest.getName()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", 201));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage(), 500));
        }
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        if (changePasswordRequest.getOldPassword() == null || changePasswordRequest.getOldPassword().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Old Password can't be empty", 400));
        }
        if (changePasswordRequest.getNewPassword() == null || changePasswordRequest.getNewPassword().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("New Password can't be empty", 400));
        }
        if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getOldPassword())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password can't be same", 400));
        }

        try {
            boolean isPasswordUpdated = userDetailsService.updatePassword(changePasswordRequest.getUsername(), changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
            if (isPasswordUpdated) return ResponseEntity.ok(
                    ApiResponse.success("Password updated successfully")
            ); else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Some error occurred", 400));
            }
        } catch (UsernameNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Invalid user name or password", 404));
        } catch (PasswordMisMatchException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid user name or password", 401));
        }
    }

    @GetMapping("/connected")
    public ResponseEntity<ApiResponse<Void>> validateToken() {
        // If this endpoint is reached, you're connected
        // (because of JWT filter)
        return ResponseEntity.ok(
                ApiResponse.success("You're connected")
        );
    }
}
