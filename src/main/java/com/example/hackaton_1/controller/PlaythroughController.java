package com.example.hackaton_1.controller;

import com.example.hackaton_1.dto.Playthrough.PlaythroughRequestDTO;
import com.example.hackaton_1.dto.Playthrough.PlaythroughResponseDTO;
import com.example.hackaton_1.service.PlaythroughService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PlaythroughResponseDTO> createPlaythrough(
            @Valid @RequestBody PlaythroughRequestDTO dto
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        playthroughService
                                .createPlaythrough(dto)
                );
    }

    @GetMapping
    public ResponseEntity<List<PlaythroughResponseDTO>>
    getMyPlaythroughs() {

        return ResponseEntity.ok(
                playthroughService.getMyPlaythroughs()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaythroughResponseDTO>
    getPlaythroughById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                playthroughService
                        .getPlaythroughById(id)
        );
    }
}