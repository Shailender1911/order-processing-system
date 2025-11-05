# Order Processing System

Backend take-home assignment for PeerIslands. The application exposes a REST API that allows customers to create, view, update and cancel orders, while a scheduled background job periodically progresses orders from `PENDING` to `PROCESSING`.

## Tech Stack
- Java 17, Spring Boot 3.4.0
- Spring Web, Spring Data JPA, Spring Validation
- H2 in-memory database for local development and tests
- Maven Wrapper for build & dependency management

## Architecture Overview
- **Domain layer**: `Order`, `OrderItem`, and `OrderStatus` model the aggregate and encapsulate business rules.
- **Order identifiers**: Each order receives a human-friendly identifier (`ORD-YYYYMMDD-XXXXXX`) generated via `OrderNumberGenerator`, which is the only ID exposed through the API.
- **Inventory management**: `InventoryItem` entities track on-hand and reserved stock per SKU with pessimistic locking so concurrent orders cannot oversell. Reservations are made during order creation, released on cancellation, and committed once the order enters `PROCESSING`.
- **Service layer**: `OrderService` applies validation, orchestrates persistence, and exposes a dedicated command for order creation.
- **Web layer**: REST controller with request/response DTOs, validation, and a mapper to isolate transport concerns from the domain.
- **Scheduler**: `OrderStatusScheduler` promotes all pending orders to processing every 5 minutes.
- **Persistence**: `OrderRepository` backed by JPA/Hibernate on H2 (in-memory by default).

## Getting Started

1. Install Java 17 and ensure the `JAVA_HOME` environment variable points to it.
2. (MySQL only) Create the schema and user credentials you plan to use:

```sql
CREATE DATABASE orders CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. Clone the repository and change into the project directory.
4. Update `src/main/resources/application-mysql.properties` if your MySQL host, port, or credentials differ from the defaults (`root` / `root123@#`).

### Choosing a Database Profile

- **MySQL (default)** – no additional flags required.
  ```bash
  export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
  ./mvnw spring-boot:run
  ```
- **H2 in-memory** – quick demos & automated tests.
  ```bash
  export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
  SPRING_PROFILES_ACTIVE=h2 ./mvnw spring-boot:run
  ```

When the app starts it listens on `http://localhost:8080`. The scheduled job and manual maintenance endpoints work in both profiles. H2 also exposes the console at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:orders`, user `sa`).

### Useful SQL for MySQL

Keep an eye on inventory after each order:

```sql
SELECT product_code, product_name, stock_on_hand, reserved_quantity
FROM inventory_items
ORDER BY product_code;
```

Seed data is not loaded automatically for MySQL; populate rows manually or reuse the statements in `data-h2.sql`.

## API Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/orders` | Create a new order (reserves inventory per SKU) |
| `GET` | `/api/v1/orders` | List orders, optionally filtered by status (`?status=PROCESSING`) |
| `GET` | `/api/v1/orders/{orderId}` | Fetch full order details by public order number |
| `PATCH` | `/api/v1/orders/{orderId}/status` | Progress an order (`PROCESSING`, `SHIPPED`, `DELIVERED`) |
| `POST` | `/api/v1/orders/{orderId}/cancel` | Cancel a pending order and release reservations |
| `POST` | `/internal/tools/orders/promote-pending` | Manually trigger the scheduled promotion of pending orders |

### Sample Create Order Request

```json
POST /api/v1/orders
{
  "customerName": "Jane Doe",
  "customerEmail": "jane.doe@example.com",
  "shippingAddress": "221B Baker Street, London",
  "items": [
    {
      "productCode": "SKU-123",
      "productName": "Wireless Mouse",
      "quantity": 1,
      "unitPrice": 15.00
    },
    {
      "productCode": "SKU-999",
      "productName": "Mechanical Keyboard",
      "quantity": 1,
      "unitPrice": 25.00
    }
  ]
}
```

### Sample Order Response

```json
{
  "orderId": "ORD-20251105-1AB2C3",
  "customerName": "Jane Doe",
  "customerEmail": "jane.doe@example.com",
  "shippingAddress": "221B Baker Street, London",
  "status": "PENDING",
  "totalAmount": 40.00,
  "createdAt": "2025-11-03T14:38:27.123Z",
  "updatedAt": "2025-11-03T14:38:27.123Z",
  "items": [
    {
      "id": 1,
      "productCode": "SKU-123",
      "productName": "Wireless Mouse",
      "quantity": 1,
      "unitPrice": 15.00,
      "lineTotal": 15.00
    }
  ]
}
```

### Status Rules
- Orders start in `PENDING`.
- Valid transitions:
  - `PENDING → PROCESSING` (background job or manual request)
  - `PROCESSING → SHIPPED`
  - `SHIPPED → DELIVERED`
- Cancelling is only allowed while the order is still `PENDING`.
- `CANCELLED` and `DELIVERED` are terminal states.


## cURL Cookbook

Create an order (inventory is reserved immediately):

```bash
curl --location --request POST 'http://localhost:8080/api/v1/orders' \
  --header 'Content-Type: application/json' \
  --data '{
    "customerName": "Jane Doe",
    "customerEmail": "jane.doe@example.com",
    "shippingAddress": "221B Baker Street, London",
    "items": [
      { "productCode": "SKU-123", "productName": "Wireless Mouse", "quantity": 1, "unitPrice": 15.00 },
      { "productCode": "SKU-999", "productName": "Mechanical Keyboard", "quantity": 1, "unitPrice": 25.00 }
    ]
  }'
```

List all orders:

```bash
curl --location 'http://localhost:8080/api/v1/orders'
```

Filter by status:

```bash
curl --location 'http://localhost:8080/api/v1/orders?status=PROCESSING'
```

Fetch a single order (replace `<ORDER_ID>` with the value returned from create/list calls):

```bash
curl --location "http://localhost:8080/api/v1/orders/<ORDER_ID>"
```

Advance an order to the next state:

```bash
curl --location --request PATCH "http://localhost:8080/api/v1/orders/<ORDER_ID>/status" \
  --header 'Content-Type: application/json' \
  --data '{ "status": "PROCESSING" }'
```

Cancel a pending order:

```bash
curl --location --request POST "http://localhost:8080/api/v1/orders/<ORDER_ID>/cancel"
```

Trigger the background promotion job on demand (same effect as waiting for the cron):

```bash
curl --location --request POST 'http://localhost:8080/internal/tools/orders/promote-pending'
```

## Scheduled Processing
- `OrderStatusScheduler` runs every 5 minutes (`cron: 0 */5 * * * *`).
- The manual endpoint above is useful for demos when you do not want to wait for the next tick.


## Testing

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
./mvnw clean verify
```

Integration tests cover order creation, valid/invalid status transitions, cancellation rules, and the scheduled promotion logic.

## Future Enhancements
- Add authentication/authorization for admin vs. customer flows.
- Introduce pagination and sorting for the list endpoint.
- Inventory admin APIs (CRUD) and asynchronous reservation expiration handling.
- Move from in-memory H2 to an external database profile (PostgreSQL/MySQL).
- Add metrics (Micrometer) and tracing for observability.

## AI Assistance Notes
- **Planning & scaffolding**: Used Cursor/ChatGPT to outline the architecture, layer responsibilities, and REST endpoints before coding.
- **Code generation**: Leveraged the assistant for repetitive boilerplate (e.g., DTOs, controller signatures) while reviewing and refining the generated code manually for correctness and readability.
- **Issue discovery & fixes**: During testing the assistant helped reason about a failing scheduled promotion test by highlighting the need to reset state between executions; the fix was to clean the repository before each test.
- **Validation**: Relied on the assistant to double-check status transition rules and ensure mismatch errors are surfaced via the global exception handler.

All committed code has been reviewed, formatted, and executed locally to confirm the API behaves as documented.

