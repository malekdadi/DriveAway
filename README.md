# DriveAway

**Plateforme distribuée de gestion et de réservation de location de voitures**  
*Module Applications Web Distribuées (AWD · MT-41) — Année 2026–2027 — ESPRIT*

---

## 📌 Présentation du Projet

DriveAway est une application Web complète conçue selon une architecture **microservices**. Elle modernise et sécurise l'ensemble de la chaîne de location de véhicules :
* Consultation en temps réel du catalogue de véhicules disponibles par agence et catégorie.
* Gestion des réservations sans double réservation (moteur anti-surbooking).
* Automatisation des paiements, cautions et factures dématérialisées.
* Gestion du réseau d'agences physiques, des points de retrait et de restitution.
* Suivi des interventions techniques, révisions et immobilisation des véhicules en cas de panne.

---

## 👥 Équipe du Projet

* **Wissal Hamraoui** — `utilisateur-service`
* **Achref Reguiai** — `vehicule-service`
* **Malek Abdnbei Dadi** — `reservation-service`
* **Nouha Bel Hadj Maouia** — `paiement-service`
* **Farah Jemmali** — `agence-service`
* **Mahmoud Arfaoui** — `maintenance-service`

---

## 🏛️ Architecture Technique Globale

* **Backend** : Java 17/21 · Spring Boot 3 · Spring Data · OpenFeign · RabbitMQ
* **Infrastructure** : Spring Cloud Gateway · Netflix Eureka · Config Server · Keycloak (OAuth2/JWT) · Docker Compose
* **Frontend** : Angular (Single Page Application)
* **Bases de données** : Persistance polyglotte dédiée par microservice (MySQL, H2, MongoDB)

---

## 📂 Structure du Dépôt

```text
DriveAway/
├── Backend/          # Microservices Spring Boot et composants d'infrastructure
├── Frontend/         # Application Web cliente (Angular)
├── Documentation/    # Présentation (PPTX/PDF), diagrammes et dossiers techniques
└── README.md         # Présentation générale du projet
```
