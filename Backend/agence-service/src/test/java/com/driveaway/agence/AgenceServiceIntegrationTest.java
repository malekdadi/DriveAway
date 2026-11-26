package com.driveaway.agence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@Transactional
class AgenceServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListFilterSortAndPaginateAgences() throws Exception {
        mockMvc.perform(get("/api/agences").param("ville", "tunis").param("sortBy", "nom").param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nom").value("La Marsa"))
                .andExpect(jsonPath("$[1].nom").value("Tunis-Carthage"));

        mockMvc.perform(get("/api/agences/page").param("page", "0").param("size", "2").param("sortBy", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void shouldExecuteFullCrudLifecycleWithLocationAndIdempotentDelete() throws Exception {
        String createPayload = """
                {
                  "nom": "Bizerte Marina",
                  "adresse": "10 Quai Tarak Ibn Ziad, Bizerte",
                  "ville": "Bizerte",
                  "telephone": "+216 72 431 200",
                  "heureOuverture": "08:00:00",
                  "heureFermeture": "19:00:00",
                  "latitude": 37.2746,
                  "longitude": 9.8739
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/agences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/agences/")))
                .andExpect(jsonPath("$.nom").value("Bizerte Marina"))
                .andExpect(jsonPath("$.statut").value("ACTIVE"))
                .andExpect(jsonPath("$.ouvert24h").value(false))
                .andExpect(jsonPath("$.joursOuverture").value("LUN,MAR,MER,JEU,VEN,SAM"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long agenceId = created.get("id").asLong();

        // PUT full update
        String updatePayload = """
                {
                  "nom": "Bizerte Centre",
                  "adresse": "12 Avenue Habib Bourguiba, Bizerte",
                  "ville": "Bizerte",
                  "telephone": "+216 72 431 205",
                  "heureOuverture": "07:30:00",
                  "heureFermeture": "20:00:00",
                  "ouvert24h": false,
                  "joursOuverture": "LUN,MAR,MER,JEU,VEN,SAM,DIM",
                  "statut": "ACTIVE",
                  "latitude": 37.2746,
                  "longitude": 9.8739
                }
                """;

        mockMvc.perform(put("/api/agences/{id}", agenceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Bizerte Centre"))
                .andExpect(jsonPath("$.joursOuverture").value("LUN,MAR,MER,JEU,VEN,SAM,DIM"));

        // PATCH horaires (partial)
        mockMvc.perform(patch("/api/agences/{id}/horaires", agenceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"heureFermeture\":\"21:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heureOuverture").value("07:30:00"))
                .andExpect(jsonPath("$.heureFermeture").value("21:00:00"));

        // DELETE 1st call -> 204 No Content
        mockMvc.perform(delete("/api/agences/{id}", agenceId))
                .andExpect(status().isNoContent());

        // DELETE 2nd call -> 404 Not Found (Workshop 1 Q8 idempotence)
        mockMvc.perform(delete("/api/agences/{id}", agenceId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldRespectRfc9457ErrorsFor400_404_409Scenarios() throws Exception {
        // E1: Validation failure -> 400 + errors map
        mockMvc.perform(post("/api/agences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"\",\"adresse\":\"\",\"ville\":\"\",\"telephone\":\"abc\",\"heureOuverture\":\"09:00:00\",\"heureFermeture\":\"18:00:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.nom").exists())
                .andExpect(jsonPath("$.errors.telephone").exists())
                .andExpect(jsonPath("$.instance").value("/api/agences"));

        // E2: Malformed JSON -> 400
        mockMvc.perform(post("/api/agences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\": \"Broken\""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Malformed Request"));

        // E3: Non-numeric path ID -> 400
        mockMvc.perform(get("/api/agences/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid Parameter"));

        // Business validation: closing time <= opening time -> 400
        mockMvc.perform(post("/api/agences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Test\",\"adresse\":\"Rue 1\",\"ville\":\"Nabeul\",\"telephone\":\"+216 72 111 222\",\"heureOuverture\":\"18:00:00\",\"heureFermeture\":\"08:00:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        // E5: Not found -> 404
        mockMvc.perform(get("/api/agences/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        // E8: Duplicate agency (same name + city) -> 409
        mockMvc.perform(post("/api/agences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"La Marsa\",\"adresse\":\"Autre rue\",\"ville\":\"Tunis\",\"telephone\":\"+216 71 000 000\",\"heureOuverture\":\"08:00:00\",\"heureFermeture\":\"18:00:00\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Conflict"));

        // E10: Deleting an agency that still has pickup points -> 409
        mockMvc.perform(delete("/api/agences/1"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", containsString("point(s) de retrait")));
    }

    @Test
    void shouldVerifySimpleBusinessFeaturesAndPointsRetrait() throws Exception {
        // Opening check: La Marsa open Wednesday 14:30, closed Sunday 14:30, closed Wednesday 22:00
        mockMvc.perform(get("/api/agences/2/ouverture").param("dateHeure", "2026-11-25T14:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ouverte").value(true));

        mockMvc.perform(get("/api/agences/2/ouverture").param("dateHeure", "2026-11-29T14:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ouverte").value(false))
                .andExpect(jsonPath("$.raison", containsString("dimanche")));

        mockMvc.perform(get("/api/agences/2/ouverture").param("dateHeure", "2026-11-25T22:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ouverte").value(false));

        // Return check: Tunis-Carthage allows one-way return; Hammamet (FERMEE_TEMPORAIREMENT) refuses
        mockMvc.perform(get("/api/agences/1/retour-autorise").param("autreVille", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agenceActive").value(true))
                .andExpect(jsonPath("$.retourAutorise").value(true))
                .andExpect(jsonPath("$.nombrePointsCompatibles").value(2));

        mockMvc.perform(get("/api/agences/5/retour-autorise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agenceActive").value(false))
                .andExpect(jsonPath("$.retourAutorise").value(false));

        // Proximity search around Tunis-Carthage airport (25 km) -> Tunis-Carthage first, then La Marsa
        mockMvc.perform(get("/api/agences/proximite")
                        .param("latitude", "36.8510")
                        .param("longitude", "10.2272")
                        .param("rayonKm", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].agence.nom").value("Tunis-Carthage"))
                .andExpect(jsonPath("$[0].distanceKm").value(0.0));

        // Point coherence rule: autreVilleAutorise=true with retourAutorise=false -> 400
        String incoherentPoint = """
                {
                  "nom": "Point Incohérent",
                  "adresse": "Rue Test 99",
                  "typePoint": "CENTRE_VILLE",
                  "retraitAutorise": true,
                  "retourAutorise": false,
                  "autreVilleAutorise": true
                }
                """;
        mockMvc.perform(post("/api/agences/1/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incoherentPoint))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        // Point creation + PATCH parametres + DELETE
        String validPoint = """
                {
                  "nom": "Relais Centre Urbain Nord",
                  "adresse": "Boulevard de la Terre, Centre Urbain Nord",
                  "typePoint": "CENTRE_VILLE",
                  "retraitAutorise": true,
                  "retourAutorise": true,
                  "autreVilleAutorise": false,
                  "capaciteMax": 15
                }
                """;
        MvcResult pointRes = mockMvc.perform(post("/api/agences/1/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPoint))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/agences/1/points/")))
                .andExpect(jsonPath("$.actif").value(true))
                .andReturn();

        long pointId = objectMapper.readTree(pointRes.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/agences/1/points/{pointId}/parametres", pointId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"autreVilleAutorise\":true,\"capaciteMax\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autreVilleAutorise").value(true))
                .andExpect(jsonPath("$.capaciteMax").value(30));

        mockMvc.perform(delete("/api/agences/1/points/{pointId}", pointId))
                .andExpect(status().isNoContent());

        // Network statistics
        mockMvc.perform(get("/api/agences/statistiques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAgences").value(5))
                .andExpect(jsonPath("$.agencesActives").value(4))
                .andExpect(jsonPath("$.totalPointsRetrait").value(7));
    }

    @Test
    void shouldExposeMinimalActuatorContractAndCustomHealthIndicator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.agenceNetwork.status").value("UP"))
                .andExpect(jsonPath("$.components.agenceNetwork.details.agencesActives").value(4));

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("agence-service"))
                .andExpect(jsonPath("$.app.author").value("Mahmoud Arfaoui"));

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());

        // Chapitre 4 Slide 31 / Quiz Q7 : /actuator/env ne doit PAS etre expose !
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isNotFound());
    }
}
