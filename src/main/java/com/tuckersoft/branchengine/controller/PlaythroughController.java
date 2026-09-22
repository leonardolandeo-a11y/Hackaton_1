package com.tuckersoft.branchengine.controller;

import com.tuckersoft.branchengine.dto.Playthrough.PlaythroughPathResponseDTO;
import com.tuckersoft.branchengine.dto.Playthrough.PlaythroughRequestDTO;
import com.tuckersoft.branchengine.dto.Playthrough.PlaythroughResponseDTO;
import com.tuckersoft.branchengine.service.PlaythroughService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/playthroughs")
public class PlaythroughController {

    private final PlaythroughService playthroughService;

    public PlaythroughController(
            PlaythroughService playthroughService
    ) {
        this.playthroughService = playthroughService;
    }

    @PostMapping
    public ResponseEntity<PlaythroughResponseDTO> create(
            @Valid @RequestBody PlaythroughRequestDTO request,
            Authentication authentication
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        playthroughService.createPlaythrough(
                                request,
                                authentication
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<PlaythroughResponseDTO>> getAll(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                playthroughService.getAllPlaythroughs(
                        authentication
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaythroughResponseDTO> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                playthroughService.getPlaythroughById(
                        id,
                        authentication
                )
        );
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<PlaythroughPathResponseDTO> getPath(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                playthroughService.getPath(
                        id,
                        authentication
                )
        );
    }
}