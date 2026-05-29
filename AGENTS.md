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

1. Stabilize Order Management.
2. Build Billing workflows.
3. Replace the old Reports module with Invoice History.
4. Add a new Inventory Management module for Lodging and Restaurant.
5. Implement Reservation and Table workflows.
6. Build a project-aware AI Assistant.
7. Update Rooms search maintenance status workflow.
8. Enforce a consistent light-theme UI and reliable selection behavior across the app.
9. Add API usage logging / metrics.
10. Add third-party mock integration.
11. Add Analytics.

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