package com.example.hackaton_1.controller;

import dto.StoryNode.StoryNodePageResponseDTO;
import dto.StoryNode.StoryNodeRequestDTO;
import dto.StoryNode.StoryNodeResponseDTO;
import com.example.hackaton_1.service.StoryNodeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nodes")
public class StoryNodeController {

    private final StoryNodeService storyNodeService;

    public StoryNodeController(StoryNodeService storyNodeService) {
        this.storyNodeService = storyNodeService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StoryNodeResponseDTO> createStoryNode(
            @Valid @RequestBody StoryNodeRequestDTO dto
    ) {
        StoryNodeResponseDTO response = storyNodeService.createStoryNode(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<StoryNodePageResponseDTO> getAllStoryNodes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(storyNodeService.getAllStoryNodes(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoryNodeResponseDTO> getStoryNodeById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(storyNodeService.getStoryNodeById(id));
    }
}