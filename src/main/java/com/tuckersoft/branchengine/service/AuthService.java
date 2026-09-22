package com.tuckersoft.branchengine.service;


import com.tuckersoft.branchengine.dto.AuthResponseDTO;
import com.tuckersoft.branchengine.dto.LoginRequestDTO;
import com.tuckersoft.branchengine.dto.RegisterRequestDTO;
import com.tuckersoft.branchengine.exceptions.ConflictException;
import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.repository.UserRepository;
import com.tuckersoft.branchengine.security.JwtService;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();

        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setDisplayName(request.getDisplayName());
        user.setRole("ROLE_USER");
        user.setCreatedAt(Instant.now());

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponseDTO(
                token,
                "Bearer",
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponseDTO(
                token,
                "Bearer",
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }
}