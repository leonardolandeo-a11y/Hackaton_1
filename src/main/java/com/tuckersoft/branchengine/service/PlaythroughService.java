package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.Playthrough.PlaythroughPathResponseDTO;
import com.tuckersoft.branchengine.dto.Playthrough.PlaythroughPathStepDTO;
import com.tuckersoft.branchengine.dto.Playthrough.PlaythroughRequestDTO;
import com.tuckersoft.branchengine.dto.Playthrough.PlaythroughResponseDTO;
import com.tuckersoft.branchengine.entity.Decision;
import com.tuckersoft.branchengine.entity.Playthrough;
import com.tuckersoft.branchengine.entity.PlaythroughStatus;
import com.tuckersoft.branchengine.entity.StoryNode;
import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.exceptions.ConflictException;
import com.tuckersoft.branchengine.exceptions.ForbiddenException;
import com.tuckersoft.branchengine.exceptions.ResourceNotFoundException;
import com.tuckersoft.branchengine.repository.DecisionRepository;
import com.tuckersoft.branchengine.repository.PlaythroughRepository;
import com.tuckersoft.branchengine.repository.StoryNodeRepository;
import com.tuckersoft.branchengine.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlaythroughService {

    private final PlaythroughRepository playthroughRepository;
    private final StoryNodeRepository storyNodeRepository;
    private final UserRepository userRepository;
    private final DecisionRepository decisionRepository;

    public PlaythroughService(
            PlaythroughRepository playthroughRepository,
            StoryNodeRepository storyNodeRepository,
            UserRepository userRepository,
            DecisionRepository decisionRepository
    ) {
        this.playthroughRepository = playthroughRepository;
        this.storyNodeRepository = storyNodeRepository;
        this.userRepository = userRepository;
        this.decisionRepository = decisionRepository;
    }

    @Transactional
    public PlaythroughResponseDTO createPlaythrough(
            PlaythroughRequestDTO request,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        StoryNode startNode = storyNodeRepository
                .findByNodeCode(request.getStartNodeCode())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "StoryNode not found: "
                                        + request.getStartNodeCode()
                        )
                );

        if (playthroughRepository.existsByPlayerTag(
                request.getPlayerTag()
        )) {
            throw new ConflictException(
                    "playerTag already exists"
            );
        }

        int currentBranches =
                startNode.getCurrentBranches() == null
                        ? 0
                        : startNode.getCurrentBranches();

        int capacity =
                startNode.getBranchCapacity() == null
                        ? 0
                        : startNode.getBranchCapacity();

        /*
         * ERRATA v1.3:
         * branchCapacity = 0 significa ilimitado.
         * Nodo lleno responde 409.
         */
        if (capacity > 0 && currentBranches >= capacity) {
            throw new ConflictException(
                    "StoryNode branch capacity is full"
            );
        }

        Instant now = Instant.now();

        Playthrough playthrough = new Playthrough(
                request.getPlayerTag(),
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
                currentBranches + 1
        );

        storyNodeRepository.save(startNode);

        Playthrough saved =
                playthroughRepository.save(playthrough);

        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<PlaythroughResponseDTO> getAllPlaythroughs(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        /*
         * ERRATA v1.3:
         * incluso ADMIN ve solamente sus propias partidas
         * en este listado.
         */
        return playthroughRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaythroughResponseDTO getPlaythroughById(
            Long id,
            Authentication authentication
    ) {

        Playthrough playthrough =
                findPlaythrough(id);

        validateReadPermission(
                playthrough,
                authentication
        );

        return toResponseDTO(playthrough);
    }

    @Transactional(readOnly = true)
    public PlaythroughPathResponseDTO getPath(
            Long id,
            Authentication authentication
    ) {

        Playthrough playthrough =
                findPlaythrough(id);

        validateReadPermission(
                playthrough,
                authentication
        );

        List<Decision> decisions =
                decisionRepository
                        .findByPlaythroughIdAndResolvedNodeCodeIsNotNullOrderByCreatedAtAsc(
                                id
                        );

        List<PlaythroughPathStepDTO> steps =
                new ArrayList<>();

        for (int i = 0; i < decisions.size(); i++) {

            Decision decision = decisions.get(i);

            steps.add(
                    new PlaythroughPathStepDTO(
                            i + 1,
                            decision.getId(),
                            decision.getNode().getNodeCode(),
                            decision.getResolvedNodeCode(),
                            decision.getBranchType(),
                            decision.getImpactLevel(),
                            decision.getCreatedAt()
                    )
            );
        }

        return new PlaythroughPathResponseDTO(
                playthrough.getId(),
                playthrough.getPlayerTag(),
                playthrough.getStatus().name(),
                playthrough.getEndingCode(),
                playthrough.getStartNodeCode(),
                playthrough.getCurrentNode().getNodeCode(),
                steps
        );
    }

    private Playthrough findPlaythrough(Long id) {

        return playthroughRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Playthrough not found"
                        )
                );
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        if (authentication == null
                || authentication.getName() == null) {
            throw new ForbiddenException(
                    "Authentication required"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private void validateReadPermission(
            Playthrough playthrough,
            Authentication authentication
    ) {

        boolean owner =
                playthrough.getUser()
                        .getEmail()
                        .equals(authentication.getName());

        boolean admin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                        );

        if (!owner && !admin) {
            throw new ForbiddenException(
                    "You do not have permission to access this playthrough"
            );
        }
    }

    private PlaythroughResponseDTO toResponseDTO(
            Playthrough playthrough
    ) {

        PlaythroughResponseDTO dto =
                new PlaythroughResponseDTO();

        dto.setId(playthrough.getId());
        dto.setPlayerTag(
                playthrough.getPlayerTag()
        );
        dto.setOwnerEmail(
                playthrough.getUser().getEmail()
        );
        dto.setStartNodeCode(
                playthrough.getStartNodeCode()
        );
        dto.setCurrentNodeCode(
                playthrough.getCurrentNode()
                        .getNodeCode()
        );
        dto.setLucidity(
                playthrough.getLucidity()
        );
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