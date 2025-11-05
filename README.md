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
- **Service layer**: `OrderService` applies validation, orchestrates persistence, and exposes a dedicated command for order creation.
- **Web layer**: REST controller with request/response DTOs, validation, and a mapper to isolate transport concerns from the domain.
- **Scheduler**: `OrderStatusScheduler` promotes all pending orders to processing every 5 minutes.
- **Persistence**: `OrderRepository` backed by JPA/Hibernate on H2 (in-memory by default).

## API Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/orders` | Create a new order with one or more items |
| `GET` | `/api/v1/orders/{id}` | Fetch full order details by id |
| `GET` | `/api/v1/orders?status=PROCESSING` | List orders, optionally filtered by status |
| `PATCH` | `/api/v1/orders/{id}/status` | Progress an order to the next status (`PROCESSING`, `SHIPPED`, `DELIVERED`) |
| `POST` | `/api/v1/orders/{id}/cancel` | Cancel a pending order |

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

## Scheduled Processing
- `OrderStatusScheduler` runs every 5 minutes (`cron: 0 */5 * * * *`).
- The job promotes all orders that are still `PENDING` to `PROCESSING`. The scheduler logs the number of orders updated on each run.

## Running the Application

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080`.

### H2 Console
- Enabled at `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:orders`
- Username: `sa`, Password: *(leave blank)*

## Testing

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
./mvnw clean verify
```

Integration tests cover order creation, valid/invalid status transitions, cancellation rules, and the scheduled promotion logic.

## Future Enhancements
- Add authentication/authorization for admin vs. customer flows.
- Introduce pagination and sorting for the list endpoint.
- Move from in-memory H2 to an external database profile (PostgreSQL/MySQL).
- Add metrics (Micrometer) and tracing for observability.

## AI Assistance Notes
- **Planning & scaffolding**: Used Cursor/ChatGPT to outline the architecture, layer responsibilities, and REST endpoints before coding.
- **Code generation**: Leveraged the assistant for repetitive boilerplate (e.g., DTOs, controller signatures) while reviewing and refining the generated code manually for correctness and readability.
- **Issue discovery & fixes**: During testing the assistant helped reason about a failing scheduled promotion test by highlighting the need to reset state between executions; the fix was to clean the repository before each test.
- **Validation**: Relied on the assistant to double-check status transition rules and ensure mismatch errors are surfaced via the global exception handler.

All committed code has been reviewed, formatted, and executed locally to confirm the API behaves as documented.

