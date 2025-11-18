package com.learning.springboot.service;

import com.learning.springboot.model.User;
import com.learning.springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service Layer - Now uses MySQL database via JPA Repository
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all users from database
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID from database
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Create new user in database
     */
    public User createUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Update existing user in database
     */
    public Optional<User> updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(updatedUser.getName());
                    existingUser.setEmail(updatedUser.getEmail());
                    return userRepository.save(existingUser);
                });
    }

    /**
     * Delete user from database
     */
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
