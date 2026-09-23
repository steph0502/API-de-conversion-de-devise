package com.converter.currency_converter.service;

import com.converter.currency_converter.dto.ConversionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
public class CurrencyConverterService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyConverterService.class);
    private final WebClient webClient;

    @Value("${exchange.api.base-url}")
    private String apiBaseUrl;

    public CurrencyConverterService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public ConversionResponse convert(String fromCurrency, String toCurrency, Double amount) {
        logger.info("Conversion demandée: {} {} -> {}", amount, fromCurrency, toCurrency);

        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();

        if (from.equals(to)) {
            return ConversionResponse.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .amount(amount)
                    .convertedAmount(amount)
                    .rate(1.0)
                    .build();
        }

        try {
            Map<String, Object> response = webClient.get()
                    .uri(apiBaseUrl + "/latest/{base}", from)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("rates")) {
                throw new IllegalArgumentException("Devise source invalide: " + from);
            }

            @SuppressWarnings("unchecked")
            Map<String, Double> rates = (Map<String, Double>) response.get("rates");

            if (!rates.containsKey(to)) {
                throw new IllegalArgumentException("Devise cible invalide: " + to);
            }

            Double rate = rates.get(to);
            Double convertedAmount = amount * rate;

            logger.info("Taux {} -> {}: {}. Montant converti: {}", from, to, rate, convertedAmount);

            return ConversionResponse.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .amount(amount)
                    .convertedAmount(Math.round(convertedAmount * 100.0) / 100.0)
                    .rate(rate)
                    .build();

        } catch (WebClientResponseException ex) {
            logger.error("Erreur API externe: {}", ex.getMessage());
            throw new IllegalArgumentException("Devise source invalide ou problème de connexion à l'API externe");
        } catch (Exception ex) {
            logger.error("Erreur lors de la conversion: {}", ex.getMessage());
            throw ex;
        }
    }
}
