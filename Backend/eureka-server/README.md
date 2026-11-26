# DriveAway — Eureka Server (Service Discovery)

**Port** : `8761`  
**Dashboard Web** : http://localhost:8761  
**Registre REST** : http://localhost:8761/eureka/apps  

## Démarrage
```cmd
cd Backend\eureka-server
.\mvnw.cmd spring-boot:run
```
Tous les microservices (`api-gateway`, `agence-service`, `vehicule-service`, `reservation-service`, etc.) s'y enregistrent automatiquement au démarrage.
