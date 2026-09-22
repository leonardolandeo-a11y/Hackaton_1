package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.Decision.DecisionPageResponse;
import com.tuckersoft.branchengine.dto.Decision.DecisionRequest;
import com.tuckersoft.branchengine.dto.Decision.DecisionResponse;
import com.tuckersoft.branchengine.dto.RealityLogResponse;
import com.tuckersoft.branchengine.event.DecisionCommittedEvent;
import com.tuckersoft.branchengine.model.Decision;
import com.tuckersoft.branchengine.model.Playthrough;
import com.tuckersoft.branchengine.model.RealityLog;
import com.tuckersoft.branchengine.model.StoryNode;
import com.tuckersoft.branchengine.repository.DecisionRepository;
import com.tuckersoft.branchengine.repository.PlaythroughRepository;
import com.tuckersoft.branchengine.repository.RealityLogRepository;
import com.tuckersoft.branchengine.repository.StoryNodeRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final RealityLogRepository realityLogRepository;
    private final PlaythroughRepository playthroughRepository;
    private final StoryNodeRepository storyNodeRepository;
    private final DecisionClassifier decisionClassifier;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.display-name}")
    private String adminDisplayName;

    public DecisionService(
            DecisionRepository decisionRepository,
            RealityLogRepository realityLogRepository,
            PlaythroughRepository playthroughRepository,
            StoryNodeRepository storyNodeRepository,
            DecisionClassifier decisionClassifier,
            ApplicationEventPublisher eventPublisher
    ) {
        this.decisionRepository = decisionRepository;
        this.realityLogRepository = realityLogRepository;
        this.playthroughRepository = playthroughRepository;
        this.storyNodeRepository = storyNodeRepository;
        this.decisionClassifier = decisionClassifier;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DecisionResponse createDecision(
            DecisionRequest request,
            Authentication authentication,
            String simulationHeader
    ) {

        Playthrough playthrough = playthroughRepository
                .findById(request.playthroughId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Playthrough no encontrado"
                ));

        validateWritePermission(playthrough, authentication);

        if ("FINALIZADA".equals(playthrough.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La partida ya esta finalizada"
            );
        }

        StoryNode sourceNode = playthrough.getCurrentNode();

        DecisionClassifier.ClassificationResult result =
                decisionClassifier.classify(request.rawInput());

        Instant now = Instant.now();

        Decision decision = new Decision();

        decision.setPlaythrough(playthrough);
        decision.setNode(sourceNode);
        decision.setRawInput(request.rawInput());
        decision.setBranchType(result.branchType());
        decision.setImpactLevel(request.impactLevel());
        decision.setHandlerUnit(result.handlerUnit());
        decision.setOutcomeCode(result.outcomeCode());
        decision.setCreatedAt(now);
        decision.setUpdatedAt(now);

        /*
         * ERRATA v1.3:
         * ENTRADA_CORRUPTA SI modifica stats.
         */
        applyStats(playthrough, request.impactLevel());

        if ("ENTRADA_CORRUPTA".equals(result.branchType())) {

            decision.setResolvedNodeCode(null);
            decision.setStatus("ERROR");

            /*
             * No se mueve currentNode y no se publica evento.
             * Pero los stats pueden provocar un final.
             */
            resolveStatsOnlyEnding(playthrough);

            playthrough.setUpdatedAt(now);

            playthroughRepository.save(playthrough);

            Decision saved = decisionRepository.save(decision);

            return toResponse(saved);
        }

        String destinationCode = resolveDestinationCode(
                sourceNode,
                result.branchType(),
                request.impactLevel()
        );

        decision.setResolvedNodeCode(destinationCode);

        resolvePlaythroughState(
                playthrough,
                destinationCode
        );

        playthrough.setUpdatedAt(now);

        playthroughRepository.save(playthrough);

        decision.setStatus("REGISTRADA");

        Decision saved = decisionRepository.save(decision);

        boolean simulateFailure =
                "MAIL_FAILURE".equals(simulationHeader);

        eventPublisher.publishEvent(
                new DecisionCommittedEvent(
                        saved.getId(),
                        adminEmail,
                        adminDisplayName,
                        playthrough.getPlayerTag(),
                        saved.getBranchType(),
                        saved.getImpactLevel(),
                        saved.getHandlerUnit(),
                        saved.getOutcomeCode(),
                        sourceNode.getNodeCode(),
                        saved.getResolvedNodeCode(),
                        playthrough.getStatus(),
                        playthrough.getLucidity(),
                        playthrough.getControlLevel(),
                        playthrough.getEndingCode(),
                        saved.getRawInput(),
                        saved.getCreatedAt(),
                        simulateFailure
                )
        );

        return toResponse(saved);
    }

    private void applyStats(
            Playthrough playthrough,
            String impactLevel
    ) {

        int lucidityLoss;
        int controlGain;

        switch (impactLevel) {

            case "LEVE" -> {
                lucidityLoss = 5;
                controlGain = 5;
            }

            case "MODERADO" -> {
                lucidityLoss = 15;
                controlGain = 10;
            }

            case "GRAVE" -> {
                lucidityLoss = 30;
                controlGain = 20;
            }

            case "CRITICO" -> {
                // ERRATA v1.3
                lucidityLoss = 45;
                controlGain = 40;
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "impactLevel invalido"
            );
        }

        playthrough.setLucidity(
                Math.max(
                        0,
                        playthrough.getLucidity() - lucidityLoss
                )
        );

        playthrough.setControlLevel(
                Math.min(
                        100,
                        playthrough.getControlLevel() + controlGain
                )
        );
    }

    private String resolveDestinationCode(
            StoryNode sourceNode,
            String branchType,
            String impactLevel
    ) {

        if (
                "RUPTURA_CUARTA_PARED".equals(branchType)
                        ||
                        "CRITICO".equals(impactLevel)
        ) {
            return sourceNode.getGlitchBranchCode();
        }

        return sourceNode.getPrimaryBranchCode();
    }

    private void resolveStatsOnlyEnding(
            Playthrough playthrough
    ) {

        // ERRATA v1.3: lucidity se evalua primero.
        if (playthrough.getLucidity() <= 0) {
            playthrough.setStatus("FINALIZADA");
            playthrough.setEndingCode(
                    "ENDING_WHITE_BEAR"
            );
            return;
        }

        if (playthrough.getControlLevel() >= 100) {
            playthrough.setStatus("FINALIZADA");
            playthrough.setEndingCode(
                    "ENDING_PAC_SYMBOL"
            );
        }
    }

    private void resolvePlaythroughState(
            Playthrough playthrough,
            String destinationCode
    ) {

        // ERRATA v1.3
        if (playthrough.getLucidity() <= 0) {

            playthrough.setStatus("FINALIZADA");
            playthrough.setEndingCode(
                    "ENDING_WHITE_BEAR"
            );

            return;
        }

        if (playthrough.getControlLevel() >= 100) {

            playthrough.setStatus("FINALIZADA");
            playthrough.setEndingCode(
                    "ENDING_PAC_SYMBOL"
            );

            return;
        }

        if (destinationCode == null) {

            playthrough.setStatus("FINALIZADA");
            playthrough.setEndingCode(
                    "ENDING_NETFLIX_CUT"
            );

            return;
        }

        StoryNode destination =
                storyNodeRepository
                        .findByNodeCode(destinationCode)
                        .orElse(null);

        if (destination == null) {

            playthrough.setStatus("FINALIZADA");
            playthrough.setEndingCode(
                    "ENDING_NETFLIX_CUT"
            );

            return;
        }

        playthrough.setCurrentNode(destination);
        playthrough.setStatus("ACTIVA");
        playthrough.setEndingCode(null);
    }

    private void validateWritePermission(
            Playthrough playthrough,
            Authentication authentication
    ) {

        boolean owner =
                playthrough.getUser()
                        .getEmail()
                        .equals(authentication.getName());

        boolean admin =
                isAdmin(authentication);

        /*
         * ERRATA v1.3:
         * ADMIN puede decidir sobre partidas ajenas.
         */
        if (!owner && !admin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso sobre esta partida"
            );
        }
    }

    private void validateReadPermission(
            Decision decision,
            Authentication authentication
    ) {

        boolean owner =
                decision.getPlaythrough()
                        .getUser()
                        .getEmail()
                        .equals(authentication.getName());

        if (!owner && !isAdmin(authentication)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para ver esta decision"
            );
        }
    }

    private boolean isAdmin(
            Authentication authentication
    ) {

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(
                                authority.getAuthority()
                        )
                );
    }

    @Transactional(readOnly = true)
    public DecisionResponse getById(
            Long id,
            Authentication authentication
    ) {

        Decision decision = decisionRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Decision no encontrada"
                ));

        validateReadPermission(
                decision,
                authentication
        );

        return toResponse(decision);
    }

    @Transactional(readOnly = true)
    public List<RealityLogResponse> getRealityLogs(
            Long decisionId,
            Authentication authentication
    ) {

        Decision decision = decisionRepository
                .findById(decisionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Decision no encontrada"
                ));

        validateReadPermission(
                decision,
                authentication
        );

        return realityLogRepository
                .findByDecisionIdOrderByCreatedAtAsc(decisionId)
                .stream()
                .map(this::toRealityLogResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DecisionPageResponse getAll(
            String branchType,
            String impactLevel,
            String status,
            Long playthroughId,
            int page,
            int size,
            Authentication authentication
    ) {

        if (page < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page debe empezar en 1"
            );
        }

        if (size < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "size debe ser mayor a 0"
            );
        }

        Specification<Decision> specification =
                Specification.where(null);

        if (branchType != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("branchType"),
                                    branchType
                            )
            );
        }

        if (impactLevel != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("impactLevel"),
                                    impactLevel
                            )
            );
        }

        if (status != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (playthroughId != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("playthrough")
                                            .get("id"),
                                    playthroughId
                            )
            );
        }

        /*
         * USER solamente ve decisiones de sus partidas.
         * ADMIN puede ver todas.
         */
        if (!isAdmin(authentication)) {

            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("playthrough")
                                            .get("user")
                                            .get("email"),
                                    authentication.getName()
                            )
            );
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Decision> result =
                decisionRepository.findAll(
                        specification,
                        pageable
                );

        List<DecisionResponse> content =
                result.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new DecisionPageResponse(
                content,
                result.getTotalElements(),
                result.getTotalPages(),
                page,
                size
        );
    }

    private DecisionResponse toResponse(
            Decision decision
    ) {

        Playthrough playthrough =
                decision.getPlaythrough();

        return new DecisionResponse(
                decision.getId(),
                playthrough.getId(),
                playthrough.getPlayerTag(),
                decision.getNode().getNodeCode(),
                decision.getResolvedNodeCode(),
                decision.getRawInput(),
                decision.getBranchType(),
                decision.getImpactLevel(),
                decision.getHandlerUnit(),
                decision.getOutcomeCode(),
                decision.getStatus(),
                playthrough.getStatus(),
                playthrough.getLucidity(),
                playthrough.getControlLevel(),
                playthrough.getEndingCode(),
                decision.getCreatedAt(),
                decision.getUpdatedAt()
        );
    }

    private RealityLogResponse toRealityLogResponse(
            RealityLog log
    ) {

        return new RealityLogResponse(
                log.getId(),
                log.getDecision().getId(),
                log.getRecipientEmail(),
                log.getSubject(),
                log.getLogStatus(),
                log.getErrorMessage(),
                log.getSentAt(),
                log.getCreatedAt()
        );
    }
}