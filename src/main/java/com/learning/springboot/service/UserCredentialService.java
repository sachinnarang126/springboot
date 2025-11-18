package com.learning.springboot.service;

import com.learning.springboot.model.UserCredential;
import com.learning.springboot.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UserCredential Service
 * Handles all credential-related operations (passwords, lockouts, etc.)
 */
@Service
public class UserCredentialService {

    @Autowired
    private UserCredentialRepository credentialRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Create credentials for a user
     */
    @Transactional
    public void createCredential(Long userId, String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        UserCredential credential = new UserCredential(userId, hashedPassword);
        credentialRepository.save(credential);
    }

    /**
     * Get credentials by user ID
     */
    public Optional<UserCredential> getCredentialByUserId(Long userId) {
        return credentialRepository.findByUserId(userId);
    }

    /**
     * Verify password and handle failed attempts
     */
    @Transactional
    public boolean verifyPassword(Long userId, String plainPassword) {
        Optional<UserCredential> credentialOpt = getCredentialByUserId(userId);
        if (credentialOpt.isEmpty()) {
            return false;
        }

        UserCredential credential = credentialOpt.get();

        // Check if account is locked
        if (credential.isLocked()) {
            return false;
        }

        boolean matches = passwordEncoder.matches(plainPassword, credential.getPasswordHash());

        if (matches) {
            credential.resetFailedAttempts();
            credentialRepository.save(credential);
        } else {
            credential.incrementFailedAttempts();
            credentialRepository.save(credential);
        }

        return matches;
    }

    /**
     * Update password
     */
    @Transactional
    public boolean updatePassword(Long userId, String newPassword) {
        Optional<UserCredential> credentialOpt = getCredentialByUserId(userId);
        if (credentialOpt.isEmpty()) {
            return false;
        }

        UserCredential credential = credentialOpt.get();
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        credential.setMustChangePassword(false);
        credentialRepository.save(credential);
        return true;
    }

    /**
     * Delete credentials
     */
    @Transactional
    public void deleteCredential(Long userId) {
        credentialRepository.deleteByUserId(userId);
    }

    /**
     * Check if account is locked
     */
    public boolean isAccountLocked(Long userId) {
        return getCredentialByUserId(userId)
                .map(UserCredential::isLocked)
                .orElse(false);
    }

    /**
     * Get password hash directly (for Spring Security)
     */
    public String getPasswordHash(Long userId) {
        return getCredentialByUserId(userId)
                .map(UserCredential::getPasswordHash)
                .orElse(null);
    }

    @Transactional
    public void updateFailedLoginAttempt(Long userId) {
        Optional<UserCredential> credentialOpt = getCredentialByUserId(userId);
        if (credentialOpt.isEmpty()) return;

        UserCredential credential = credentialOpt.get();
        credential.incrementFailedAttempts();
        credentialRepository.save(credential);
    }

    @Transactional
    public void resetFailedLoginAttempts(Long userId) {
        Optional<UserCredential> credentialOpt = getCredentialByUserId(userId);
        if (credentialOpt.isEmpty()) return;

        UserCredential credential = credentialOpt.get();
        credential.resetFailedAttempts();
        credentialRepository.save(credential);
    }
}

