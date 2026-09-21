package com.tuckersoft.branchengine.dto.decision;

import java.util.List;

public record DecisionPageResponse(

        List<com.tuckersoft.branchengine.dto.decision.DecisionResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int size

) {
}