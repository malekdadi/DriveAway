# DriveAway — Microservice `agence-service`

**Responsable** : Mahmoud Arfaoui  
**Port** : `8085`  
**Service ID (Eureka)** : `AGENCE-SERVICE`  
**Route API Gateway** : `/api/agences/**` → `lb://AGENCE-SERVICE`  
**Base de données dédiée** : H2 (`jdbc:h2:mem:agencedb`)  
**Module** : Architecture Web Distribuée (AWD) — ESPRIT 2026–2027

---

## Stack technique
* Java 17 · Spring Boot 3.2.5 · Spring Cloud 2023.0.1
* Spring Data JPA · H2 (Database-per-Service) · Bean Validation
* OpenAPI 3 / Swagger UI (`springdoc-openapi` 2.5.0)
* Erreurs normalisées RFC 9457 (`application/problem+json`)
* Spring Boot Actuator (`health`, `info`, `metrics`, sondes `liveness`/`readiness` + `AgenceNetworkHealthIndicator`)
* Netflix Eureka Discovery Client (`prefer-ip-address=true`, `eureka.client.healthcheck.enabled=true`)

---

## Contrat d'API REST (`/api/agences`)

### Agences
| Méthode | URI | Rôle | Codes |
|---|---|---|---|
| `GET` | `/api/agences` | Lister, filtrer (`ville`, `nom`, `statut`, `ouvert24h`, `ouverteMaintenant`, `typePoint`, `retraitAutorise`, `retourAutorise`, `autreVilleAutorise`) et trier (`sortBy`, `direction`) | `200`, `400` |
| `GET` | `/api/agences/page` | Lister avec pagination (`page`, `size`) et filtres | `200`, `400` |
| `GET` | `/api/agences/{id}` | Détail d'une agence avec ses points de retrait | `200`, `400`, `404` |
| `POST` | `/api/agences` | Créer une agence (+ en-tête `Location`) | `201`, `400`, `409` |
| `PUT` | `/api/agences/{id}` | Remplacer complètement une agence | `200`, `400`, `404`, `409` |
| `DELETE` | `/api/agences/{id}` | Supprimer une agence (refusée `409` si elle possède encore des points) | `204`, `404`, `409` |
| `PATCH` | `/api/agences/{id}/horaires` | Modifier partiellement les horaires / jours / mode 24h | `200`, `400`, `404` |
| `PATCH` | `/api/agences/{id}/statut` | Changer le statut (`ACTIVE`, `FERMEE_TEMPORAIREMENT`, `FERMEE_DEFINITIVEMENT`) | `200`, `400`, `404` |
| `GET` | `/api/agences/{id}/ouverture` | Vérifier si l'agence est ouverte maintenant ou à `dateHeure` (ISO 8601) | `200`, `400`, `404` |
| `GET` | `/api/agences/{id}/retour-autorise` | Vérifier l'autorisation de retour (`?autreVille=true/false`) | `200`, `404` |
| `GET` | `/api/agences/proximite` | Trouver les agences dans un rayon GPS (`latitude`, `longitude`, `rayonKm`) | `200`, `400` |
| `GET` | `/api/agences/statistiques` | Statistiques agrégées du réseau | `200` |

### Points de retrait (sous-ressource)
| Méthode | URI | Rôle | Codes |
|---|---|---|---|
| `GET` | `/api/agences/points` | Lister et filtrer tous les points du réseau | `200` |
| `GET` | `/api/agences/{agenceId}/points` | Lister les points d'une agence | `200`, `404` |
| `GET` | `/api/agences/{agenceId}/points/{pointId}` | Consulter un point précis | `200`, `404` |
| `POST` | `/api/agences/{agenceId}/points` | Ajouter un point de retrait (+ en-tête `Location`) | `201`, `400`, `404`, `409` |
| `PUT` | `/api/agences/{agenceId}/points/{pointId}` | Remplacer un point de retrait | `200`, `400`, `404`, `409` |
| `PATCH` | `/api/agences/{agenceId}/points/{pointId}/parametres` | Basculer `retraitAutorise`, `retourAutorise`, `autreVilleAutorise`, `actif`, `capaciteMax` | `200`, `400`, `404`, `409` |
| `DELETE` | `/api/agences/{agenceId}/points/{pointId}` | Supprimer un point de retrait | `204`, `404` |

---

## Démarrage complet (Architecture DriveAway : Eureka → Gateway → agence-service → Angular)

1. **Eureka Server (`:8761`)** :
   ```cmd
   cd Backend\eureka-server
   .\mvnw.cmd spring-boot:run
   ```
   Dashboard : http://localhost:8761

2. **API Gateway (`:8080`)** :
   ```cmd
   cd Backend\api-gateway
   .\mvnw.cmd spring-boot:run
   ```
   Routes actives : http://localhost:8080/actuator/gateway/routes

3. **Agence Service (`:8085`)** *(s'enregistre automatiquement sous `AGENCE-SERVICE` dans Eureka)* :
   ```cmd
   cd Backend\agence-service
   .\mvnw.cmd spring-boot:run
   ```

4. **Frontend Angular (`:4200`)** *(appelle `/api/**` via la Gateway `:8080`)* :
   ```cmd
   cd Frontend\DriveAway
   npm start
   ```

* **Point d'entrée unique (Gateway)** : http://localhost:8080/api/agences
* **Swagger UI (agence-service)** : http://localhost:8085/swagger-ui.html
* **OpenAPI JSON** : http://localhost:8085/v3/api-docs
* **Actuator Health** : http://localhost:8085/actuator/health
* **Actuator Info** : http://localhost:8085/actuator/info
* **H2 Console** : http://localhost:8085/h2-console (`jdbc:h2:mem:agencedb`, user `sa`, password vide)
