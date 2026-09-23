package com.converter.currency_converter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de conversion de devise")
public class ConversionRequest {

    @NotBlank(message = "La devise source ne peut pas être vide")
    @Schema(description = "Code de la devise source (ex: USD, EUR, CDF)", example = "USD")
    private String fromCurrency;

    @NotBlank(message = "La devise cible ne peut pas être vide")
    @Schema(description = "Code de la devise cible (ex: USD, EUR, CDF)", example = "EUR")
    private String toCurrency;

    @NotNull(message = "Le montant ne peut pas être vide")
    @Positive(message = "Le montant doit être supérieur à zéro")
    @Schema(description = "Montant à convertir", example = "100.00")
    private Double amount;
}
