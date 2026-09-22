package com.tuckersoft.branchengine.controller;


import com.tuckersoft.branchengine.dto.RoleUpdateRequestDTO;
import com.tuckersoft.branchengine.dto.UserResponseDTO;
import com.tuckersoft.branchengine.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userService.getCurrentUser(
                        authentication.getName()
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequestDTO request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userService.updateRole(
                        id,
                        request.getRole(),
                        authentication.getName()
                )
        );
    }
}