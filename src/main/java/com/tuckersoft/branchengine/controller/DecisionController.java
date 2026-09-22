package com.tuckersoft.branchengine.controller;

import com.tuckersoft.branchengine.dto.DecisionPageResponse;
import com.tuckersoft.branchengine.dto.DecisionRequest;
import com.tuckersoft.branchengine.dto.DecisionResponse;
import com.tuckersoft.branchengine.dto.RealityLogResponse;
import com.tuckersoft.branchengine.service.DecisionService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/decisions")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(
            DecisionService decisionService
    ) {
        this.decisionService = decisionService;
    }

    @PostMapping
    public ResponseEntity<DecisionResponse> create(
            @Valid
            @RequestBody DecisionRequest request,

            @RequestHeader(
                    value = "X-Bandersnatch-Simulate",
                    required = false
            )
            String simulationHeader,

            Authentication authentication
    ) {

        DecisionResponse response =
                decisionService.createDecision(
                        request,
                        authentication,
                        simulationHeader
                );

        /*
         * ERRATA v1.3:
         * POST Decision retorna 202.
         */
        return ResponseEntity
                .accepted()
                .body(response);
    }

    @GetMapping
    public ResponseEntity<DecisionPageResponse> getAll(

            @RequestParam(required = false)
            String branchType,

            @RequestParam(required = false)
            String impactLevel,

            @RequestParam(required = false)
            String status,

            @RequestParam(required = false)
            Long playthroughId,

            @RequestParam(defaultValue = "1")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            Authentication authentication
    ) {

        return ResponseEntity.ok(
                decisionService.getAll(
                        branchType,
                        impactLevel,
                        status,
                        playthroughId,
                        page,
                        size,
                        authentication
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DecisionResponse> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                decisionService.getById(
                        id,
                        authentication
                )
        );
    }

    @GetMapping("/{id}/reality-logs")
    public ResponseEntity<List<RealityLogResponse>>
    getRealityLogs(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                decisionService.getRealityLogs(
                        id,
                        authentication
                )
        );
    }
}
