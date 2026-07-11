package com.biblioteca.ms_users.controller;

import com.biblioteca.ms_users.model.User;
import com.biblioteca.ms_users.model.dto.UserDTO;
import com.biblioteca.ms_users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios", description = "Endpoints para la gestión, registro y consulta de usuarios del sistema")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Listar todos los usuarios", description = "Obtiene una lista completa de todos los usuarios registrados en el sistema")
    public CollectionModel<EntityModel<User>> getAll() {
        List<EntityModel<User>> users = userService.getAllUsers()
                .stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).getAll()).withSelfRel()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Busca y devuelve los detalles de un usuario específico usando su ID")
    public ResponseEntity<EntityModel<User>> getById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(toModel(user));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario", description = "Registra un nuevo usuario en la plataforma validando sus datos y que el email no esté duplicado")
    public ResponseEntity<EntityModel<User>> create(@Valid @RequestBody UserDTO dto) {

        // Mapeo manual del DTO a la Entidad
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());

        User createdUser = userService.createUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toModel(createdUser));
    }

    private EntityModel<User> toModel(User user) {
        return EntityModel.of(
                user,
                linkTo(methodOn(UserController.class).getById(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAll()).withRel("todos-los-usuarios")
        );
    }
}