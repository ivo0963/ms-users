package com.biblioteca.ms_users.service;

import com.biblioteca.ms_users.exception.ResourceNotFoundException;
import com.biblioteca.ms_users.model.User;
import com.biblioteca.ms_users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_retornaListaDeUsuarios() {
        User user = new User(1L, "ivo", "ivo@test.com", "1234", "ADMIN");

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> resultado = userService.getAllUsers();

        assertEquals(1, resultado.size());
        assertEquals("ivo", resultado.get(0).getUsername());
        assertEquals("ivo@test.com", resultado.get(0).getEmail());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_cuandoExiste_retornaUsuario() {
        User user = new User(1L, "ivo", "ivo@test.com", "1234", "ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User resultado = userService.getUserById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("ivo", resultado.getUsername());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_cuandoNoExiste_lanzaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(99L);
        });

        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void createUser_cuandoEmailNoExiste_guardaUsuario() {
        User user = new User(null, "ivo", "ivo@test.com", "1234", "ADMIN");
        User savedUser = new User(1L, "ivo", "ivo@test.com", "1234", "ADMIN");

        when(userRepository.findByEmail("ivo@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(savedUser);

        User resultado = userService.createUser(user);

        assertNotNull(resultado.getId());
        assertEquals("ivo", resultado.getUsername());
        assertEquals("ivo@test.com", resultado.getEmail());

        verify(userRepository, times(1)).findByEmail("ivo@test.com");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_cuandoEmailYaExiste_lanzaRuntimeException() {
        User user = new User(null, "ivo", "ivo@test.com", "1234", "ADMIN");
        User existingUser = new User(1L, "otro", "ivo@test.com", "1234", "USER");

        when(userRepository.findByEmail("ivo@test.com")).thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(user);
        });

        assertEquals("Error: El correo electrónico ya está registrado.", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("ivo@test.com");
        verify(userRepository, never()).save(any(User.class));
    }
}