package com.example.hackaton_1.service;

import com.example.hackaton_1.dto.Playthrough.PlaythroughRequestDTO;
import com.example.hackaton_1.dto.Playthrough.PlaythroughResponseDTO;
import com.example.hackaton_1.model.Playthrough;
import com.example.hackaton_1.model.PlaythroughStatus;
import com.example.hackaton_1.model.StoryNode;
import com.example.hackaton_1.model.User;
import com.example.hackaton_1.repository.PlaythroughRepository;
import com.example.hackaton_1.repository.StoryNodeRepository;
import com.example.hackaton_1.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class PlaythroughService {

    private final PlaythroughRepository playthroughRepository;
    private final StoryNodeRepository storyNodeRepository;
    private final UserRepository userRepository;

    public PlaythroughService(
            PlaythroughRepository playthroughRepository,
            StoryNodeRepository storyNodeRepository,
            UserRepository userRepository
    ) {
        this.playthroughRepository = playthroughRepository;
        this.storyNodeRepository = storyNodeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PlaythroughResponseDTO createPlaythrough(
            PlaythroughRequestDTO dto
    ) {

        User user = getAuthenticatedUser();

        if (playthroughRepository.existsByPlayerTag(dto.getPlayerTag())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "playerTag already exists"
            );
        }

        StoryNode startNode = storyNodeRepository
                .findByNodeCode(dto.getStartNodeCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "StoryNode not found"
                ));

        /*
         * branchCapacity = 0 significa capacidad ilimitada.
         */
        if (startNode.getBranchCapacity() > 0
                && startNode.getCurrentBranches()
                >= startNode.getBranchCapacity()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "StoryNode has reached its branch capacity"
            );
        }

        Instant now = Instant.now();

        Playthrough playthrough = new Playthrough(
                dto.getPlayerTag(),
                user,
                startNode.getNodeCode(),
                startNode,
                100,
                0,
                PlaythroughStatus.ACTIVA,
                null,
                now,
                now
        );

        startNode.setCurrentBranches(
                startNode.getCurrentBranches() + 1
        );

        storyNodeRepository.save(startNode);

        Playthrough saved =
                playthroughRepository.save(playthrough);

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<PlaythroughResponseDTO> getMyPlaythroughs() {

        User user = getAuthenticatedUser();

        return playthroughRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaythroughResponseDTO getPlaythroughById(Long id) {

        User authenticatedUser = getAuthenticatedUser();

        Playthrough playthrough =
                playthroughRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Playthrough not found"
                                ));

        if (!isAdmin()
                && !playthrough.getUser().getId()
                .equals(authenticatedUser.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access this playthrough"
            );
        }

        return toDTO(playthrough);
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                authentication.getPrincipal()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));
    }

    private boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication != null
                && authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN"));
    }

    private PlaythroughResponseDTO toDTO(
            Playthrough playthrough
    ) {

        PlaythroughResponseDTO dto =
                new PlaythroughResponseDTO();

        dto.setId(playthrough.getId());
        dto.setPlayerTag(playthrough.getPlayerTag());
        dto.setOwnerEmail(
                playthrough.getUser().getEmail()
        );
        dto.setStartNodeCode(
                playthrough.getStartNodeCode()
        );
        dto.setCurrentNodeCode(
                playthrough.getCurrentNode().getNodeCode()
        );
        dto.setLucidity(playthrough.getLucidity());
        dto.setControlLevel(
                playthrough.getControlLevel()
        );
        dto.setStatus(
                playthrough.getStatus().name()
        );
        dto.setEndingCode(
                playthrough.getEndingCode()
        );
        dto.setCreatedAt(
                playthrough.getCreatedAt()
        );
        dto.setUpdatedAt(
                playthrough.getUpdatedAt()
        );

        return dto;
    }
}