# Operations Project Instructions

You are working on an existing Spring Boot 3.5.14 + Java 21 + Maven project called `operations`.

## Core rules

* Treat the repository as the source of truth.
* Do not rewrite the app from scratch.
* Do not touch unrelated modules.
* Remove dark mode completely, along with theme switching logic and unused theme state. Use a consistent light theme across the application.
* Keep the existing code style and conventions.
* Do not remove working behavior unless explicitly requested.
* Do not invent features not requested.
* The AI assistant must be restricted to project data only. Do not use external knowledge or general-purpose AI behavior.
* If requested information is not available in project data, respond strictly with: "Information not available in the system."

## Current backlog

✅ All 11 backlog items are complete.

| # | Module | Status |
|---|--------|--------|
| 1 | Stabilize Order Management | ✅ Complete |
| 2 | Build Billing workflows | ✅ Complete |
| 3 | Replace old Reports with Invoice History | ✅ Complete |
| 4 | Inventory Management (Lodging & Restaurant) | ✅ Complete |
| 5 | Reservation and Table workflows | ✅ Complete |
| 6 | Project-aware AI Assistant | ✅ Complete |
| 7 | Rooms search maintenance status cleanup | ✅ Complete |
| 8 | Light-theme UI + selection behavior | ✅ Complete |
| 9 | API usage logging / metrics | ✅ Complete |
| 10 | Third-party mock integration | ✅ Complete |
| 11 | Analytics views | ✅ Complete |

## RBAC Enhancements

Four-role system implemented:

| Role | Credentials | Access |
|------|-------------|--------|
| **User** | `user` / `1234` | Basic app workflows (rooms, menu, orders, tables, billing, calculator). **Restricted from INVOICE HISTORY, TASKS, Inventory, User Management, Payroll.** |
| **Manager** | `manager` / `mngr` | Everything User can, plus Inventory CRUD and TASKS. **Restricted from billing price editing, invoice editing, User Management, Payroll.** |
| **Admin** | `admin` / `abcd` | User + Invoice Management, User CRUD, password resets, Analytics, API Metrics (incl. CSV/JSON export), Mock Mode toggle, Assistant enable/disable, TASKS, Inventory. **Restricted from Payroll.** |
| **Owner** | `owner` / `4321` | Full access including Payroll Manager CRUD, Payroll CSV export, Payroll Records (bonus/deductions/history), System Configuration, TASKS, Inventory. |

### Backend changes
- **SecurityConfig** — Complete role-based endpoint rules: `ROLE_OWNER` for payroll, `ROLE_ADMIN`+`ROLE_OWNER` for user mgmt, metrics, mock mode, analytics, assistant admin; `ROLE_USER`+ for all other API; `ROLE_MANAGER` added to inventory + action-items; action-items restricted from `ROLE_USER`; invoice history GET listing + CSV export restricted to `ROLE_ADMIN`+`ROLE_OWNER`+`ROLE_MANAGER`
- **DataInitializer** — Updated `admin` password to `abcd`, added `owner`/`4321` seed, added `manager`/`mngr` seed
- **V12 migration** — `payroll_employees` table
- **V13 migration** — `payroll_records` table (employee_id FK, base_salary, bonus, deductions, net_pay, payment_date, notes, status)
- **PayrollEmployee** entity, repository, service, controller (`/api/v1/admin/payroll/employees/**`) — Owner-only CRUD, CSV export, search/filter by name/status/department
- **PayrollRecord** entity, repository, service, controller (`/api/v1/admin/payroll/records/**`) — Owner-only CRUD with auto-calculated net_pay
- **UserManagementController** (`/api/v1/admin/users/**`) — Admin/Owner: list, create, password reset
- **ApiMetricsController** — Added `/export/csv` and `/export/json` endpoints (Admin/Owner)
- **ApiUsageLogService** — Added `exportCsv()` and `exportJson()` methods
- **BillController** — Added `/export/csv` endpoint (Admin/Owner/Manager) with date-filtered CSV export
- **BillServiceImpl** — Added `exportCsv()` method

### Frontend changes
- **Sidebar** — "USER MGMT", "API METRICS", "MOCK MODE", "ANALYTICS", "PAYROLL" visible to Admin/Owner; "TASKS", "INVENTORY", "INVOICE HISTORY" visible to Admin/Owner/Manager (all hidden for User role)
- **`updateSidebarVisibility()`** — Extended to show/hide TASKS, INVENTORY, INVOICE HISTORY, API METRICS, MOCK MODE, ANALYTICS sidebar items via `isAdminOrOwner()` / `isAdminOrOwnerOrManager()`
- **Page-level guards** — `loadApiMetrics()`, `loadAnalytics()`, `loadMockModePage()` now block non-admin/owner users with "Access denied" message (replacing page content); `loadActionItems()` blocks non-admin/owner/manager; `loadInventory()` blocks non-admin/owner/manager; `loadInvoiceHistory()` blocks non-admin/owner/manager; matches pattern already used by `loadUserMgmt()` and `loadPayroll()`
- **API Metrics export buttons** — Effectively hidden from User role because the page-level guard replaces the entire page content before User reaches them
- **Mock Mode controls** — Effectively hidden from User role via the same page-level guard mechanism
- **User Management page** — list users, add user, reset password, delete user
- **Payroll page** — Employee cards with search bar (name), status filter, department filter. Click employee card to highlight. "RECORDS" button opens payroll history panel with bonus/deductions/net_pay breakdown. Add/edit/delete employees with joining date and status. CSV export download.
- **API Metrics page** — added Export CSV/JSON buttons that download via browser flow
- **Payroll Record modal** — bonus, deductions, payment date, status, notes with auto-calculated net pay
- **`isOwner()`** and **`updateSidebarVisibility()`** functions  
- **CSS** — `.payroll-layout` two-column layout, `.payroll-records-panel` sticky records panel, `.payroll-record-row` card with date/status/details/notes/actions

## Product requirements

* **Order management:** Must include full CRUD and date filters (Today, This Week, This Month, Custom Range, All). Every report, calculation, and displayed record must respect the active date filter.
* **Billing:** Must support both dropdown selection mode and manual entry mode. Items must be selectable throughout the billing workflow.
* **Invoice history:** Must include full listing, search, sorting, pagination, and invoice detail view. Must use the same date filters as Order Management and be available as a universal module across the app.
* **Inventory:** Must support both Lodging and Restaurant Inventory with full CRUD, search, filtering, status tracking, quantity management, and validation.
* **Assistant:** Must query and restrict answers to internal app-managed project data sources only.
* **UI/UX:** Selection must work reliably across all selectable UI components.
* **Rooms search:** Remove the obsolete "maintained" filter, migrate "maint" content into "under maintenance", and delete "maint".
* **API metrics:** Track API usage, latency, and failures in a way that fits the existing application structure.
* **Mock integration:** Support isolated third-party mocks for development and testing without affecting production behavior.
* **Analytics:** Add analytics views or aggregations based on internal project data only.

## Suggested implementation order

1. UI Standardization (Light theme enforcement and selection behavior)
2. Order Management (CRUD and date filters)
3. Billing Workflows (Dropdown and manual entry support)
4. Invoice History (Universal module replacement for Reports)
5. Inventory Module (Lodging and Restaurant)
6. Reservation and Table Workflows
7. Rooms Search Maintenance Status Cleanup
8. Project-aware AI Assistant
9. API Usage Logging / Metrics
10. Third-party Mock Integration
11. Analytics

## Coding style

* Prefer clear separation of concerns.
* Use DTOs for request/response boundaries.
* Use service and repository layers.
* Use mappers where helpful.
* Validate inputs at the edge.
* Return consistent API error responses.
* Keep UI state predictable and centralized where needed.

## Workflow

* Start by inspecting the codebase and identifying the current implementation.
* Report the plan before editing if the task spans multiple areas.
* Implement one module at a time.
* After finishing a module, stop and wait for confirmation unless asked to continue.
* Include tests or verification steps for each change.

## Safety

* Inspect before editing.
* Make the smallest safe change that satisfies the request.
* Prefer incremental, reversible changes.
* If requirements are ambiguous, ask before editing.
* When a request conflicts with current behavior, explain the tradeoff and ask before proceeding.

## Database guidance

* Prefer explicit migrations for schema changes.
* Do not change database structure casually.
* Preserve existing data when possible.
* Design schemas to support future expansion cleanly.

## Testing expectations

* Update or add tests for changed behavior.
* Verify affected backend, frontend, and integration paths.
* Re-run targeted tests after each meaningful change.
* Do not mark work complete without checking for obvious regressions.

## Output style

* Be direct and concise.
* State what you inspected.
* State what you plan to change.
* State what was changed.
* State any risks or follow-ups.

## Sidebar Icon Inventory

| # | Label | SVG Paths (abbreviated) | Visual Meaning |
|---|-------|------------------------|----------------|
| 1 | RESERVATIONS | `<rect x="2" y="7" w="20" h="14"/>` + `<path d="M16 7V5a4 4 0 0 0-8 0v2"/>` | Building/hotel with door and roof |
| 2 | RESTAURANT | Cup path + 3 steam lines | Coffee cup with steam |
| 3 | TASKS | Document path + polyline + 2 lines | Clipboard with checklines |
| 4 | INVENTORY | Box rect + lid line | Crate/box |
| 5 | INVOICE HISTORY | Three descending bar lines | Bar chart (invoice/report) |
| 6 | CALCULATOR | Rect + 9 grid lines | Calculator with button grid |
| 7 | ASSISTANT | Circle face + 2 dot eyes | Face (AI chat) |
| 8 | API METRICS | Three vertical bar paths | Stats bars (metrics) |
| 9 | MOCK MODE | Box + plus cross lines | Toggle/simulation |
| 10 | ANALYTICS | Polyline ascending | Trend/line chart |
| 11 | USER MGMT | Two people paths | Users |
| 12 | PAYROLL | Wallet + coin paths | Payroll/money |

**Note:** All icons are 18×18px SVG, stroke-width="2", inside `.sidebar-item` with 10px gap. No icon movement applied.

## Branding & UI Redesign

### Brand Names (suggested)
1. **Cafe Hana** (하나 — "one/unity" in Korean) — warm, simple, works as software brand
2. **Mochi** — soft, cute, internationally recognized
3. **Dalgona** (달고나) — Korean honeycomb toffee, trendy, distinctly Seoul
4. **Seoul & Co.** — professional but playful, city-inspired

### Fonts (Google Fonts)
| Role | Font | Usage |
|------|------|-------|
| Primary (body) | Noto Sans | All body text, inputs, buttons, tables |
| Heading | Jua | Page titles, topbar title, section headers, login brand |
| Secondary | Gaegu | Decorative/accent text (login subtitle) |

### Color Palette (Seoul Cafe Warm)
| Token | Color | Description |
|-------|-------|-------------|
| `--bg-app` | `#FDF6F0` | Warm cream page background |
| `--bg-surface` | `#FFFCF9` | Warm white cards/surfaces |
| `--bg-surface-alt` | `#FFF0E6` | Seashell warm hover/alt |
| `--border` | `#E8D5C4` | Warm beige borders |
| `--border-light` | `#F0E6D8` | Lighter beige |
| `--border-dashed` | `#D4BFAA` | Dashed border beige |
| `--text-primary` | `#3D2C2C` | Warm dark brown text |
| `--text-secondary` | `#7A6B5D` | Warm gray-brown |
| `--text-muted` | `#A89888` | Muted warm gray |
| `--text-inverse` | `#FFFCF9` | White on accent |
| `--accent` | `#E8916E` | Coral/terracotta (cafe terrace) |
| `--accent-hover` | `#D47A58` | Darker coral |
| `--danger` | `#D46767` | Soft rose red |
| `--success` | `#7FB07F` | Matcha/sage green |
| `--warning` | `#E8B86E` | Warm amber |
| `--radius` | `6px` / `10px` | Softer rounding |

### Header Changes
- **Home icon** added immediately after back button (house SVG, navigates to landing page via `btn-home` click handler)
- **Title** changed to "HIMALI'S MANAGEMENT SYSTEM" rendered in Jua heading font
- **Logout button** redesigned: pill shape, accent coral bg, shows `LOGOUT <span class="logout-username">username</span>`, updated via `updateLogoutButton()`
- **Back/Home icons**: hover changes border+color to accent coral
- `.topbar-title`: Jua font, larger size, centered, uppercase with lighter letter-spacing

### Logout Button Behavior
- Login handler stores username in `localStorage.setItem('username', username)`
- `updateLogoutButton()` reads `localStorage.getItem('username')` and sets `btn.innerHTML = 'LOGOUT <span class="logout-username">' + uname + '</span>'`
- Called on login, page refresh (DOMContentLoaded), and every `showPage()` navigation
- Logout clears username from localStorage

### Floating Assistant Widget
- **Position**: `position: fixed; bottom: 80px; right: 24px; z-index: 850` — above billing FAB (at bottom:24px, z-index:900)
- **Button**: 52px circle, accent coral, assistant face icon
- **Popup**: 340×420px max, absolute above button (bottom:60px), `.assistant-popup` with header + scrollable messages + input bar
- **Functionality**: click button toggles popup; uses same `API.assistant` endpoint; separate message list from page assistant
- **Show/hide**: popup hidden initially; click button to open; close button or re-click to hide
- **Responsive**: at 480px, popup narrows to 290px

### Login Page
- "CAFE HANA" in Jua heading, accent coral, uppercase
- "himālī's management" subtitle in muted text (Gaegu secondary font)
- "SIGN IN" button updated to use accent coral + rounded corners

### What Did NOT Change
- Wireframe structure (topbar height 52px, sidebar 200px, page layout, content area)
- Sidebar icon SVGs (no movement, no replacement — inventory-only)
- Navigation logic, back button, page hierarchy
- Backend code, API endpoints, SecurityConfig
- All 85 backend tests still pass