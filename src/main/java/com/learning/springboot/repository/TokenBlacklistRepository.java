package com.learning.springboot.repository;

import com.learning.springboot.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * TokenBlacklist Repository
 * Manages blacklisted JWT tokens
 */
@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    
    /**
     * Check if a token is blacklisted
     */
    boolean existsByToken(String token);
    
    /**
     * Delete expired tokens from blacklist
     * Called periodically to clean up old entries
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist t WHERE t.expirationTime < ?1")
    void deleteExpiredTokens(LocalDateTime now);
    
    /**
     * Delete all tokens for a specific user
     * Useful for logout from all devices
     */
    @Modifying
    @Transactional
    void deleteByUsername(String username);
}

