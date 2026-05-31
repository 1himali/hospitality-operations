# Himāli's Bed n Breakfast

An app for Adv. Java LRMS — a Lodging and Restaurant Management System built as a full-stack Java web application.

## Technology Stack

- **Backend:** Java 21, Spring Boot 3.5.14, Spring Security, Spring Data JPA
- **Frontend:** Vanilla HTML/CSS/JS (single-page application)
- **Database:** H2 (development), PostgreSQL (production-ready via config)
- **Build:** Maven
- **Testing:** JUnit 5, Spring Boot Test, Mockito, REST Assured

## How to Run Locally

1. **Prerequisites:** Java 21+, Maven 3.9+

2. **Clone and build:**
   ```
   mvn clean package -DskipTests
   ```

3. **Run:**
   ```
   mvn spring-boot:run
   ```

4. **Access:** Open `http://localhost:8080` in your browser.

5. **Run tests:**
   ```
   mvn test
   ```

## User Roles & Access

Three roles are supported:

| Role   | Credentials          | Access                                                                    |
|--------|----------------------|---------------------------------------------------------------------------|
| User   | `user` / `1234`      | Rooms, menu, orders, tables, billing, inventory, action items, assistant, calculator, invoice history |
| Admin  | `admin` / `abcd`     | User access + user management, API metrics (CSV/JSON export), analytics, mock mode toggle. **No payroll access.** |
| Owner  | `owner` / `4321`     | All access including payroll (employees, records, CSV export), system configuration |

## Modules

- **Rooms Search** — Browse, filter, and manage lodging rooms by status, type, and floor.
- **Action Items** — Housekeeping and maintenance task management with status tracking.
- **Menu Management** — CRUD for restaurant menu items with categories and availability.
- **Order Management** — Create and manage dining orders with date range filters.
- **Table Management** — Dining table status overview and management.
- **Billing** — Invoice generation with room charges, menu items, manual entry, discounts, and tax.
- **Invoice History** — Search, sort, and paginate past invoices with detail views.
- **Inventory** — Lodging and restaurant inventory tracking with stock levels and reorder management.
- **Calculator** — Built-in calculator with tax and discount functions.
- **Assistant** — AI-powered query assistant restricted to internal application data.
- **API Metrics** — Request tracking, latency monitoring, endpoint usage (admin/owner).
- **Mock Mode** — Third-party simulation toggle for development (admin/owner).
- **Analytics** — Revenue, occupancy, billing, and inventory aggregations (admin/owner).
- **User Management** — User CRUD and password resets (admin/owner).
- **Payroll** — Employee management, payroll records with bonus/deductions, CSV export (owner only).

## Setup Requirements

- No environment variables are required for local development — H2 runs in-memory with demo data auto-seeded on startup.
- For production, configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
- The application uses a single-page frontend served from `src/main/resources/static/`.
