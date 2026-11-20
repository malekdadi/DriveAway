package com.driveaway.vehicule.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vehiculeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DriveAway - Vehicule Service API")
                        .description("Microservice de gestion des véhicules et catégories pour la plateforme DriveAway")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Achref Reguiai")
                                .email("achref.reguiai@esprit.tn")));
    }
}
