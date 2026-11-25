# DriveAway Facturation

Projet Maven Java 17, Spring Boot 3.2.5 et Spring Cloud 2023.0.1.
Le service `facturation-service` écoute sur le port 8083 et s'inscrit
sous `FACTURATION-SERVICE` dans le registre Eureka central d'Achref.

## Compiler et démarrer

Depuis la racine du projet, avec Java 17 et Maven dans le PATH :

```powershell
mvn clean package -DskipTests
.\Lancer-Facturation.cmd
```

Le lanceur démarre uniquement la facturation. Le registre central est
`http://100.78.116.80:8761/eureka/` et le Gateway central est sur le port
8080 de la même machine. Le service annonce `100.92.208.91:8083`.
Pour modifier ces valeurs, utiliser `EUREKA_SERVER_URL`,
`EUREKA_INSTANCE_IP` et `PORT`.

Les modules Eureka et Gateway locaux sont compilés avec le projet.
Pour tester la facturation avec un registre local démarré séparément,
définir dans le terminal du service :

```powershell
$env:EUREKA_SERVER_URL = 'http://127.0.0.1:8761/eureka/'
$env:EUREKA_INSTANCE_IP = '127.0.0.1'
.\Lancer-Facturation.cmd
```

## Base de données

Le service utilise MySQL sur `127.0.0.1:3306/driveaway` par défaut.
Les variables `DB_URL`, `DB_USER` et `DB_PASSWORD` permettent de changer
la connexion. Créer la base `driveaway` avant le premier démarrage.
Spring crée les tables absentes à partir de
[schema.sql](facturation-service/src/main/resources/schema.sql).

Si MySQL est inaccessible au démarrage, le service utilise une base H2
en mémoire pendant toute cette exécution. Les écritures H2 disparaissent
à l'arrêt et ne sont pas transférées à MySQL. Redémarrer le service quand
MySQL redevient disponible. Une erreur d'identifiants ou de schéma reste
une erreur de configuration.

## Vérifier

- [Santé locale](http://127.0.0.1:8083/actuator/health) indique le moteur actif.
- [Tableau de bord local](http://127.0.0.1:8083/test-facturation.html) permet de tester les factures.
- Le dashboard Eureka d'Achref doit afficher `FACTURATION-SERVICE` en `UP`.
- Si le Gateway d'Achref route `/api/factures` vers `lb://facturation-service`,
  tester `http://100.78.116.80:8080/api/factures`.

Configurer SMTP et Konnect par variables d'environnement. Les paramètres
sont dans [application.properties](facturation-service/src/main/resources/application.properties).
