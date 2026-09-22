package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.Decision.DecisionRequest;
import com.tuckersoft.branchengine.dto.Decision.DecisionResponse;
import com.tuckersoft.branchengine.model.Decision;
import com.tuckersoft.branchengine.model.Playthrough;
import com.tuckersoft.branchengine.model.StoryNode;
import com.tuckersoft.branchengine.model.User;
import com.tuckersoft.branchengine.repository.DecisionRepository;
import com.tuckersoft.branchengine.repository.PlaythroughRepository;
import com.tuckersoft.branchengine.repository.RealityLogRepository;
import com.tuckersoft.branchengine.repository.StoryNodeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DecisionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private RealityLogRepository realityLogRepository;

    @Mock
    private PlaythroughRepository playthroughRepository;

    @Mock
    private StoryNodeRepository storyNodeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DecisionService decisionService;

    private User user;
    private StoryNode sourceNode;
    private StoryNode destinationNode;
    private Playthrough playthrough;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        decisionService = new DecisionService(
                decisionRepository,
                realityLogRepository,
                playthroughRepository,
                storyNodeRepository,
                new DecisionClassifier(),
                eventPublisher
        );

        ReflectionTestUtils.setField(
                decisionService,
                "adminEmail",
                "colin@tuckersoft.co.uk"
        );

        ReflectionTestUtils.setField(
                decisionService,
                "adminDisplayName",
                "Colin Ritman"
        );

        user = new User();
        user.setEmail("user@test.com");
        user.setDisplayName("Tester");

        sourceNode = new StoryNode();
        sourceNode.setNodeCode("NODE-A");
        sourceNode.setPrimaryBranchCode("NODE-B");
        sourceNode.setGlitchBranchCode("NODE-GLITCH");

        destinationNode = new StoryNode();
        destinationNode.setNodeCode("NODE-B");

        playthrough = new Playthrough();
        playthrough.setId(1L);
        playthrough.setPlayerTag("STEFAN-01");
        playthrough.setUser(user);
        playthrough.setCurrentNode(sourceNode);
        playthrough.setLucidity(100);
        playthrough.setControlLevel(0);
        playthrough.setStatus("ACTIVA");

        when(
                playthroughRepository.findById(1L)
        ).thenReturn(
                Optional.of(playthrough)
        );

        when(
                storyNodeRepository.findByNodeCode("NODE-B")
        ).thenReturn(
                Optional.of(destinationNode)
        );

        when(
                storyNodeRepository.findByNodeCode("NODE-GLITCH")
        ).thenReturn(
                Optional.of(destinationNode)
        );

        when(
                decisionRepository.save(any(Decision.class))
        ).thenAnswer(invocation -> {

            Decision decision =
                    invocation.getArgument(0);

            decision.setId(10L);

            return decision;
        });
    }

    private UsernamePasswordAuthenticationToken authentication() {

        return new UsernamePasswordAuthenticationToken(
                "user@test.com",
                null,
                List.of()
        );
    }

    /*
     * TEST 1
     *
     * ERRATA v1.3:
     * REBELDIA tiene prioridad sobre RUPTURA.
     */
    @Test
    void shouldClassifyDestruyeCamaraAsRebeldia() {

        DecisionRequest request =
                new DecisionRequest(
                        1L,
                        "Stefan destruye la camara",
                        "LEVE"
                );

        DecisionResponse response =
                decisionService.createDecision(
                        request,
                        authentication(),
                        null
                );

        assertEquals(
                "REBELDIA",
                response.branchType()
        );

        assertEquals(
                "Control de Continuidad",
                response.handlerUnit()
        );

        assertEquals(
                "FORK_TIMELINE",
                response.outcomeCode()
        );
    }

    /*
     * TEST 2
     *
     * Entrada corrupta:
     * aplica stats segun errata,
     * no mueve nodo,
     * no publica evento.
     */
    @Test
    void corruptInputShouldApplyStatsButNotMoveNode() {

        StoryNode original =
                playthrough.getCurrentNode();

        DecisionRequest request =
                new DecisionRequest(
                        1L,
                        "1234567890 !!!",
                        "LEVE"
                );

        DecisionResponse response =
                decisionService.createDecision(
                        request,
                        authentication(),
                        null
                );

        assertEquals(
                "ENTRADA_CORRUPTA",
                response.branchType()
        );

        assertEquals(
                "ERROR",
                response.status()
        );

        assertNull(
                response.resolvedNodeCode()
        );

        assertEquals(
                95,
                playthrough.getLucidity()
        );

        assertEquals(
                5,
                playthrough.getControlLevel()
        );

        assertSame(
                original,
                playthrough.getCurrentNode()
        );

        verify(
                eventPublisher,
                never()
        ).publishEvent(any());
    }

    /*
     * TEST 3
     *
     * ERRATA v1.3:
     * CRITICO = -45 lucidity / +40 control.
     */
    @Test
    void criticalImpactShouldUpdateStatsCorrectly() {

        DecisionRequest request =
                new DecisionRequest(
                        1L,
                        "Stefan rechaza completamente la realidad",
                        "CRITICO"
                );

        decisionService.createDecision(
                request,
                authentication(),
                null
        );

        assertEquals(
                55,
                playthrough.getLucidity()
        );

        assertEquals(
                40,
                playthrough.getControlLevel()
        );
    }

    /*
     * TEST 4
     *
     * ERRATA v1.3:
     * Si lucidity <= 0 y control >= 100
     * gana WHITE_BEAR.
     */
    @Test
    void lucidityEndingShouldHavePriorityOverControlEnding() {

        playthrough.setLucidity(45);
        playthrough.setControlLevel(60);

        DecisionRequest request =
                new DecisionRequest(
                        1L,
                        "Stefan destruye completamente toda la realidad",
                        "CRITICO"
                );

        DecisionResponse response =
                decisionService.createDecision(
                        request,
                        authentication(),
                        null
                );

        assertEquals(
                0,
                playthrough.getLucidity()
        );

        assertEquals(
                100,
                playthrough.getControlLevel()
        );

        assertEquals(
                "FINALIZADA",
                response.playthroughStatus()
        );

        assertEquals(
                "ENDING_WHITE_BEAR",
                response.endingCode()
        );
    }

    /*
     * TEST 5
     *
     * Una decision normal publica exactamente un evento.
     */
    @Test
    void normalDecisionShouldPublishExactlyOneEvent() {

        DecisionRequest request =
                new DecisionRequest(
                        1L,
                        "Stefan acepta continuar con el juego",
                        "LEVE"
                );

        decisionService.createDecision(
                request,
                authentication(),
                null
        );

        verify(
                eventPublisher,
                times(1)
        ).publishEvent(any());
    }
}