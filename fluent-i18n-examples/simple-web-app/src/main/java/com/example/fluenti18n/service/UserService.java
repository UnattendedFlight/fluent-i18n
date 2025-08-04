package com.example.fluenti18n.service;

import com.example.fluenti18n.model.User;
import io.github.unattendedflight.fluent.i18n.I18n;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final Map<Long, User> users = new HashMap<>();
    
    public UserService() {
        // Initialize with some test users
        users.put(1L, new User(1L, "John Doe", "john@example.com"));
        users.put(2L, new User(2L, "Jane Smith", "jane@example.com"));
    }
    
    public User findUser(Long id) {
        log.info(I18n.translate("Looking up user with ID: {}", id));
        
        User user = users.get(id);
        if (user == null) {
            log.warn(I18n.translate("User with ID {} was not found", id));
        } else {
            log.info(I18n.translate("Found user: {}", user.getName()));
        }
        
        return user;
    }
    
    public User createUser(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                I18n.translate("User name is required"));
        }
        
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException(
                I18n.translate("Please provide a valid email address"));
        }
        
        Long id = (long) (users.size() + 1);
        User user = new User(id, name, email);
        users.put(id, user);
        
        log.info(I18n.translate("Created new user: {} with email {}", name, email));
        return user;
    }
    
    public int getUserCount() {
        return users.size();
    }
    
    public String validateUser(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return I18n.translate("Name field cannot be empty");
        }
        
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            return I18n.translate("Email must be a valid email address");
        }
        
        if (user.getName().length() < 2) {
            return I18n.translate("Name must be at least 2 characters long");
        }
        
        return I18n.translate("User validation successful");
    }
}