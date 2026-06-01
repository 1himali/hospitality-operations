# Himāli's Bed n Breakfast — Operations

Lodging and restaurant management system built for **Advanced Java LRMS**. A single Spring Boot application serves a REST API and a static browser UI for rooms, dining, billing, inventory, payroll, analytics, and role-based administration.

**Repository:** [github.com/1himali/hospitality-operations](https://github.com/1himali/hospitality-operations)

---

## Tech stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Build | Maven |
| Database | PostgreSQL (Flyway migrations) |
| Security | Spring Security, JWT (JJWT) |
| API docs | SpringDoc OpenAPI (`/swagger-ui.html`) |
| Caching | Caffeine |
| Metrics | Spring Actuator, Micrometer Prometheus |
| Frontend | Static HTML, CSS, vanilla JavaScript (`src/main/resources/static/`) |
| Tests | JUnit 5, Spring Boot Test, Testcontainers (PostgreSQL) |

---

## Features

### Lodging

- **Rooms** — CRUD, search, status updates (`VACANT`, `OCCUPIED`, `RESERVED`, `UNDER_MAINTENANCE`), notes, rate management (Admin/Owner for writes).
- **Action items (Tasks)** — CRUD and status workflow for operational tasks (Admin, Owner, Manager).

### Restaurant

- **Menu** — Menu item CRUD, categories, availability toggle.
- **Orders** — Full CRUD with date filters (Today, This Week, This Month, Custom Range, All).
- **Tables** — Dining table management and status.
- **Billing** — Generate bills from orders, rooms, and manual line items; dropdown and manual entry modes; tax and discount support.
- **Invoice history** — List, search, sort, pagination, detail view, CSV export, invoice flagging (Admin/Owner/Manager for listing; Admin/Owner for edits).

### Operations & admin

- **Inventory** — Lodging and restaurant stock with quantity, reorder levels, and status tracking (Admin, Owner, Manager).
- **Calculator** — In-app billing calculator workflow.
- **AI assistant** — Rule-based queries over in-app data only (orders, rooms, tables, tasks, inventory). If data is not available, the assistant responds with: *"Information not available in the system."*
- **API metrics** — Request logging, latency, failure tracking; CSV/JSON export (Admin, Owner).
- **Mock mode** — In-memory toggle to route third-party integration to mock implementations (Admin, Owner).
- **Analytics** — Aggregations from internal data: revenue, invoices, rooms, tables, billing, inventory (Admin, Owner).
- **Activity log** — Audited actions with search and filters (Admin, Owner).
- **User management** — List, create, reset password, delete users (Admin only).
- **Payroll** — Employee CRUD, payroll records (bonus, deductions, net pay), CSV export (Admin, Owner).

### Integrations

- **Third-party orders** — `/api/v1/third-party` listings and order placement; uses mock or production service depending on mock mode.
- **Groq** — Configuration present in `application.yml` (`GROQ_API_KEY`); `GroqClient` is a placeholder and invoice descriptions use a local stub (`InvoiceDescriptionService`).

---

## Project layout

```
operations/
├── pom.xml
├── .env.example                 # Environment variable template (copy to .env)
├── AGENTS.md                    # Contributor/agent conventions for this repo
├── src/main/java/com/hospitality/operations/
│   ├── OperationsApplication.java
│   ├── admin/                   # User management
│   ├── ai/                      # Invoice description stub, Groq DTOs
│   ├── auth/                    # JWT login/register, seed data
│   ├── config/                  # Security, WebMvc, OpenAPI, WebClient
│   ├── dashboard/               # Analytics, API metrics
│   ├── domain/                  # Rooms, restaurant, inventory, payroll, assistant, activity
│   ├── integration/             # Mock mode, third-party orders
│   ├── interceptor/             # API usage logging
│   └── lodging/                 # Action items
├── src/main/resources/
│   ├── application.yml
│   ├── db/migration/            # Flyway V1–V15
│   └── static/                  # index.html, css/index.css, js/app.js
└── src/test/java/               # 12 test classes (85 tests in suite)
```

---

## Prerequisites

1. **JDK 21**
2. **Maven 3.9+**
3. **PostgreSQL** reachable at the URL configured in `application.yml`

Default database settings (from `src/main/resources/application.yml`):

| Setting | Value |
|---------|--------|
| URL | `jdbc:postgresql://localhost:5432/hospitality_db` |
| Username | `hospitality_user` |
| Password | `${DB_PASSWORD}` (from environment) |

Create the database and user in PostgreSQL before first run, for example:

```sql
CREATE USER hospitality_user WITH PASSWORD 'your_password';
CREATE DATABASE hospitality_db OWNER hospitality_user;
```

Adjust credentials to match your environment.

---

## Configuration

Copy the example environment file and set secrets:

```bash
cp .env.example .env
```

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_PASSWORD` | Yes | PostgreSQL password for `hospitality_user` |
| `JWT_SECRET` | Yes | At least 32 characters; used for JWT signing |
| `GROQ_API_KEY` | Optional | Groq API key (for future AI features; assistant currently uses internal data) |

Export variables in your shell (or use a tool that loads `.env` before starting the app):

```bash
# Windows PowerShell
$env:DB_PASSWORD="your_postgres_password"
$env:JWT_SECRET="your_256bit_secret_key_minimum_32_chars"
$env:GROQ_API_KEY="gsk_..."   # optional

# Linux / macOS
export DB_PASSWORD=your_postgres_password
export JWT_SECRET=your_256bit_secret_key_minimum_32_chars
export GROQ_API_KEY=gsk_...   # optional
```

`.env` is gitignored; never commit secrets.

---

## Run the application

From the project root:

```bash
mvn spring-boot:run
```

Then open:

| Resource | URL |
|----------|-----|
| Web UI | http://localhost:8080/ |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| Health | http://localhost:8080/actuator/health |

On startup, `DataInitializer` seeds demo users, rooms, menu items, tables, inventory, action items, and sample bills when tables are empty.

---

## Default login accounts

Seeded on first run (if usernames do not already exist):

| Username | Password | Role | Typical access |
|----------|----------|------|----------------|
| `user` | `1234` | User | Rooms, restaurant, orders, tables, billing, calculator, assistant |
| `manager` | `mngr` | Manager | User access + Tasks, Inventory, Invoice History |
| `admin` | `abcd` | Admin | Manager access + User Mgmt, API Metrics, Mock Mode, Analytics, Activity, Payroll; invoice/menu/room price edits |
| `owner` | `4321` | Owner | Same as Admin for API rules in `SecurityConfig` (payroll, analytics, mock mode, metrics, activity) |

The UI hides sidebar items based on role. Page-level guards show “Access denied” when a role opens a restricted screen.

New accounts can also be created via `POST /api/v1/auth/register` (defaults to `ROLE_USER`).

---

## Role-based API access (summary)

Enforced in `SecurityConfig`. Authenticated users can reach most read/write APIs unless listed below.

| Path pattern | Roles |
|--------------|-------|
| `/api/v1/auth/**` | Public |
| `/`, `/index.html`, `/css/**`, `/js/**` | Public |
| `/api/v1/admin/payroll/**` | Admin, Owner |
| `/api/v1/admin/users/**` | Admin |
| `/api/v1/admin/config/**` | Owner |
| `/api/v1/admin/mock-mode/**` | Admin, Owner |
| `/api/v1/admin/analytics/**` | Admin, Owner |
| `/api/v1/metrics/**` | Admin, Owner |
| `/api/v1/admin/activity/**` | Admin, Owner |
| `/api/v1/assistant/admin/**` | Admin, Owner |
| `/api/v1/action-items/**` | Admin, Owner, Manager |
| `/api/v1/inventory/**` | Admin, Owner, Manager |
| `PUT`/`DELETE` `/api/v1/bills/**` | Admin, Owner |
| `GET` `/api/v1/bills`, `/api/v1/bills/export/**` | Admin, Owner, Manager |
| `POST`/`PUT` `/api/v1/menu/**` | Admin, Owner |
| `POST`/`PUT` `/api/v1/rooms/**` | Admin, Owner |
| `POST`/`PUT` `/api/v1/tables/**` | Admin, Owner |
| `DELETE` `/api/v1/tables/**` | Admin |
| Other `/api/v1/**` | Any authenticated role |

Send the JWT on API calls: `Authorization: Bearer <token>` (returned from login).

---

## Main API endpoints

| Area | Base path |
|------|-----------|
| Auth | `/api/v1/auth` (`login`, `register`) |
| Rooms | `/api/v1/rooms` |
| Menu | `/api/v1/menu` |
| Orders | `/api/v1/orders` |
| Tables | `/api/v1/tables` |
| Bills | `/api/v1/bills` (includes `/{id}/flag`, `/export/csv`) |
| Action items | `/api/v1/action-items` |
| Inventory | `/api/v1/inventory` |
| Assistant | `/api/v1/assistant/query` |
| Metrics | `/api/v1/metrics` |
| Analytics | `/api/v1/admin/analytics` |
| Mock mode | `/api/v1/admin/mock-mode` |
| Activity | `/api/v1/admin/activity` |
| Users | `/api/v1/admin/users` |
| Payroll employees | `/api/v1/admin/payroll/employees` |
| Payroll records | `/api/v1/admin/payroll/records` |
| Third party | `/api/v1/third-party` |

---

## Database migrations

Flyway scripts in `src/main/resources/db/migration/`:

| Version | File | Purpose |
|---------|------|---------|
| V1 | `V1__init_schema.sql` | Rooms, users, API usage logs |
| V2 | `V2__restaurant_schema.sql` | Restaurant domain tables |
| V3 | `V3__action_items.sql` | Action items |
| V4–V5 | User seed/cleanup | Test user adjustments |
| V6 | `V6__alter_bills_nullable_order_id.sql` | Billing schema |
| V7 | `V7__inventory.sql` | Inventory |
| V8 | `V8__merge_maint_statuses.sql` | Room maintenance status cleanup |
| V9 | `V9__invoice_customer_fields.sql` | Invoice customer fields |
| V11 | `V11__api_metrics_username.sql` | Metrics username |
| V12 | `V12__payroll.sql` | Payroll employees |
| V13 | `V13__payroll_records.sql` | Payroll records |
| V14 | `V14__activity_logs.sql` | Activity logs |
| V15 | `V15__invoice_flagging_and_notifications.sql` | Bill flagging; notifications table |

`spring.jpa.hibernate.ddl-auto` is `validate`; schema changes must go through Flyway.

---

## Tests

```bash
mvn test
```

Set `DB_PASSWORD` and `JWT_SECRET` in the environment before running tests (same as runtime). The suite includes unit and slice tests; some tests use Testcontainers for PostgreSQL.

---

## UI overview

- **Branding:** “Himāli's Bed n Breakfast” / “An app for Adv. Java LRMS”
- **Theme:** Light warm palette (Noto Sans, Jua headings, Gaegu accent on login)
- **Navigation:** Sidebar for Reservations (rooms), Restaurant hub (menu, orders, tables, billing), Tasks, Inventory, Invoice History, Calculator, Assistant, and admin sections (role-gated)
- **Floating assistant:** Quick chat widget on authenticated pages (same backend as Assistant page)

Static assets are served from the classpath; no separate frontend build step.

---

## Development notes

- **Multi-tenant field:** Entities use `tenant_schema` (default `default`); seeded data uses this value.
- **Tax rate:** Bills use a default tax rate of 8.875% in `BillServiceImpl`.
- **API usage:** `ApiUsageInterceptor` records endpoint, method, status, and duration for metrics.
- **Contributor conventions:** See `AGENTS.md` for project-specific rules (RBAC, UI, assistant data restrictions).

---

## License

No license file is included in this repository. Contact the repository owner for usage terms.
