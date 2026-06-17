package com.biblioteca.ms_users.controller;

import com.biblioteca.ms_users.model.User;
import com.biblioteca.ms_users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
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
    public ResponseEntity<EntityModel<User>> getById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(toModel(user));
    }

    @PostMapping
    public ResponseEntity<EntityModel<User>> create(@RequestBody User user) {
        User createdUser = userService.createUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toModel(createdUser));
    }

    private EntityModel<User> toModel(User user) {
        return EntityModel.of(
                user,
                linkTo(methodOn(UserController.class).getById(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAll()).withRel("usuarios")
        );
    }
}