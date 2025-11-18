package com.learning.springboot.repository;

import com.learning.springboot.model.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserCredential Repository
 * Manages authentication credentials separately from user profiles
 */
@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    
    Optional<UserCredential> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}


