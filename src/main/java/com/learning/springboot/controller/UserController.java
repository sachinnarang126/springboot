package com.learning.springboot.controller;

import com.learning.springboot.model.User;
import com.learning.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller - This is like your ViewModel + API interface combined
 * @RestController = @Controller + @ResponseBody (auto-converts to JSON)
 * @RequestMapping sets the base path for all endpoints in this controller
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * Dependency Injection - Just like @Inject in Dagger/Hilt
     * Spring automatically injects the UserService instance
     */
    @Autowired
    private UserService userService;

    /**
     * GET /api/users
     * Returns all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /api/users/{id}
     * Returns specific user
     * @PathVariable is like route parameters in Navigation Component
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/users
     * Creates new user
     * @RequestBody automatically deserializes JSON to User object
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/users/{id}
     * Updates existing user
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/users/{id}
     * Deletes user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
