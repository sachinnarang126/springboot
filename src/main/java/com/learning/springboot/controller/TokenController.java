package com.learning.springboot.controller;

import com.learning.springboot.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validate")
public class TokenController {

    @GetMapping("/token")
    public ResponseEntity<ApiResponse<Void>> validateToken() {
        // If this endpoint is reached, token is valid
        // (because of JWT filter)
        return ResponseEntity.ok(
                ApiResponse.success("Token is valid")
        );
    }
}
