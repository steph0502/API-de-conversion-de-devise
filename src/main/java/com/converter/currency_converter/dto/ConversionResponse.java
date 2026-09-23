package com.converter.currency_converter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse de conversion de devise")
public class ConversionResponse {

    @Schema(description = "Devise source", example = "USD")
    private String fromCurrency;

    @Schema(description = "Devise cible", example = "EUR")
    private String toCurrency;

    @Schema(description = "Montant source", example = "100.00")
    private Double amount;

    @Schema(description = "Montant converti", example = "92.50")
    private Double convertedAmount;

    @Schema(description = "Taux de change utilisé", example = "0.925")
    private Double rate;
}
