package com.example.hackaton_1.exceptions;

import java.time.Instant;

public record ErrorResponse(
        String error,
        String message,
        Instant timestamp,
        String path
) {
}