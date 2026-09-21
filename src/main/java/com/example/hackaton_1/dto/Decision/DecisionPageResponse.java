package com.example.hackaton_1.dto.Decision;

import java.util.List;

public record DecisionPageResponse(

        List<DecisionResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int size

) {
}