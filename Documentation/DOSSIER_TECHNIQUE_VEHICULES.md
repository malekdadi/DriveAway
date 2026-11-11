# DOSSIER TECHNIQUE : MICROSERVICE VÉHICULES ET CATÉGORIES
**Architecture Épurée (Exactement 3 Tables / Entités) & Persistance Relationnelle**

* **Périmètre** : Gestion exclusive du Parc Automobile, Catégorisation & Tarifs (Projet 7 Semaines - MVP)
* **Session** : Séance 4 (Validation 1 - Conception) | Année 2026-2027 | ESPRIT (Module AWD · MT-41)
* **Responsable** : **Achref Reguiai**
* **Base de Données** : **MySQL 8.x** (Conforme au barème : transactions ACID, intégrité référentielle et indexation)
* **Conformité Barème AWD** : Critères **I1** (Conception : 3 pts) et **I2** (Interactions : 2 pts) — Score Cible **8/8**

---

## 1. Technologies Retenues (Gestion Véhicules)

| Composant | Technologie | Rôle & Justification |
|---|---|---|
| **Backend** | Java 17/21 + Spring Boot 3.x | API RESTful sans état, injection de dépendances, architecture en couches (Controller, Service, Repository, DTO). |
| **Base de Données** | **MySQL 8.x** | Modèle relationnel strict, intégrité référentielle forte entre Catégories et Véhicules, transactions ACID, index B-Tree performants pour la recherche catalogue. |
| **Accès aux Données** | Spring Data JPA / Hibernate | Repositories natifs, requêtes dérivées et spécifications JPQL pour le filtrage multi-critères dynamique. |
| **Validation des Données** | Bean Validation (Hibernate Validator) | Validation déclarative des DTOs (`@NotNull`, `@NotBlank`, `@Positive`, `@Pattern` pour l'immatriculation). |
| **Synchrone Inter-MS** | OpenFeign + Resilience4j | Exposition d'endpoints pour Réservations ; appel du service Agences avec disjoncteur (Circuit Breaker) en cas de panne réseau. |
| **Asynchrone Inter-MS** | RabbitMQ + Spring AMQP | Écoute de `vehicule.immobilise` (Maintenance) et `location.terminee` (Réservations pour mise à jour kilométrique). |
| **Sécurité & Documentation** | Keycloak (JWT) + Swagger / OpenAPI | Validation des jetons RBAC (`ROLE_CLIENT` pour lecture, `ROLE_AGENT` pour modifications) et contrat d'API RFC 9110. |

---

## 2. Modèle de Données Épuré (Exactement 3 Tables / Carreaux UML)

Le modèle a été conçu pour respecter le principe de responsabilité unique (SRP) et éviter toute surcharge inutile pour le jalon MVP :

| Table / Carreau UML | Attributs & Champs Épurés | Relations & Rôle Métier |
|---|---|---|
| **1. Categorie** *(Taxonomie & Tarification de base)* | • `id` : Long [PK, Auto-incrément]<br>• `libelle` : Enum/String (`CITADINE`, `BERLINE`, `SUV`, `UTILITAIRE`)<br>• `description` : String (Description commerciale)<br>• `tarifBaseParJour` : Double (Tarif journalier HT)<br>• `cautionBase` : Double (Montant de l'empreinte bancaire) | **Relation** : Categorie (1) $\longleftrightarrow$ (0..*) Vehicule.<br>**Rôle** : Définit la grille tarifaire de référence et le niveau de gamme du véhicule. |
| **2. Vehicule** *(Unité Physique du Parc)* | • `id` : Long [PK, Auto-incrément]<br>• `immatriculation` : String [UK, ex: '123-TN-4567']<br>• `marque` : String (ex: 'Renault', 'Peugeot')<br>• `modele` : String (ex: 'Clio 5', '3008')<br>• `annee` : Integer (Année de mise en circulation)<br>• `kilometrage` : Integer (Compteur kilométrique actuel)<br>• `statut` : Enum (`DISPONIBLE`, `RESERVE`, `EN_LOCATION`, `IMMOBILISE_MAINTENANCE`, `REFORME`)<br>• `agenceActuelleId` : Long [Référence logique externe vers Agences]<br>• `categorieId` : Long [FK interne vers Categorie] | **Relation** : Categorie (1) $\longleftrightarrow$ (0..*) Vehicule<br>**Relation** : Vehicule (1) $\longleftrightarrow$ (0..*) OptionVehicule.<br>**Rôle** : Représente la voiture physique disponible à la location. Stocke son état opérationnel et son agence de rattachement. |
| **3. OptionVehicule** *(Équipements & Suppléments)* | • `id` : Long [PK, Auto-incrément]<br>• `vehiculeId` : Long [FK interne vers Vehicule]<br>• `nomOption` : String (ex: 'GPS Intégré', 'Siège Bébé', 'Boîte Automatique')<br>• `supplementJournalier` : Double (Coût journalier additionnel) | **Relation** : Vehicule (1) $\longleftrightarrow$ (0..*) OptionVehicule.<br>**Rôle** : Permet d'associer des équipements valorisants facturés en supplément journalier. |

---

## 3. Les Métiers Simples (CRUD, Tri & Recherche)

* **Cycle de Vie & CRUD** :
  * Créer un nouveau véhicule (`DISPONIBLE`), modifier les caractéristiques techniques, réassigner à une autre agence (`agenceActuelleId`), archiver ou marquer pour vente (`REFORME`).
* **Recherche Multi-critères Catalogue** :
  * Filtrage combiné : par agence (`agenceActuelleId`), par catégorie (`categorieId`), par tranche de prix maximum journalier, et par disponibilité opérationnelle (`statut == DISPONIBLE`).
* **Tri & Pagination Standardisée** :
  * Tri par tarif journalier croissant / décroissant, tri par kilométrage, tri par marque/modèle.
  * Pagination systématique conforme aux bonnes pratiques REST (`?page=0&size=12&sort=tarifBaseParJour,asc`).

---

## 4. Les Métiers Avancés (Valeur Métier Ajoutée)

1. **Calculateur de Devis Dynamique** :
   * Endpoint de calcul instantané prenant en compte la durée de location en jours, le tarif journalier de la catégorie du véhicule, et la somme des suppléments de toutes les options actives sélectionnées.
2. **Machine à États et Contrôle de Transition Stricte** :
   * Règles de transition interdisant les états incohérents : impossible de passer une voiture de `IMMOBILISE_MAINTENANCE` directement à `EN_LOCATION` sans validation intermédiaire d'un retour au statut `DISPONIBLE`.
3. **Contrôle d'Éligibilité Catégorie / Permis** :
   * Règle métier restreignant les catégories haut de gamme (ex: SUV de luxe exigeant 3 ans d'ancienneté de permis de conduire, vérifié en synchrone auprès du service Utilisateurs).
4. **Résilience et Tolérance aux Pannes (Resilience4j Circuit Breaker)** :
   * Si le service Agences est injoignable lors de la vérification de l'agence de rattachement, un fallback dégradé valide l'enregistrement sous réserve de vérification asynchrone ultérieure.

---

## 5. Interactions Inter-Services (Synchrones & Asynchrones)

### A. Flux Synchrones (OpenFeign)
* **Consommé par `reservation-service`** :
  * `GET /api/vehicules/{id}/disponibilite` : Réservations vérifie en temps réel que le véhicule est opérationnel avant de finaliser la réservation.
  * `POST /api/vehicules/{id}/calcul-devis?jours=X` : Réservations récupère le montant exact à facturer pour le contrat.
* **Appelé vers `agence-service`** :
  * `GET /api/agences/{id}` : Vérifie que l'agence spécifiée lors de l'enregistrement ou de la réaffectation du véhicule existe bien.

### B. Flux Asynchrones (RabbitMQ / Spring AMQP)
* **Événement consommé `vehicule.immobilise`** (Émis par `maintenance-service`) :
  * Dès qu'une panne mécanique grave est déclarée par l'atelier, le statut du véhicule est automatiquement basculé en `IMMOBILISE_MAINTENANCE` sans intervention humaine.
* **Événement consommé `maintenance.terminee`** (Émis par `maintenance-service`) :
  * Clôture des réparations : le véhicule repasse automatiquement au statut `DISPONIBLE`.
* **Événement consommé `location.terminee`** (Émis par `reservation-service`) :
  * À la restitution du véhicule, le kilométrage réel retourné est directement incrémenté dans la table `Vehicule`.

---

## 6. Contrat d'API REST à Créer (Endpoints du Microservice)

| Verbe HTTP | URI de la Ressource | Description & Rôle Métier | Code Succès |
|---|---|---|---|
| **GET** | `/api/vehicules` | Catalogue paginé avec filtres (categorieId, agenceId, prixMax, statut) | `200 OK` |
| **GET** | `/api/vehicules/{id}` | Fiche détaillée d'un véhicule et de ses équipements | `200 OK` |
| **POST** | `/api/vehicules` | Enregistrement d'un nouveau véhicule dans la flotte (Agent) | `201 Created` |
| **PUT** | `/api/vehicules/{id}` | Mise à jour des caractéristiques (marque, modèle, année) | `200 OK` |
| **PATCH** | `/api/vehicules/{id}/statut` | Changement d'état opérationnel (`DISPONIBLE`, `IMMOBILISE`, etc.) | `200 OK` |
| **PATCH** | `/api/vehicules/{id}/kilometrage` | Mise à jour du compteur kilométrique | `200 OK` |
| **GET** | `/api/vehicules/{id}/disponibilite` | Test d'éligibilité immédiate à la location (pour Réservations) | `200 OK` |
| **POST** | `/api/vehicules/{id}/calcul-devis` | Calcul dynamique du tarif total selon le nombre de jours | `200 OK` |
| **GET** | `/api/categories` | Liste complète des catégories et tarifs de base | `200 OK` |
| **POST** | `/api/categories` | Création d'une nouvelle catégorie tarifaire | `201 Created` |
| **POST** | `/api/vehicules/{id}/options` | Ajout d'une option d'équipement au véhicule | `201 Created` |
| **DELETE** | `/api/vehicules/{id}` | Retrait d'un véhicule du parc (archivage / réforme) | `204 No Content` |
