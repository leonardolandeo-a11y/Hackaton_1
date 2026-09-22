package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.UserResponseDTO;
import com.tuckersoft.branchengine.exceptions.BadRequestException;
import com.tuckersoft.branchengine.exceptions.ResourceNotFoundException;
import com.tuckersoft.branchengine.model.User;
import com.tuckersoft.branchengine.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        return toDTO(user);
    }

    public List<UserResponseDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public UserResponseDTO updateRole(
            Long id,
            String role,
            String authenticatedEmail
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        if (user.getEmail().equals(authenticatedEmail)) {
            throw new BadRequestException(
                    "You cannot change your own role"
            );
        }

        user.setRole(role);

        userRepository.save(user);

        return toDTO(user);
    }

    private UserResponseDTO toDTO(User user) {

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}