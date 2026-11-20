# 🚗 DriveAway — Vehicule & Categorie Service

**Author**: Achref Reguiai  
**Branch**: `gestion_vehicule_categorie`  
**Port**: `8087`  
**Module**: AWD (Applications Web Distribuées) — ESPRIT 2026–2027  

---

## 📌 Overview

The **Vehicule Service** is a core microservice of the **DriveAway** car rental platform. It manages the vehicle catalog and vehicle categories, provides fleet search and availability tracking for customers, and exposes administrative operations via an integrated **Dark-Themed Backoffice** and a documented **REST API**.

---

## 🛠️ Tech Stack & Architecture

* **Language & Framework**: Java 21 · Spring Boot 3.2.5
* **Data Persistence**: Spring Data JPA · H2 In-Memory Database (`jdbc:h2:mem:vehiculedb`)
* **API Documentation**: SpringDoc OpenAPI 3 / Swagger UI (`/swagger-ui.html`)
* **Admin UI (Backoffice)**: Spring Boot Starter Thymeleaf · Bootstrap 5.3 · Bootstrap Icons
* **Service Discovery**: Netflix Eureka Client (ready, disabled for local standalone development)
* **Design Patterns**:
  * 3-Tier Layered Architecture (`Controller` &rarr; `Service` &rarr; `Repository` &rarr; `Database`)
  * Data Transfer Object (DTO) Pattern with immutable Java `record`s
  * Constructor-based Dependency Injection (IoC Container Singleton Scope)
  * RFC 7807 Standardized Problem Details (`GlobalExceptionHandler`)

---

## 🎯 Core Business Features ("Métiers Basiques")

| # | Business Feature | Method / Endpoint | Description |
|---|---|---|---|
| **1** | **Duplicate Plate Prevention** | `VehiculeService.create/update` | Rejects any vehicle registration if its license plate already exists (throws HTTP 409 `ConflictException`). |
| **2** | **Duplicate Category Prevention** | `CategorieService.create/update` | Rejects category creation/update if the name already exists (case-insensitive check). |
| **3** | **Delete Protection for Categories** | `CategorieService.delete` | Blocks deleting a category as long as it still contains attached vehicles (protects relational integrity). |
| **4** | **Available Cars Discovery** | `GET /api/vehicules/disponibles` | Returns only vehicles with status `DISPONIBLE`, used by the booking engine. |
| **5** | **Search by Brand** | `GET /api/vehicules/search?marque=...` | Case-insensitive search allowing customers to look up cars by manufacturer name. |
| **6** | **Filter by Status** | `GET /api/vehicules/statut/{statut}` | Filters vehicles by `DISPONIBLE`, `LOUE`, `EN_MAINTENANCE`, or `HORS_SERVICE`. |
| **7** | **Filter by Category** | `GET /api/vehicules/categorie/{id}` | Retrieves all cars belonging to a specific category segment (SUV, Sedan, etc.). |
| **8** | **Lookup by Exact Plate** | `GET /api/vehicules/immatriculation/{plate}` | Fast exact lookup used during rental returns and police/insurance checks. |
| **9** | **Vehicle Status Update** | `PATCH /api/vehicules/{id}/statut` | Allows `reservation-service` and `maintenance-service` to transition car states (e.g. `LOUE`). |
| **10** | **Odometer Reading Update** | `PATCH /api/vehicules/{id}/kilometrage` | Updates mileage in kilometers upon vehicle return. |
| **11** | **Fleet Status Statistics** | `GET /api/vehicules/stats/count-by-statut` | Aggregates and groups vehicle counts by operational status for dashboard widgets. |
| **12** | **Vehicles-per-Category Counter** | `CategorieResponse.nombreVehicules` | Dynamically counts the active vehicles assigned to each category. |
| **13** | **Secure Image Upload (< 2.5 MB)** | `FileStorageService.saveFile` | Uploads car & category images into `/uploads/`, strictly enforcing a `< 2.5 MB` limit and image MIME-type validation. |
| **14** | **Dynamic Pagination & Sorting** | `GET /api/vehicules/page?page=0&size=6` | Page-by-page display (6 items per page) both via Spring Data `Pageable` in the REST API and live in the Backoffice UI. |

---

## 🎨 Backoffice Administration Panel

Accessible at **`http://localhost:8087/backoffice`**:
* **Dark Theme UI**: Clean, professional design with deep navy backgrounds and indigo accents.
* **Card-Based Views**: Interactive cards with vehicle details, category badges, and multiple images.
* **Live Dynamic Filtering**: Search by name/plate, filter by status or category, and adjust min/max mileage without page reloads.
* **Connected Dynamic Pagination**: Pagination automatically recalculates pages based on active filters and sorting criteria.
* **Auto-Slide Hover Carousel**: Vehicle cards showcase multiple images that cycle smoothly on mouse hover.
* **Client-Side Form Validation**: Real-time error alerts for empty inputs, invalid formats, and files exceeding 2.5 MB.

---

## 📡 REST API Reference Summary

### Categories (`/api/categories`)
* `GET /api/categories` — List all categories
* `GET /api/categories/page?page=0&size=6` — Paginated categories
* `GET /api/categories/{id}` — Get category by ID
* `GET /api/categories/{id}/vehicules` — Get all vehicles in a category
* `POST /api/categories` — Create a new category
* `PUT /api/categories/{id}` — Update an existing category
* `DELETE /api/categories/{id}` — Delete a category (refused if vehicles exist)
* `GET /api/categories/statut/{statut}` — Filter by active/inactive status
* `GET /api/categories/search?nom=...` — Search categories by name

### Vehicles (`/api/vehicules`)
* `GET /api/vehicules` — List all vehicles
* `GET /api/vehicules/page?page=0&size=6&sortBy=...&direction=...` — Paginated and sorted vehicles
* `GET /api/vehicules/{id}` — Get vehicle by ID
* `GET /api/vehicules/disponibles` — List all currently available vehicles
* `GET /api/vehicules/statut/{statut}` — Filter vehicles by status
* `GET /api/vehicules/categorie/{id}` — Filter vehicles by category ID
* `GET /api/vehicules/search?marque=...` — Search vehicles by brand name
* `GET /api/vehicules/immatriculation/{immatriculation}` — Lookup vehicle by license plate
* `POST /api/vehicules` — Register a new vehicle
* `PUT /api/vehicules/{id}` — Update an existing vehicle
* `DELETE /api/vehicules/{id}` — Delete a vehicle
* `PATCH /api/vehicules/{id}/statut?statut=...` — Update vehicle status (inter-service)
* `PATCH /api/vehicules/{id}/kilometrage?kilometrage=...` — Update vehicle mileage (inter-service)
* `GET /api/vehicules/stats/count-by-statut` — Vehicle counts grouped by status
* `GET /api/vehicules/stats/count-by-categorie/{id}` — Count of vehicles in a category

---

## 🚀 How to Run & Test

### 1. Start the Microservice
* In **IntelliJ IDEA**: Open the project, locate `VehiculeServiceApplication.java` and click **Run**.
* Via **Maven**:
  ```bash
  cd Backend/vehicule-service
  mvn spring-boot:run
  ```

### 2. Access the Application
* **Backoffice Admin**: [http://localhost:8087/backoffice](http://localhost:8087/backoffice)
* **Swagger API Documentation**: [http://localhost:8087/swagger-ui.html](http://localhost:8087/swagger-ui.html)
* **H2 Database Console**: [http://localhost:8087/h2-console](http://localhost:8087/h2-console)  
  *(JDBC URL: `jdbc:h2:mem:vehiculedb`, Username: `sa`, Password: empty)*
