package com.tuckersoft.branchengine.listener;

import com.tuckersoft.branchengine.event.DecisionCommittedEvent;
import com.tuckersoft.branchengine.entity.Decision;
import com.tuckersoft.branchengine.entity.RealityLog;
import com.tuckersoft.branchengine.repository.DecisionRepository;
import com.tuckersoft.branchengine.repository.RealityLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
public class BranchNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(BranchNotificationListener.class);

    private final DecisionRepository decisionRepository;
    private final RealityLogRepository realityLogRepository;
    private final JavaMailSender mailSender;

    public BranchNotificationListener(
            DecisionRepository decisionRepository,
            RealityLogRepository realityLogRepository,
            JavaMailSender mailSender
    ) {
        this.decisionRepository = decisionRepository;
        this.realityLogRepository = realityLogRepository;
        this.mailSender = mailSender;
    }

    @Async("branchExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleDecisionCommitted(
            DecisionCommittedEvent event
    ) {

        Decision decision = decisionRepository
                .findById(event.decisionId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Decision not found: "
                                        + event.decisionId()
                        )
                );

        decision.setStatus("PROCESANDO");
        decision.setUpdatedAt(Instant.now());

        decisionRepository.save(decision);

        String subject = buildSubject(event);

        try {

            /*
             * Simulación obligatoria de fallo de correo.
             * Debe lanzar una excepción real para que entre
             * al mismo catch que un fallo SMTP real.
             */
            if (event.simulateMailFailure()) {
                throw new RuntimeException(
                        "Simulated mail failure"
                );
            }

            String body = buildBody(event);

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(event.recipientEmail());
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            // ENVÍO EXITOSO
            decision.setStatus("ESTABILIZADA");
            decision.setUpdatedAt(Instant.now());

            decisionRepository.save(decision);

            RealityLog realityLog = new RealityLog();

            realityLog.setDecision(decision);
            realityLog.setRecipientEmail(
                    event.recipientEmail()
            );
            realityLog.setSubject(subject);
            realityLog.setLogStatus("SENT");
            realityLog.setErrorMessage(null);
            realityLog.setSentAt(Instant.now());
            realityLog.setCreatedAt(Instant.now());

            realityLogRepository.save(realityLog);

        } catch (Exception exception) {

            // ENVÍO FALLIDO
            decision.setStatus("ERROR");
            decision.setUpdatedAt(Instant.now());

            decisionRepository.save(decision);

            RealityLog realityLog = new RealityLog();

            realityLog.setDecision(decision);
            realityLog.setRecipientEmail(
                    event.recipientEmail()
            );
            realityLog.setSubject(subject);
            realityLog.setLogStatus("FAILED");
            realityLog.setErrorMessage(
                    exception.getMessage()
            );
            realityLog.setSentAt(null);
            realityLog.setCreatedAt(Instant.now());

            realityLogRepository.save(realityLog);

            log.error(
                    "Error sending reality report for decision {}",
                    decision.getId(),
                    exception
            );
        }

        printBranchLog(event, decision);
    }

    private String buildSubject(
            DecisionCommittedEvent event
    ) {

        return "[TUCKERSOFT] "
                + event.branchType()
                + " en "
                + event.playerTag()
                + " | Impacto "
                + event.impactLevel();
    }

    private String buildBody(
            DecisionCommittedEvent event
    ) {

        String endingCode =
                event.endingCode() == null
                        ? "-"
                        : event.endingCode();

        String resolvedNodeCode =
                event.resolvedNodeCode() == null
                        ? "-"
                        : event.resolvedNodeCode();

        return """
                Hola %s,

                Una partida de prueba acaba de ramificarse.

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Decision ID      : #%s
                Jugador          : %s
                Rama             : %s
                Impacto          : %s
                Departamento     : %s
                Consecuencia     : %s
                Nodo origen      : %s
                Nodo destino     : %s
                Estado partida   : %s
                Lucidez          : %s/100
                Nivel de control : %s/100
                Final            : %s
                Registrada       : %s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                Decisión original del jugador:
                "%s"

                — Tuckersoft Branch Engine, 1984
                """.formatted(
                event.displayName(),
                event.decisionId(),
                event.playerTag(),
                event.branchType(),
                event.impactLevel(),
                event.handlerUnit(),
                event.outcomeCode(),
                event.sourceNodeCode(),
                resolvedNodeCode,
                event.playthroughStatus(),
                event.lucidity(),
                event.controlLevel(),
                endingCode,
                event.createdAt(),
                event.rawInput()
        );
    }

    private void printBranchLog(
            DecisionCommittedEvent event,
            Decision decision
    ) {

        System.out.printf(
                "[BRANCH-LOG] Decision ID: %s | "
                        + "Player: %s | "
                        + "Branch: %s | "
                        + "Impact: %s | "
                        + "Unit: %s | "
                        + "Node: %s -> %s | "
                        + "Thread: %s | "
                        + "Status: %s%n",
                decision.getId(),
                event.playerTag(),
                event.branchType(),
                event.impactLevel(),
                event.handlerUnit(),
                event.sourceNodeCode(),
                event.resolvedNodeCode(),
                Thread.currentThread().getName(),
                decision.getStatus()
        );
    }
}