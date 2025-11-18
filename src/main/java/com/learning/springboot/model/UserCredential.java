package com.learning.springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * UserCredential Entity - Stores authentication credentials
 * Separate from User profile for better security separation
 * Similar to credential storage in enterprise systems
 */
@Entity
@Table(name = "user_credentials")
public class UserCredential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @JsonIgnore  // Never expose in any JSON response
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;
    
    @Column(nullable = false, length = 20)
    private String algorithm = "BCrypt";  // Track which algorithm was used
    
    @Column(name = "last_changed")
    private LocalDateTime lastChanged;
    
    @Column(name = "must_change_password")
    private Boolean mustChangePassword = false;
    
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-one relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;
    
    // Constructors
    public UserCredential() {}
    
    public UserCredential(Long userId, String passwordHash) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.algorithm = "BCrypt";
        this.lastChanged = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime time = LocalDateTime.now();
        createdAt = time;
        updatedAt = time;
        if (lastChanged == null) {
            lastChanged = time;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
    
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            // Lock account for 30 minutes after 5 failed attempts
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }
    
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.lastChanged = LocalDateTime.now();
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(LocalDateTime lastChanged) {
        this.lastChanged = lastChanged;
    }

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}


