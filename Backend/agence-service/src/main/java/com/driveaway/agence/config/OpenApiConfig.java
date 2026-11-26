package com.driveaway.agence.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agenceServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DriveAway — Agence & Lieux de Retrait API")
                        .description("Contrat OpenAPI du microservice agence-service : gestion des agences, horaires d'ouverture, points de retrait/retour, recherche par proximité GPS et statistiques du réseau.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mahmoud Arfaoui")
                                .email("mahmoud.arfaoui@esprit.tn")))
                .servers(List.of(
                        new Server().url("http://localhost:8085").description("Instance locale agence-service"),
                        new Server().url("http://localhost:8080").description("API Gateway DriveAway")
                ));
    }
}
