package com.converter.currency_converter.service;

import com.converter.currency_converter.dto.ConversionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyConverterServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private CurrencyConverterService service;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        service = new CurrencyConverterService(webClientBuilder);
        ReflectionTestUtils.setField(service, "apiBaseUrl", "https://open.er-api.com/v6");
    }

    @Test
    @DisplayName("Conversion USD -> EUR avec succès")
    void convert_validCurrencies_returnsConvertedAmount() {
        // Given
        Map<String, Object> ratesResponse = Map.of(
                "result", "success",
                "base_code", "USD",
                "rates", Map.of("USD", 1.0, "EUR", 0.85618)
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(ratesResponse));

        // When
        ConversionResponse response = service.convert("USD", "EUR", 100.0);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFromCurrency()).isEqualTo("USD");
        assertThat(response.getToCurrency()).isEqualTo("EUR");
        assertThat(response.getAmount()).isEqualTo(100.0);
        assertThat(response.getConvertedAmount()).isEqualTo(85.62);
        assertThat(response.getRate()).isEqualTo(0.85618);
    }

    @Test
    @DisplayName("Conversion EUR -> CDF avec succès")
    void convert_eurToCdf_returnsConvertedAmount() {
        // Given
        Map<String, Object> ratesResponse = Map.of(
                "result", "success",
                "base_code", "EUR",
                "rates", Map.of("EUR", 1.0, "CDF", 2673.87)
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(ratesResponse));

        // When
        ConversionResponse response = service.convert("eur", "cdf", 50.0);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFromCurrency()).isEqualTo("EUR");
        assertThat(response.getToCurrency()).isEqualTo("CDF");
        assertThat(response.getRate()).isEqualTo(2673.87);
    }

    @Test
    @DisplayName("Conversion de la même devise retourne le même montant avec taux 1.0")
    void convert_sameCurrency_returnsSameAmountWithRateOne() {
        // When
        ConversionResponse response = service.convert("USD", "USD", 150.0);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFromCurrency()).isEqualTo("USD");
        assertThat(response.getToCurrency()).isEqualTo("USD");
        assertThat(response.getAmount()).isEqualTo(150.0);
        assertThat(response.getConvertedAmount()).isEqualTo(150.0);
        assertThat(response.getRate()).isEqualTo(1.0);

        // Aucun appel API ne doit être fait
        verify(webClient, never()).get();
    }

    @Test
    @DisplayName("Les devises sont converties en majuscules automatiquement")
    void convert_lowercaseCurrencies_convertsToUppercase() {
        // Given
        Map<String, Object> ratesResponse = Map.of(
                "result", "success",
                "base_code", "USD",
                "rates", Map.of("USD", 1.0, "EUR", 0.85)
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(ratesResponse));

        // When
        ConversionResponse response = service.convert("usd", "eur", 100.0);

        // Then
        assertThat(response.getFromCurrency()).isEqualTo("USD");
        assertThat(response.getToCurrency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Devise source invalide - pas de clé 'rates' dans la réponse")
    void convert_invalidSourceCurrency_throwsIllegalArgumentException() {
        // Given - réponse sans clé "rates"
        Map<String, Object> errorResponse = Map.of(
                "result", "error",
                "error-type", "invalid-code"
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(errorResponse));

        // When & Then
        assertThatThrownBy(() -> service.convert("INVALID", "EUR", 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Devise source invalide");
    }

    @Test
    @DisplayName("Devise cible invalide - taux non présent dans la réponse")
    void convert_invalidTargetCurrency_throwsIllegalArgumentException() {
        // Given - réponse avec des taux mais pas la devise cible
        Map<String, Object> ratesResponse = Map.of(
                "result", "success",
                "base_code", "USD",
                "rates", Map.of("USD", 1.0, "EUR", 0.85)
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(ratesResponse));

        // When & Then
        assertThatThrownBy(() -> service.convert("USD", "XYZ", 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Devise cible invalide");
    }

    @Test
    @DisplayName("Erreur API externe - WebClientResponseException")
    void convert_webClientResponseException_throwsIllegalArgument() {
        // Given
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new WebClientResponseException(
                        404, "Not Found", null, new byte[0], null)));

        // When & Then
        assertThatThrownBy(() -> service.convert("USD", "EUR", 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Devise source invalide ou problème de connexion");
    }

    @Test
    @DisplayName("Réponse vide de l'API externe")
    void convert_emptyResponse_throwsIllegalArgument() {
        // Given
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.empty());

        // When & Then
        assertThatThrownBy(() -> service.convert("USD", "EUR", 100.0))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Montant avec décimales - arrondi correct à 2 chiffres")
    void convert_decimalAmount_roundsCorrectly() {
        // Given
        Map<String, Object> ratesResponse = Map.of(
                "result", "success",
                "base_code", "USD",
                "rates", Map.of("USD", 1.0, "EUR", 0.85618)
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(ratesResponse));

        // When
        ConversionResponse response = service.convert("USD", "EUR", 33.33);

        // Then
        assertThat(response.getConvertedAmount()).isEqualTo(28.54);
    }

    @Test
    @DisplayName("Montant de zéro avec devise différente appel à l'API")
    void convert_zeroAmount_callsApi() {
        // Given
        Map<String, Object> ratesResponse = Map.of(
                "result", "success",
                "base_code", "USD",
                "rates", Map.of("USD", 1.0, "EUR", 0.85)
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(ratesResponse));

        // When
        ConversionResponse response = service.convert("USD", "EUR", 0.0);

        // Then
        assertThat(response.getConvertedAmount()).isEqualTo(0.0);
        verify(webClient).get();
    }
}
