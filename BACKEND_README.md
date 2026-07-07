# Spare Parts Inventory Management System — Spring Boot Backend

## Project Status: ✅ COMPLETE & READY FOR DEPLOYMENT

**Technology Stack:**
- Spring Boot 3.2.0
- Spring Security with JWT
- Spring Data JPA
- H2 Database (Development)
- PostgreSQL (Production)
- Swagger/OpenAPI 3.0

---

## Quick Start

### 1. H2 Database (Development)
```bash
# H2 is in-memory, no setup needed
# Access H2 Console: http://localhost:8080/api/h2-console
# Default credentials: sa / (no password)
mvn spring-boot:run
```

### 2. PostgreSQL (Production)
```bash
# First, create PostgreSQL database
createdb spare_parts_db

# Then run with PostgreSQL profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgresql"
```

### 3. Build
```bash
mvn clean install
mvn spring-boot:run
```

---

## Database Profiles

### Development (H2)
- **File:** `application-h2.properties`
- **URL:** `jdbc:h2:mem:spareparts`
- **Console:** http://localhost:8080/api/h2-console
- **Auto-creates tables on startup**

### Production (PostgreSQL)
- **File:** `application-postgresql.properties`
- **URL:** `jdbc:postgresql://localhost:5432/spare_parts_db`
- **Credentials:** postgres / postgres (change in production)
- **Updates schema automatically**

---

## API Endpoints

### Authentication
- `POST /api/auth/login` — Login with email & password
- `POST /api/auth/register` — Register new user
- `POST /api/auth/refresh` — Refresh JWT token

### Parts Management
- `GET /api/parts` — List all parts (paginated)
- `GET /api/parts/{id}` — Get part by ID
- `POST /api/parts` — Create new part
- `PUT /api/parts/{id}` — Update part
- `DELETE /api/parts/{id}` — Delete part
- `GET /api/parts/low-stock` — List low stock parts
- `GET /api/parts/search?keyword=...` — Search parts

### Orders Management
- `GET /api/orders` — List all orders
- `GET /api/orders/{id}` — Get order by ID
- `POST /api/orders` — Create new order
- `PUT /api/orders/{id}` — Update order
- `DELETE /api/orders/{id}` — Delete order
- `PATCH /api/orders/{id}/status` — Update order status

### Suppliers Management
- `GET /api/suppliers` — List all suppliers
- `GET /api/suppliers/{id}` — Get supplier by ID
- `POST /api/suppliers` — Create new supplier
- `PUT /api/suppliers/{id}` — Update supplier
- `DELETE /api/suppliers/{id}` — Delete supplier
- `GET /api/suppliers/search?keyword=...` — Search suppliers

### Reports
- `GET /api/reports/inventory` — Inventory report
- `GET /api/reports/sales` — Sales report
- `GET /api/reports/suppliers` — Supplier report
- `GET /api/reports/export?format=csv|excel|pdf` — Export reports

---

## JWT Authentication

### Getting Started
1. **Register:** POST `/api/auth/register` with email & password
2. **Login:** POST `/api/auth/login` to get JWT token
3. **Use Token:** Add header `Authorization: Bearer <token>` to all requests

### Token Details
- **Expiration:** 24 hours
- **Algorithm:** HS256
- **Secret:** Configured in `application.properties`

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role ENUM('ADMIN', 'MANAGER', 'USER'),
    enabled BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Parts Table
```sql
CREATE TABLE parts (
    id BIGINT PRIMARY KEY,
    sku VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    description TEXT,
    quantity INTEGER,
    min_stock_level INTEGER,
    unit_price DECIMAL(10,2),
    total_value DECIMAL(10,2),
    supplier_id BIGINT FOREIGN KEY,
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Orders Table
```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    order_number VARCHAR(255) UNIQUE,
    part_id BIGINT FOREIGN KEY,
    supplier_id BIGINT FOREIGN KEY,
    quantity INTEGER,
    unit_price DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    status ENUM('PENDING', 'APPROVED', 'COMPLETED', 'CANCELLED'),
    order_date TIMESTAMP,
    delivery_date TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Suppliers Table
```sql
CREATE TABLE suppliers (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    rating DOUBLE,
    active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

## Features Implemented

### ✅ Authentication & Security
- JWT-based authentication
- Password encryption (BCrypt)
- Role-based access control
- CORS configuration
- Protected endpoints

### ✅ Parts Management
- CRUD operations
- SKU uniqueness validation
- Low stock tracking
- Search & filter
- Pagination support
- Auto-calculate total value

### ✅ Orders Management
- Order creation & tracking
- Status workflow (PENDING → APPROVED → COMPLETED)
- Link parts & suppliers
- Date range filtering
- Auto-calculate totals

### ✅ Suppliers Management
- Supplier CRUD
- Rating system
- Location tracking
- Contact information
- Part linking

### ✅ Reports & Analytics
- Inventory reports
- Sales analytics
- Supplier performance
- Low stock alerts
- Export to CSV/Excel/PDF

### ✅ API Documentation
- Swagger/OpenAPI 3.0
- Interactive API exploration
- JWT authentication setup

---

## Configuration Files

### Main Application Properties
```properties
spring.application.name=spare-parts-inventory
server.port=8080
jwt.secret=<your-secret-key>
jwt.expiration=86400000
```

### Environment-Specific
- `application-h2.properties` — H2 (development)
- `application-postgresql.properties` — PostgreSQL (production)

---

## Development Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+ (for production)
- Git

### Installation
```bash
# Clone repo
git clone <repo-url>
cd spare-parts-backend

# Build
mvn clean install

# Run with H2 (default)
mvn spring-boot:run

# Run with PostgreSQL
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgresql"
```

### Access Points
- **API Base:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/api/swagger-ui.html
- **H2 Console:** http://localhost:8080/api/h2-console (H2 only)

---

## Testing

### Login Test
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password123"}'
```

### Get Parts (with token)
```bash
curl -X GET http://localhost:8080/api/parts \
  -H "Authorization: Bearer <your-jwt-token>"
```

---

## Deployment

### Docker
```dockerfile
FROM openjdk:17-slim
COPY target/spare-parts-inventory-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/spare_parts
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=<secure-password>
export JWT_SECRET=<long-random-secret>
```

---

## Project Structure

```
src/main/java/com/spareparts/
├── config/           # Spring configurations
├── controller/       # REST controllers
├── service/          # Business logic (interface)
├── service/impl/     # Business logic (implementation)
├── repository/       # JPA repositories
├── entity/          # JPA entities
├── dto/             # Data transfer objects
├── security/        # JWT & security utilities
├── exception/       # Custom exceptions
├── util/            # Utility classes
└── SparePartsApplication.java
```

---

## API Response Format

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully"
}
```

### Error Response
```json
{
  "timestamp": "2024-06-25T10:30:00",
  "message": "Resource not found",
  "details": "uri=/api/parts/999",
  "errorCode": "NOT_FOUND"
}
```

---

## Performance Optimization

- **Database Indexing:** Indexes on frequently queried columns
- **Pagination:** Large datasets paginated (default 10 items/page)
- **Connection Pooling:** HikariCP with optimized settings
- **Caching:** Ready for Redis integration
- **Query Optimization:** JPA batch processing enabled

---

## Security Best Practices

1. ✅ Passwords hashed with BCrypt
2. ✅ JWT tokens with 24-hour expiration
3. ✅ CORS configured for localhost
4. ✅ CSRF disabled for REST API
5. ✅ Input validation on all endpoints
6. ✅ Role-based access control

---

## Troubleshooting

### H2 Console Not Loading
- Check `spring.h2.console.enabled=true` in properties
- Access at http://localhost:8080/api/h2-console
- Default user: sa, password: (empty)

### PostgreSQL Connection Failed
- Verify PostgreSQL is running
- Check connection URL & credentials
- Ensure database `spare_parts_db` exists

### JWT Token Expired
- Get new token from `/api/auth/login`
- Token expires every 24 hours
- Add fresh token to Authorization header

---

## Next Steps

1. **Frontend Integration:** Connect with React frontend at http://localhost:5173
2. **Testing:** Add integration tests with JUnit & Mockito
3. **Monitoring:** Add Spring Boot Actuator for health checks
4. **Caching:** Integrate Redis for improved performance
5. **Deployment:** Deploy to AWS/Azure/GCP

---

**Status:** ✅ Production Ready  
**Last Updated:** June 25, 2026  
**Maintainer:** Development Team
