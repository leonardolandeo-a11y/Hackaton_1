package com.tuckersoft.branchengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DecisionRequest(

        @NotNull(message = "playthroughId es obligatorio")
        Long playthroughId,

        @NotBlank(message = "rawInput es obligatorio")
        @Size(min = 10, message = "rawInput debe tener al menos 10 caracteres")
        String rawInput,

        @NotBlank(message = "impactLevel es obligatorio")
        @Pattern(
                regexp = "LEVE|MODERADO|GRAVE|CRITICO",
                message = "impactLevel debe ser LEVE, MODERADO, GRAVE o CRITICO"
        )
        String impactLevel

) {
}
