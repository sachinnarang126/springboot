package com.learning.springboot.repository;

import com.learning.springboot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JpaRepository provides CRUD methods automatically
 * No need to write SQL queries!
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Data JPA automatically implements these methods
    // Just by the method name, it knows what SQL to generate
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Built-in methods from JpaRepository:
    // - findAll()           → SELECT * FROM users
    // - findById(id)        → SELECT * FROM users WHERE id = ?
    // - save(user)          → INSERT or UPDATE
    // - deleteById(id)      → DELETE FROM users WHERE id = ?
    // - count()             → SELECT COUNT(*) FROM users
}

