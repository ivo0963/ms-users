package com.biblioteca.ms_users.controller;

import com.biblioteca.ms_users.exception.GlobalExceptionHandler;
import com.biblioteca.ms_users.exception.ResourceNotFoundException;
import com.biblioteca.ms_users.model.User;
import com.biblioteca.ms_users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getAll_retornaListaConLinksHateoas() throws Exception {
        User user = new User(1L, "ivo", "ivo@test.com", "1234", "ADMIN");

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._embedded.userList[0].id").value(1))
                .andExpect(jsonPath("$._embedded.userList[0].username").value("ivo"))
                .andExpect(jsonPath("$._embedded.userList[0]._links.self.href").exists());
    }

    @Test
    void getById_cuandoExiste_retornaUsuarioConLinksHateoas() throws Exception {
        User user = new User(1L, "ivo", "ivo@test.com", "1234", "ADMIN");

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("ivo"))
                .andExpect(jsonPath("$.email").value("ivo@test.com"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.usuarios.href").exists());
    }

    @Test
    void getById_cuandoNoExiste_retorna404() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("Usuario con ID 99 no existe."));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.message").value("Usuario con ID 99 no existe."));
    }

    @Test
    void create_cuandoUsuarioValido_retornaCreatedConLinksHateoas() throws Exception {
        User savedUser = new User(1L, "ivo", "ivo@test.com", "1234", "ADMIN");

        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        String json = """
                {
                    "username": "ivo",
                    "email": "ivo@test.com",
                    "password": "1234",
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("ivo"))
                .andExpect(jsonPath("$.email").value("ivo@test.com"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.usuarios.href").exists());
    }

    @Test
    void create_cuandoEmailDuplicado_retorna409() throws Exception {
        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("Error: El correo electrónico ya está registrado."));

        String json = """
                {
                    "username": "ivo",
                    "email": "ivo@test.com",
                    "password": "1234",
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.message").value("Error: El correo electrónico ya está registrado."));
    }
}