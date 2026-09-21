package com.example.hackaton_1.service;

import dto.StoryNode.StoryNodePageResponseDTO;
import dto.StoryNode.StoryNodeRequestDTO;
import dto.StoryNode.StoryNodeResponseDTO;
import com.example.hackaton_1.model.StoryNode;
import com.example.hackaton_1.repository.StoryNodeRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class StoryNodeService {

    private final StoryNodeRepository storyNodeRepository;

    public StoryNodeService(StoryNodeRepository storyNodeRepository) {
        this.storyNodeRepository = storyNodeRepository;
    }

    public StoryNodeResponseDTO createStoryNode(StoryNodeRequestDTO dto) {

        if (storyNodeRepository.existsByNodeCode(dto.getNodeCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A StoryNode with this nodeCode already exists");
        }

        StoryNode storyNode = new StoryNode(
                dto.getNodeCode(),
                dto.getTitle(),
                dto.getSceneText(),
                dto.getBranchCapacity(),
                0,
                dto.getPrimaryBranchCode(),
                dto.getGlitchBranchCode(),
                Instant.now()
        );

        StoryNode savedStoryNode = storyNodeRepository.save(storyNode);

        return toDTO(savedStoryNode);
    }

    public StoryNodeResponseDTO getStoryNodeById(Long id) {

        StoryNode storyNode = storyNodeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "StoryNode not found"));

        return toDTO(storyNode);
    }

    public StoryNodePageResponseDTO getAllStoryNodes(int page, int size) {

        if (page < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page must be greater than or equal to 1"
            );
        }

        if (size < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "size must be greater than 0"
            );
        }

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<StoryNode> storyNodes = storyNodeRepository.findAll(pageable);

        return new StoryNodePageResponseDTO(
                storyNodes.getContent()
                        .stream()
                        .map(this::toDTO)
                        .toList(),
                storyNodes.getTotalElements(),
                storyNodes.getTotalPages(),
                page,
                size
        );
    }

    private StoryNodeResponseDTO toDTO(StoryNode storyNode) {

        StoryNodeResponseDTO dto = new StoryNodeResponseDTO();

        dto.setId(storyNode.getId());
        dto.setNodeCode(storyNode.getNodeCode());
        dto.setTitle(storyNode.getTitle());
        dto.setSceneText(storyNode.getSceneText());
        dto.setBranchCapacity(storyNode.getBranchCapacity());
        dto.setCurrentBranches(storyNode.getCurrentBranches());
        dto.setPrimaryBranchCode(storyNode.getPrimaryBranchCode());
        dto.setGlitchBranchCode(storyNode.getGlitchBranchCode());
        dto.setCreatedAt(storyNode.getCreatedAt());

        return dto;
    }
}