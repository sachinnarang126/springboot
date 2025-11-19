package com.learning.springboot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * TokenBlacklist Entity
 * Stores invalidated JWT tokens (for logout functionality)
 * Tokens are kept until their expiration time
 */
@Entity
@Table(name = "token_blacklist", indexes = {
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_expiration", columnList = "expiration_time")
})
public class TokenBlacklist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 512, unique = true)
    private String token;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blacklistedAt;
    
    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;
    
    // Constructors
    public TokenBlacklist() {}
    
    public TokenBlacklist(String token, String username, LocalDateTime expirationTime) {
        this.token = token;
        this.username = username;
        this.blacklistedAt = LocalDateTime.now();
        this.expirationTime = expirationTime;
    }
    
    @PrePersist
    protected void onCreate() {
        if (blacklistedAt == null) {
            blacklistedAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getBlacklistedAt() {
        return blacklistedAt;
    }

    public void setBlacklistedAt(LocalDateTime blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}

