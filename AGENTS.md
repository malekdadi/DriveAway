# DriveAway Facturation

Ce dossier est le projet backend autonome de facturation de l'utilisateur.
Java 17, Maven multi-module, Spring Boot 3.5.8 et Spring Cloud 2025.0.0.
Le frontend Angular de l'ancien projet DriveAway est dans un autre dossier.
Faire les modifications de facturation dans ce projet autonome, sauf demande explicite.

## Carte du code

- `paiement-service` (8083) contient `BillingController` pour les routes REST et
  `BillingService` pour le CRUD des factures, les paiements et les cautions avec JDBC.
  `InvoiceDocuments` gere les PDF et l'envoi par e-mail. `KonnectClient` gere Konnect.
- `eureka-server` (8761) fournit le registre et le dashboard. Le service s'y
  enregistre sous `PAIEMENT-SERVICE`.
- `api-gateway` (8080) route les factures, paiements, cautions, le catalogue de
  vehicules utilise par la facturation et le webhook Konnect via `lb://paiement-service`.
- `paiement-service/src/main/resources/schema.sql` contient les tables `facture`,
  `paiement`, `caution`, `caution_avance` et `facture_location`.
  MySQL utilise par defaut la base `driveaway` deja presente dans XAMPP.
  Les variables DB_URL, DB_USER et DB_PASSWORD permettent de changer la connexion.

## Demarrage et verification

- Consulter `README.md` pour les commandes Maven et le chemin Java sous Windows.
- Compiler depuis la racine avec `mvn package`. `Lancer-Facturation.cmd` lance les
  JAR deja compiles. Demarrer MySQL, Eureka, paiement-service, puis la Gateway.
- Verifier `http://127.0.0.1:8083/actuator/health` pour MySQL, le dashboard Eureka
  sur 8761 pour l'enregistrement et `http://127.0.0.1:8080/api/factures` pour le routage.
- Le dernier test de la stack a confirme MySQL UP, PAIEMENT-SERVICE UP et HTTP 200
  avec les memes donnees en direct et via la Gateway. Le lanceur Windows a ete
  ajoute ensuite. Verifier les ports occupes si le demarrage echoue.
- Utiliser des donnees de test pour les essais d'ecriture. Garder les identifiants
  SMTP et Konnect dans les variables d'environnement.
