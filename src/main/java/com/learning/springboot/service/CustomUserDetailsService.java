package com.learning.springboot.service;

import com.learning.springboot.exceptions.AccountLockException;
import com.learning.springboot.exceptions.PasswordMisMatchException;
import com.learning.springboot.model.User;
import com.learning.springboot.model.UserCredential;
import com.learning.springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Custom UserDetailsService - Uses separate credentials table
 * Loads user profile and credentials from different tables
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserCredentialService credentialService;

    /**
     * Load user by username from database
     * Loads profile from users table and credentials from user_credentials table
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // Find user profile
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        // Get credentials from separate table
        UserCredential credential = credentialService.getCredentialByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        // Check if account is locked
        if (credential.isLocked()) {
            throw new AccountLockException("locked");
        }

        // Return Spring Security UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(credential.getPasswordHash())
                .authorities(new ArrayList<>())
                .accountLocked(credential.isLocked())
                .build();
    }

    /**
     * Register new user with credentials in separate tables
     */
    @Transactional
    public void registerUser(String username, String password, String email, String name) {
        // Create user profile (no password here!)
        User user = new User(username, email, name);
        user = userRepository.save(user);
        
        // Create credentials in separate table
        credentialService.createCredential(user.getId(), password);
    }

    /**
     * Check if username exists in database
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Check if email exists in database
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean updatePassword(String userName, String oldPassword, String newPassword) throws UsernameNotFoundException, PasswordMisMatchException  {
        User user =  userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userName));
        boolean matches = credentialService.verifyPassword(user.getId(), oldPassword);
        if (!matches) throw new PasswordMisMatchException();
        return credentialService.updatePassword(user.getId(), newPassword);
    }

    public void recordFailedAttempt(String userName) {
        Optional<User> user = userRepository.findByUsername(userName);
        user.ifPresent(value -> credentialService.updateFailedLoginAttempt(value.getId()));
    }

    public void resetFailedAttempts(String userName) {
        Optional<User> user = userRepository.findByUsername(userName);
        user.ifPresent(value -> credentialService.resetFailedLoginAttempts(value.getId()));
    }
}

