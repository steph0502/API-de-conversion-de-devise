package com.converter.currency_converter.controller;

import com.converter.currency_converter.dto.ConversionRequest;
import com.converter.currency_converter.dto.ConversionResponse;
import com.converter.currency_converter.service.CurrencyConverterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Currency Converter", description = "API de conversion de devises")
public class CurrencyConverterController {

    private final CurrencyConverterService currencyConverterService;

    public CurrencyConverterController(CurrencyConverterService currencyConverterService) {
        this.currencyConverterService = currencyConverterService;
    }

    @PostMapping("/convert")
    @Operation(
            summary = "Convertir une devise",
            description = "Convertit un montant d'une devise source vers une devise cible en utilisant les taux de change en temps réel.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Conversion réussie",
                            content = @Content(schema = @Schema(implementation = ConversionResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Paramètres invalides ou devise invalide",
                            content = @Content(schema = @Schema(implementation = ConversionResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = "Service externe indisponible",
                            content = @Content(schema = @Schema(implementation = ConversionResponse.class))
                    )
            }
    )
    public ResponseEntity<ConversionResponse> convert(@Valid @RequestBody ConversionRequest request) {
        ConversionResponse response = currencyConverterService.convert(
                request.getFromCurrency(),
                request.getToCurrency(),
                request.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/convert")
    @Operation(
            summary = "Convertir une devise via paramètres query",
            description = "Convertit un montant d'une devise source vers une devise cible via des paramètres de requête."
    )
    public ResponseEntity<ConversionResponse> convert(
            @Parameter(description = "Code devise source (ex: USD)", example = "USD")
            @RequestParam String from,

            @Parameter(description = "Code devise cible (ex: EUR)", example = "EUR")
            @RequestParam String to,

            @Parameter(description = "Montant à convertir", example = "100")
            @RequestParam Double amount) {

        ConversionResponse response = currencyConverterService.convert(from, to, amount);
        return ResponseEntity.ok(response);
    }
}
