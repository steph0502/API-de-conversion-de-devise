package com.converter.currency_converter.controller;

import com.converter.currency_converter.dto.ConversionRequest;
import com.converter.currency_converter.dto.ConversionResponse;
import com.converter.currency_converter.service.CurrencyConverterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyConverterController.class)
class CurrencyConverterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CurrencyConverterService currencyConverterService;

    // ==================== POST /api/v1/convert ====================

    @Test
    @DisplayName("POST /convert - Conversion réussie USD -> EUR")
    void convert_post_validRequest_returns200() throws Exception {
        // Given
        ConversionRequest request = new ConversionRequest("USD", "EUR", 100.0);
        ConversionResponse response = ConversionResponse.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .amount(100.0)
                .convertedAmount(85.62)
                .rate(0.85618)
                .build();

        when(currencyConverterService.convert("USD", "EUR", 100.0)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.convertedAmount").value(85.62))
                .andExpect(jsonPath("$.rate").value(0.85618));
    }

    @Test
    @DisplayName("POST /convert - Devise source vide retourne 400")
    void convert_post_emptyFromCurrency_returns400() throws Exception {
        // Given
        ConversionRequest request = new ConversionRequest("", "EUR", 100.0);

        // When & Then
        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("devise source")));
    }

    @Test
    @DisplayName("POST /convert - Devise cible vide retourne 400")
    void convert_post_emptyToCurrency_returns400() throws Exception {
        // Given
        ConversionRequest request = new ConversionRequest("USD", "", 100.0);

        // When & Then
        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("devise cible")));
    }

    @Test
    @DisplayName("POST /convert - Montant négatif retourne 400")
    void convert_post_negativeAmount_returns400() throws Exception {
        // Given
        ConversionRequest request = new ConversionRequest("USD", "EUR", -5.0);

        // When & Then
        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("montant")));
    }

    @Test
    @DisplayName("POST /convert - Montant null retourne 400")
    void convert_post_nullAmount_returns400() throws Exception {
        // Given
        ConversionRequest request = new ConversionRequest("USD", "EUR", null);

        // When & Then
        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /convert - JSON vide retourne 400")
    void convert_post_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /convert - Body sans champ fromCurrency retourne 400")
    void convert_post_missingFromCurrency_returns400() throws Exception {
        String json = "{\"toCurrency\": \"EUR\", \"amount\": 100}";

        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /convert - Erreur du service retourne 400")
    void convert_post_serviceThrowsException_returns400() throws Exception {
        // Given
        ConversionRequest request = new ConversionRequest("USD", "XYZ", 100.0);
        when(currencyConverterService.convert("USD", "XYZ", 100.0))
                .thenThrow(new IllegalArgumentException("Devise cible invalide: XYZ"));

        // When & Then
        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Devise cible invalide: XYZ"));
    }

    @Test
    @DisplayName("POST /convert - Devise identique USD -> USD")
    void convert_post_sameCurrency_returns200() throws Exception {
        // Given
        ConversionRequest request = new ConversionRequest("USD", "USD", 100.0);
        ConversionResponse response = ConversionResponse.builder()
                .fromCurrency("USD")
                .toCurrency("USD")
                .amount(100.0)
                .convertedAmount(100.0)
                .rate(1.0)
                .build();

        when(currencyConverterService.convert("USD", "USD", 100.0)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(1.0))
                .andExpect(jsonPath("$.convertedAmount").value(100.0));
    }

    // ==================== GET /api/v1/convert ====================

    @Test
    @DisplayName("GET /convert - Conversion réussie via query params")
    void convert_get_validParams_returns200() throws Exception {
        // Given
        ConversionResponse response = ConversionResponse.builder()
                .fromCurrency("EUR")
                .toCurrency("CDF")
                .amount(50.0)
                .convertedAmount(133693.66)
                .rate(2673.87)
                .build();

        when(currencyConverterService.convert("EUR", "CDF", 50.0)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/convert")
                        .param("from", "EUR")
                        .param("to", "CDF")
                        .param("amount", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCurrency").value("EUR"))
                .andExpect(jsonPath("$.toCurrency").value("CDF"))
                .andExpect(jsonPath("$.amount").value(50.0))
                .andExpect(jsonPath("$.convertedAmount").value(133693.66))
                .andExpect(jsonPath("$.rate").value(2673.87));
    }

    @Test
    @DisplayName("GET /convert - Paramètre 'from' manquant retourne 400")
    void convert_get_missingFromParam_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/convert")
                        .param("to", "EUR")
                        .param("amount", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /convert - Paramètre 'to' manquant retourne 400")
    void convert_get_missingToParam_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/convert")
                        .param("from", "USD")
                        .param("amount", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /convert - Paramètre 'amount' manquant retourne 400")
    void convert_get_missingAmountParam_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/convert")
                        .param("from", "USD")
                        .param("to", "EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /convert - Erreur service retourne 400")
    void convert_get_serviceThrowsException_returns400() throws Exception {
        when(currencyConverterService.convert("USD", "INVALID", 100.0))
                .thenThrow(new IllegalArgumentException("Devise cible invalide: INVALID"));

        mockMvc.perform(get("/api/v1/convert")
                        .param("from", "USD")
                        .param("to", "INVALID")
                        .param("amount", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Devise cible invalide: INVALID"));
    }

    @Test
    @DisplayName("POST /convert - Montant zéro est rejeté par validation")
    void convert_post_zeroAmount_returns400() throws Exception {
        ConversionRequest request = new ConversionRequest("USD", "EUR", 0.0);

        mockMvc.perform(post("/api/v1/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
