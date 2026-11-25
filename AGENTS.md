# DriveAway Facturation

Ce dossier est le projet backend autonome de facturation de l'utilisateur.
Java 17, Maven multi-module, Spring Boot 3.2.5 et Spring Cloud 2023.0.1.
Le frontend Angular de l'ancien projet DriveAway est dans un autre dossier.
Faire les modifications de facturation dans ce projet autonome, sauf demande explicite.

## Carte du code

- `facturation-service` (8083) contient `BillingController` pour les routes REST et
  `BillingService` pour le CRUD des factures, les paiements et les cautions avec JDBC.
  `InvoiceDocuments` gere les PDF et l'envoi par e-mail. `KonnectClient` gere Konnect.
- `eureka-server` (8761) fournit un registre pour les tests locaux.
  Par defaut, le service s'enregistre dans Eureka chez Achref sous `FACTURATION-SERVICE`.
- `api-gateway` (8080) route les factures, paiements, cautions, le catalogue de
  vehicules utilise par la facturation et le webhook Konnect via `lb://facturation-service`.
- `facturation-service/src/main/resources/schema.sql` contient les tables `facture`,
  `paiement`, `caution`, `caution_avance` et `facture_location`.
  MySQL utilise par defaut la base `driveaway` deja presente dans XAMPP.
  Les variables DB_URL, DB_USER et DB_PASSWORD permettent de changer la connexion.
  Si MySQL est inaccessible au demarrage, H2 en memoire prend le relais.

## Demarrage et verification

- Consulter `README.md` pour les commandes Maven et le chemin Java sous Windows.
- Compiler depuis la racine avec `mvn clean package -DskipTests`.
  `Lancer-Facturation.cmd` lance uniquement le JAR de facturation deja compile.
- Verifier `http://127.0.0.1:8083/actuator/health` et le dashboard Eureka
  d'Achref sur le port 8761. Le Gateway central est sur le port 8080.
  Verifier les ports occupes si le demarrage echoue.
- Utiliser des donnees de test pour les essais d'ecriture. Garder les identifiants
  SMTP et Konnect dans les variables d'environnement.
