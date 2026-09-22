package com.tuckersoft.branchengine.dto.Playthrough;

import java.time.Instant;

public record PlaythroughPathStepDTO(
        int order,
        Long decisionId,
        String fromNodeCode,
        String toNodeCode,
        String branchType,
        String impactLevel,
        Instant createdAt
) {
}