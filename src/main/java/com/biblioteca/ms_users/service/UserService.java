package com.biblioteca.ms_users.service;

import com.biblioteca.ms_users.exception.ResourceNotFoundException;
import com.biblioteca.ms_users.model.User;
import com.biblioteca.ms_users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Error: El correo electrónico ya está registrado.");
        }

        return userRepository.save(user);
    }
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no existe."));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}