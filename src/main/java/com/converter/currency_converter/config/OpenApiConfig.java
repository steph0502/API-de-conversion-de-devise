package com.converter.currency_converter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI currencyConverterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Conversion de Devises")
                        .description("API REST pour convertir une somme d'argent d'une devise à une autre en utilisant des taux de change dynamiques.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Currency Converter Team")
                                .email("contact@converter.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
