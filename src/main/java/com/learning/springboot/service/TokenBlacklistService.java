package com.learning.springboot.service;

import com.learning.springboot.model.TokenBlacklist;
import com.learning.springboot.repository.TokenBlacklistRepository;
import com.learning.springboot.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Token Blacklist Service
 * Manages token revocation for logout functionality
 */
@Service
public class TokenBlacklistService {
    
    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Blacklist a token (logout)
     */
    @Transactional
    public void blacklistToken(String token) {
        // Extract username and expiration from token
        String username = jwtUtil.extractUsername(token);
        Date expirationDate = jwtUtil.extractExpiration(token);
        
        // Convert Date to LocalDateTime
        LocalDateTime expirationTime = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        // Create and save blacklist entry
        TokenBlacklist blacklistEntry = new TokenBlacklist(token, username, expirationTime);
        tokenBlacklistRepository.save(blacklistEntry);
    }
    
    /**
     * Check if a token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
    
    /**
     * Logout from all devices (blacklist all user's tokens)
     * Note: This only works if you track all issued tokens
     * For simplicity, this deletes existing blacklist entries for the user
     */
    @Transactional
    public void logoutAllDevices(String username) {
        tokenBlacklistRepository.deleteByUsername(username);
    }
    
    /**
     * Clean up expired tokens from blacklist
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredTokens() {
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}

