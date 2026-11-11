# DriveAway — Plateforme Intelligente de Gestion et Réservation de Location de Voitures

> **Projet Final en Équipe — Module Applications Web Distribuées (AWD · MT-41)**  
> **Année Universitaire : 2026–2027 — ESPRIT**  
> **Jalon 1 (Séance 4) : Validation de la Conception & Architecture Microservices**

---

## 👥 Membres de l'Équipe & Répartition des Rôles

| Nom & Prénom | Microservice Détenu | Rôle Transverse Partagé | Base de Données |
|---|---|---|---|
| **Wissal Hamraoui** | `utilisateur-service` (MS1) | Sécurité Keycloak, gestion des rôles (RBAC) & tokens JWT | H2 (Mode Fichier) |
| **Achref Reguiai** | `vehicule-service` (MS2) | Architecture globale, orchestration Docker & Docker Compose | MySQL 8.x |
| **Malek Abdnbei Dadi** | `reservation-service` (MS3) | Configuration Eureka Discovery Server & Spring Cloud Config Server | MySQL 8.x |
| **Nouha Bel Hadj Maouia** | `paiement-service` (MS4) | Pipeline d'événements RabbitMQ & Facturation PDF (Apache PDFBox) | MySQL 8.x |
| **Farah Jemmali** | `agence-service` (MS5) | Routage Spring Cloud Gateway & Gestion des filtres HTTP | H2 (Mode Fichier) |
| **Mahmoud Arfaoui** | `maintenance-service` (MS6) | Intégration API Externe Google Maps Places & Résilience Resilience4j | MongoDB 7.x |
| **Toute l'Équipe** | `avis-service` (MS7 Commun) | Développement de l'interface Frontend Angular & Tests d'intégration | MongoDB 7.x |

---

## 🎯 Problématique & Solution Métier

### Le Contexte
Les agences traditionnelles de location de véhicules font face à des défis opérationnels majeurs :
1. **Risque de surbooking** : Réservations manuelles ou non synchronisées entraînant l'attribution simultanée d'un même véhicule à deux clients.
2. **Gestion déficiente du parc** : Voitures immobilisées pour révision ou panne mécanique encore présentées par erreur dans le catalogue.
3. **Paiements et cautions complexes** : Difficulté de traçabilité entre acomptes, cautions bancaires bloquées et facturation finale.
4. **Cloisonnement des données** : Éparpillement des informations entre agences physiques, ateliers d'entretien et service comptable.

### La Solution DriveAway
DriveAway est une application Web distribuée bâtie sur une architecture microservices hautement disponible :
* **Catalogue en temps réel** : Contrôle strict de disponibilité par agence et catégorie.
* **Moteur anti-chevauchement** : Verrous transactionnels interdisant les doubles réservations.
* **Immobilisation d'urgence asynchrone** : Dès qu'une panne est déclarée, un événement RabbitMQ propage l'interdiction de louer le véhicule.
* **Sécurité centralisée** : Authentification OAuth2 / OpenID Connect via Keycloak avec rôles différenciés (`ROLE_CLIENT`, `ROLE_AGENT`).

---

## 🏗️ Architecture Globale & Stack Technique

```
                         +-----------------------------------+
                         |       Application Frontend        |
                         |            (Angular)              |
                         +-----------------+-----------------+
                                           | HTTP / REST
                                           v
                         +-----------------------------------+
                         |    Spring Cloud Gateway (8080)    |
                         | (Validation JWT & Routage Unique) |
                         +---+-------------+---------------+--+
                             |             |               |
        +--------------------+             |               +--------------------+
        |                                  |                                    |
        v                                  v                                    v
+---------------+                  +---------------+                    +---------------+
| Keycloak IAM  |                  | Eureka Server |                    | Config Server |
|  (Port 8180)  |                  |  (Port 8761)  |                    |  (Port 8888)  |
+---------------+                  +---------------+                    +---------------+
        |
        +=======================================================================+
        |                                                                       |
        v (Routage dynamique)                                                  v
+-----------------------+  +-----------------------+  +-----------------------+
|  utilisateur-service  |  |   vehicule-service    |  |  reservation-service  |
|      (Port 8081)      |  |      (Port 8082)      |  |      (Port 8083)      |
|    Base: H2 Fichier   |  |      Base: MySQL      |  |      Base: MySQL      |
+-----------------------+  +-----------------------+  +-----------------------+
        |                              ^                          |
        | OpenFeign                    | OpenFeign / Events       | Events
        v                              v                          v
+-----------------------+  +-----------------------+  +-----------------------+
|    agence-service     |  |  maintenance-service  |  |   paiement-service    |
|      (Port 8085)      |  |      (Port 8086)      |  |      (Port 8084)      |
|    Base: H2 Fichier   |  |     Base: MongoDB     |  |      Base: MySQL      |
+-----------------------+  +-----------------------+  +-----------------------+
                                       ^
                                       |
                           +-----------------------+
                           |     avis-service      |
                           |   (MS7 Commun 8087)   |
                           |     Base: MongoDB     |
                           +-----------------------+
```

### Stack Technique Imposée & Choix
* **Backend** : Java 17/21, Spring Boot 3.x, Spring Data JPA / MongoDB, Bean Validation.
* **Infrastructure Spring Cloud** :
  * **Spring Cloud Gateway** : Point d'entrée unique et sécurisé.
  * **Netflix Eureka** : Découverte et enregistrement dynamique des services.
  * **Spring Cloud Config** : Externalisation centralisée des propriétés applicatives.
  * **Spring Cloud OpenFeign + Resilience4j** : Appels synchrones inter-services avec Circuit Breakers.
  * **Spring AMQP / RabbitMQ** : Broker de messages pour les flux asynchrones événementiels.
* **Sécurité** : Keycloak (Serveur d'identité OAuth2/OIDC, tokens JWT sans état, RBAC).
* **Bases de données (Persistance Polyglotte - Zéro Base Partagée)** :
  * **MySQL 8.x** : `vehicule-service`, `reservation-service`, `paiement-service`.
  * **H2 en mode fichier** : `utilisateur-service`, `agence-service`.
  * **MongoDB 7.x (Technologie Avancée)** : `maintenance-service`, `avis-service`.
* **Conteneurisation** : Docker, Docker Compose multi-services.
* **Frontend** : Application Angular (SPA) consommant l'API Gateway.

---

## 📁 Structure du Dépôt Git (Conformité AWD)

```text
nom-du-projet-classe-groupe/
├── Backend/
│   ├── api-gateway/            # Port 8080 : Passerelle Spring Cloud Gateway
│   ├── eureka-server/          # Port 8761 : Annuaire des microservices
│   ├── config-server/          # Port 8888 : Centralisation des configurations
│   ├── utilisateur-service/    # Port 8081 : Profils, comptes, permis
│   ├── vehicule-service/       # Port 8082 : Flotte, catégories, tarifs, disponibilité
│   ├── reservation-service/    # Port 8083 : Réservations anti-surbooking & contrats
│   ├── paiement-service/       # Port 8084 : Règlements, cautions & factures PDF
│   ├── agence-service/         # Port 8085 : Agences, horaires & points de retrait
│   ├── maintenance-service/    # Port 8086 : Entretiens, immobilisations & Google Maps
│   └── avis-service/           # Port 8087 : Évaluations clients & modération (MS Commun)
├── Frontend/                   # Application cliente Web Angular
├── Documentation/
│   ├── Presentation/           # DriveAway_Presentation_Validation1.pptx / .pdf
│   └── Diagrams/               # Architecture globale & diagrammes de classes UML
└── README.md                   # Le présent document
```

---

## 🔄 Matrice des Échanges Inter-Services

### 1. Synchrones (OpenFeign avec Circuit Breaker Resilience4j)
* `reservation-service` $\rightarrow$ `vehicule-service` : Vérification en temps réel de la disponibilité et calcul du devis tarifaire.
* `reservation-service` $\rightarrow$ `utilisateur-service` : Validation de l'existence du client et de la conformité de son permis de conduire.
* `reservation-service` $\rightarrow$ `agence-service` : Vérification des horaires et de l'autorisation de restitution dans une autre ville.
* `vehicule-service` $\rightarrow$ `agence-service` : Contrôle de l'existence de l'agence lors du rattachement d'un véhicule.

### 2. Asynchrones (RabbitMQ Event-Driven)
* `vehicule.immobilise` (Émis par `maintenance-service`) : Consommé par `vehicule-service` pour bloquer immédiatement le statut du véhicule en `IMMOBILISE_MAINTENANCE`.
* `maintenance.terminee` (Émis par `maintenance-service`) : Consommé par `vehicule-service` pour réactiver le véhicule en `DISPONIBLE`.
* `reservation.confirmee` (Émis par `reservation-service`) : Consommé par `paiement-service` pour initialiser le paiement et la facture.
* `location.terminee` (Émis par `reservation-service`) : Consommé par `vehicule-service` (mise à jour du kilométrage) et `maintenance-service` (analyse d'usure et alerte vidange).

---

## 📅 Planning Prévisionnel (10 Séances)

| Jalon | Séances | Objectif & Livrables |
|---|---|---|
| **Cadrage & Conception** | S1 à S3 | Définition des User Stories, découpage DDD, modèles de données, dépôt Git. |
| **Validation 1 (Jalon 1)** | **S4** | **Revue de cadrage : soutenance de conception, contrats d'API, initialisation des projets.** |
| **Passerelle & Découverte** | S5 | Mise en service d'Eureka et routage Spring Cloud Gateway. |
| **Communications Synchrones** | S6 | Implémentation OpenFeign, DTOs partagés et disjoncteurs Resilience4j. |
| **Communications Asynchrones** | S7 | Configuration de RabbitMQ, queues, exchanges et listeners AMQP. |
| **Sécurité & IAM** | S8 | Intégration Keycloak, protection des API par rôles (RBAC) et filtres JWT. |
| **Intégration & Déploiement** | S9 | Conteneurisation globale Docker Compose et raccordement du Frontend Angular. |
| **Validation Finale (Jalon 2)**| **S10** | **Démonstration fonctionnelle de bout en bout et soutenance finale.** |

---

## 🛠️ Gestion de Projet & Workflow

* **Outil de Pilotage** : **Jira Software** (Méthodologie Scrum / Sprints de 2 semaines, User Stories chiffrées en Story Points).
* **Workflow Git** : **GitFlow**
  * `main` : Versions stables et livrables.
  * `develop` : Intégration continue de l'équipe.
  * `feature/<microservice>-<fonctionnalite>` : Branches isolées de développement.
  * Revue de code par les pairs obligatoire via Pull Request avant tout merge.
