# DriveAway Facturation

Projet Maven autonome avec Java 17, Spring Boot et Spring Cloud.

| Application | Port local | Role |
| --- | --- | --- |
| Eureka | 8761 | Registre des services |
| Paiement | 8083 | Factures, paiements, cautions et PDF |
| API Gateway | 8080 | Routage vers PAIEMENT-SERVICE via Eureka |

## MySQL

Demarrer MySQL dans XAMPP. Par defaut, le service utilise la base
`driveaway` sur `127.0.0.1:3306`, avec le compte `root` et un mot de
passe vide. Definir `DB_URL`, `DB_USER` et `DB_PASSWORD` pour changer
ces valeurs. Pour une nouvelle installation, creer d'abord la base avec
`CREATE DATABASE IF NOT EXISTS driveaway CHARACTER SET utf8mb4;`.

Au demarrage, Spring initialise les cinq tables de
[PAIEMENT-SERVICE](PAIEMENT-SERVICE/src/main/resources/schema.sql)
si elles sont absentes. Une base existante conserve ses donnees.

## Demarrage

Depuis le dossier de ce projet, compiler les trois modules avec
`mvn package`. Lancer ensuite chaque commande dans un terminal separe
dans l'ordre indique. Attendre que le dashboard Eureka s'ouvre avant de
lancer le service de paiement. Attendre ensuite que PAIEMENT-SERVICE soit
UP dans Eureka avant de lancer la Gateway. Si une ancienne instance
occupe deja un des ports, l'arreter depuis son terminal avec Ctrl+C ou
choisir un autre port.

```powershell
java -jar eureka-server/target/eureka-server-1.0.0.jar
```

```powershell
java -jar PAIEMENT-SERVICE/target/PAIEMENT-SERVICE-1.0.0.jar
```

Le service de paiement cible par défaut le registre Eureka d'Achref sur
`http://100.78.116.80:8761/eureka/` et annonce l'adresse Tailscale de Malek
`100.92.208.91`. Pour le démarrage manuel avec Eureka local, définir ces
variables dans le terminal du service de paiement avant de lancer le JAR.
Le lanceur Windows les définit déjà.

```powershell
$env:EUREKA_SERVER_URL = 'http://127.0.0.1:8761/eureka/'
$env:EUREKA_INSTANCE_IP = '127.0.0.1'
```

```powershell
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

Si Java est absent du PATH sur ce PC, remplacer `java` par
`& 'C:\Users\pc\.jdks\ms-17.0.20.1\bin\java.exe'`.
Les ports peuvent etre configures via `EUREKA_PORT`, `PORT` (paiement)
et `GATEWAY_PORT`. Si le port Eureka change, definir `EUREKA_SERVER_URL`
dans les terminaux du service et de la Gateway.

### Maven sur ce PC

Si PowerShell ne reconnait pas `mvn`, compiler depuis le dossier du projet
avec Maven deja present dans le cache local.

```powershell
$env:JAVA_HOME = 'C:\Users\pc\.jdks\ms-17.0.20.1'
& 'C:\Users\pc\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd' package
```

## Verification

- [Dashboard Eureka](http://127.0.0.1:8761/) affiche PAIEMENT-SERVICE avec le statut UP.
- [Sante du service](http://127.0.0.1:8083/actuator/health) affiche MySQL UP.
- [Factures via la Gateway](http://127.0.0.1:8080/api/factures) renvoie une liste JSON.
  Prevoir quelques secondes pour la propagation de l'enregistrement Eureka.

La Gateway transmet aussi les routes de paiements, cautions, du catalogue
de vehicules utilise par la facturation et du webhook Konnect. Le paiement
en ligne requiert `KONNECT_API_KEY`, `KONNECT_WALLET_ID` et
`KONNECT_WEBHOOK_URL`. L'envoi des factures requiert un serveur SMTP
(voir `PAIEMENT-SERVICE/src/main/resources/application.properties`).
