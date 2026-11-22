# 🚪 DriveAway - Central API Gateway & Observability Hub

Welcome to the **API Gateway** microservice for the **DriveAway** distributed platform.

The API Gateway is the **Single Point of Entry** (Reverse Proxy) for the entire application. All client traffic (Angular Frontend, Mobile, Backoffice, external consumers) communicates **only with port 8080**, never directly with internal microservices.

---

## 🌟 Key Features

1. **Centralized Single Point of Entry (Port 8080)**:
   * The Angular frontend connects exclusively to `http://localhost:8080` (or `http://<GATEWAY_IP>:8080`).
   * No client ever needs to know individual microservice ports or internal laptop IPs.

2. **Zero Hardcoded IPs (100% Dynamic Discovery via Eureka)**:
   * Microservices dynamically register with Eureka on startup.
   * The Gateway resolves destinations at runtime using the `lb://<SERVICE-NAME>` protocol.
   * If any student restarts their PC or switches Wi-Fi networks, the Gateway automatically tracks their new IP address with **zero configuration changes**.

3. **Global CORS Pre-configured**:
   * Pre-configured to allow cross-origin requests from Angular running on `localhost:4200` or on another computer on the local network.

4. **Real-time Traffic Monitoring (`LoggingGlobalFilter`)**:
   * Every incoming request and its latency, client IP, route target, and HTTP response code are logged in real time directly on the Gateway console.

5. **Observability & Route Introspection**:
   * Inspect all active route definitions dynamically via Spring Boot Actuator.

---

## 🗺️ Route Mapping Table

All routes are dynamically load-balanced across active instances:

| Route ID | Incoming Gateway Path | Destination (Eureka) | Description |
| :--- | :--- | :--- | :--- |
| `vehicule-service-routes` | `/api/vehicules/**`, `/api/categories/**`, `/uploads/**` | `lb://VEHICULE-SERVICE` | Vehicles catalog, categories, image uploads |
| `reservation-service-routes` | `/api/reservations/**` | `lb://RESERVATION-SERVICE` | Booking and rental reservations |
| `agence-service-routes` | `/api/agences/**` | `lb://AGENCE-SERVICE` | Agency locations and fleets |
| `paiement-service-routes` | `/api/paiements/**`, `/api/factures/**` | `lb://PAIEMENT-SERVICE` | Billing, payments, invoices |
| `utilisateur-service-routes` | `/api/utilisateurs/**`, `/api/auth/**` | `lb://UTILISATEUR-SERVICE` | User profiles, authentication |
| `maintenance-service-routes` | `/api/maintenance/**` | `lb://MAINTENANCE-SERVICE` | Vehicle repairs, inspections |

*Note: The Gateway also enables Spring Cloud's **Discovery Locator Fallback**, meaning any new microservice registered in Eureka can also be called dynamically via `http://localhost:8080/{service-name}/**`.*

---

## 🛠️ How to Run

### 1. Prerequisites
* **Java 17+**
* **Eureka Server** running on port `8761` (default)

### 2. Standard Launch (Localhost)
```bash
./mvnw clean spring-boot:run
```
Or on Windows:
```cmd
mvnw.cmd clean spring-boot:run
```

### 3. Dynamic Multi-PC / Wi-Fi Network Launch
When testing across different laptops connected to the same Wi-Fi, simply pass the IP address of the laptop hosting Eureka:

```bash
# Option A: Command Line Argument
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--eureka.client.service-url.defaultZone=http://192.168.1.50:8761/eureka/"

# Option B: Environment Variable
set EUREKA_SERVER_URL=http://192.168.1.50:8761/eureka/
mvnw.cmd spring-boot:run
```

---

## 🔍 Monitoring & Health Endpoints

Once the Gateway is running on port `8080`, you can inspect it in your browser or Postman:

| Endpoint | Purpose | URL |
| :--- | :--- | :--- |
| **Live Routes Inspector** | Shows every registered route, filter, and URI | `http://localhost:8080/actuator/gateway/routes` |
| **Gateway Health** | Check UP/DOWN status and Eureka connection | `http://localhost:8080/actuator/health` |
| **Gateway Metrics** | JVM, Netty connections, request counters | `http://localhost:8080/actuator/metrics` |

---

## 👥 How Teammates Connect Their Microservices

Each teammate simply adds `spring-cloud-starter-netflix-eureka-client` to their project and configures their `application.yml`:

```yaml
spring:
  application:
    name: <SERVICE-NAME> # e.g. reservation-service, agence-service...

eureka:
  client:
    service-url:
      defaultZone: http://<EUREKA-IP>:8761/eureka/
  instance:
    prefer-ip-address: true
```
As soon as their service boots up, the API Gateway immediately discovers it and begins routing frontend calls to their machine!
