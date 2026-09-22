package com.tuckersoft.branchengine.dto.Playthrough;

import java.util.List;

public record PlaythroughPathResponseDTO(
        Long playthroughId,
        String playerTag,
        String status,
        String endingCode,
        String startNodeCode,
        String currentNodeCode,
        List<PlaythroughPathStepDTO> steps
) {
}