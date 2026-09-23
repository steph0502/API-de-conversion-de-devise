package com.converter.currency_converter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse d'erreur")
public class ErrorResponse {

    @Schema(description = "Code d'erreur HTTP", example = "400")
    private int status;

    @Schema(description = "Message d'erreur", example = "Devise source invalide")
    private String message;

    @Schema(description = "Timestamp de l'erreur", example = "2025-01-15T10:30:00")
    private LocalDateTime timestamp;
}
