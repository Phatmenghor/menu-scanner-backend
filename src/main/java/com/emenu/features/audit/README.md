# Audit Logging Feature

## Overview
Comprehensive audit logging system that tracks **all** API requests and responses, including both authenticated and anonymous (no token) requests.

## Features

### 1. **Automatic Logging**
- ✅ Logs **all HTTP requests** (GET, POST, PUT, PATCH, DELETE)
- ✅ Logs **authenticated** requests (with JWT token)
- ✅ Logs **anonymous** requests (without token)
- ✅ Logs request/response bodies for data modification methods
- ✅ Tracks response times and error messages
- ✅ Asynchronous processing (non-blocking)

### 2. **Captured Information**
Each audit log entry includes:
- **User Information**: User ID, username, user type (or "anonymous")
- **Request Details**: HTTP method, endpoint, IP address, user agent, query parameters
- **Response Details**: Status code, response time (ms), error message
- **Request/Response Bodies**: For POST/PUT/PATCH/DELETE (configurable)
- **Session Information**: Session ID if available
- **Timestamp**: When the request occurred

### 3. **Query Capabilities**
The audit system provides powerful querying:
- Filter by user, IP address, endpoint, HTTP method
- Filter by status code, date range, response time
- Get error logs (status >= 400)
- Get anonymous access logs
- Get audit statistics

## Package Structure

```
com.emenu.features.audit/
├── controller/
│   └── AuditLogController.java          # REST endpoints for querying audit logs
├── dto/
│   ├── filter/
│   │   └── AuditLogFilterDTO.java       # Filter criteria for queries
│   └── response/
│       ├── AuditLogResponseDTO.java     # Audit log response DTO
│       └── AuditStatsResponseDTO.java   # Statistics response DTO
├── filter/
│   └── AuditLogFilter.java              # HTTP filter that intercepts ALL requests
├── models/
│   └── AuditLog.java                    # JPA entity
├── repository/
│   └── AuditLogRepository.java          # Data access layer
└── service/
    ├── AuditLogService.java             # Service interface
    └── AuditLogServiceImpl.java         # Service implementation
```

## API Endpoints

All audit endpoints require `ADMIN` or `SUPER_ADMIN` role.

### Get All Audit Logs (with filters)
```http
GET /api/v1/audit?page=0&size=20
```

**Query Parameters:**
- `userId` - Filter by user ID
- `userIdentifier` - Filter by username (partial match)
- `userType` - Filter by user type (CUSTOMER, BUSINESS_USER, PLATFORM_USER, ANONYMOUS)
- `httpMethod` - Filter by HTTP method (GET, POST, etc.)
- `endpoint` - Filter by endpoint (partial match)
- `ipAddress` - Filter by IP address
- `statusCode` - Filter by specific status code
- `minStatusCode` - Filter by minimum status code
- `maxStatusCode` - Filter by maximum status code
- `startDate` - Filter by start date (ISO format: 2026-01-24T00:00:00)
- `endDate` - Filter by end date
- `minResponseTime` - Filter by minimum response time (ms)
- `maxResponseTime` - Filter by maximum response time (ms)
- `hasError` - Filter logs with error messages (true/false)
- `isAnonymous` - Filter anonymous requests (true/false)

### Get Audit Log by ID
```http
GET /api/v1/audit/{id}
```

### Get User Audit Logs
```http
GET /api/v1/audit/user/{userId}?page=0&size=20
```

### Get Audit Logs by IP Address
```http
GET /api/v1/audit/ip/{ipAddress}?page=0&size=20
```

### Get Error Logs
```http
GET /api/v1/audit/errors?page=0&size=20
```

### Get Anonymous Access Logs
```http
GET /api/v1/audit/anonymous?page=0&size=20
```

### Get Audit Statistics
```http
GET /api/v1/audit/stats
```

**Response:**
```json
{
  "totalLogs": 12345,
  "last24Hours": 456,
  "last7Days": 3210
}
```

## Example Queries

### 1. Find all failed requests (4xx and 5xx errors)
```http
GET /api/v1/audit?minStatusCode=400
```

### 2. Find all requests from a specific IP
```http
GET /api/v1/audit?ipAddress=192.168.1.100
```

### 3. Find all anonymous requests (no token)
```http
GET /api/v1/audit?isAnonymous=true
```

### 4. Find slow requests (>1000ms)
```http
GET /api/v1/audit?minResponseTime=1000
```

### 5. Find all POST requests to a specific endpoint
```http
GET /api/v1/audit?httpMethod=POST&endpoint=/api/v1/products
```

### 6. Find all requests from a specific user type
```http
GET /api/v1/audit?userType=CUSTOMER
```

## How It Works

### 1. **AuditLogFilter**
- Registered automatically as a Spring `@Component`
- Runs with high priority (`HIGHEST_PRECEDENCE + 10`)
- Intercepts **every** HTTP request before authentication
- Wraps request/response to capture bodies
- Calls `AuditLogService.logAccessWithBodies()` asynchronously

### 2. **Async Processing**
- Uses Spring's `@Async` annotation
- Logging happens in a separate thread pool
- Won't block the main request/response cycle
- Failures in logging won't affect the API response

### 3. **Excluded Paths**
To reduce noise, these paths are **not** logged:
- `/api/images/**` - Image resources
- `/swagger-ui/**`, `/v3/api-docs/**` - API documentation
- `/actuator/**` - Health checks
- Static resources (`.css`, `.js`, `.png`, `.jpg`, `.ico`)

## Database Schema

### Table: `audit_logs`

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Primary key |
| `user_id` | UUID | User ID (NULL for anonymous) |
| `user_identifier` | VARCHAR(255) | Username or "anonymous" |
| `user_type` | VARCHAR(50) | User type or "ANONYMOUS" |
| `http_method` | VARCHAR(10) | GET, POST, PUT, DELETE, etc. |
| `endpoint` | VARCHAR(500) | API endpoint path |
| `ip_address` | VARCHAR(45) | Client IP address |
| `user_agent` | VARCHAR(500) | Browser/client user agent |
| `status_code` | INTEGER | HTTP response status |
| `response_time_ms` | BIGINT | Response time in milliseconds |
| `error_message` | VARCHAR(1000) | Error message if failed |
| `session_id` | VARCHAR(100) | HTTP session ID |
| `request_params` | VARCHAR(2000) | Query/form parameters |
| `request_body` | TEXT | Request body (POST/PUT/PATCH/DELETE) |
| `response_body` | TEXT | Response body (POST/PUT/PATCH/DELETE) |
| `created_at` | TIMESTAMP | When request occurred |
| `updated_at` | TIMESTAMP | Last update time |
| `is_deleted` | BOOLEAN | Soft delete flag |

### Indexes
Optimized indexes for common queries:
- `idx_audit_user` - User ID lookups
- `idx_audit_endpoint` - Endpoint searches
- `idx_audit_ip` - IP address searches
- `idx_audit_created` - Time-based queries
- `idx_audit_status` - Status code filters
- `idx_audit_errors` - Error log queries
- `idx_audit_anonymous` - Anonymous request queries

## Migration

Migration file: `database/migrations/V3__create_audit_logs_table.sql`

The migration:
1. Drops existing `audit_logs` table (if exists)
2. Creates new table with all fields
3. Creates all necessary indexes
4. Adds table/column comments
5. Creates auto-update trigger for `updated_at`
6. Inserts a migration audit entry

Run migrations automatically on application startup (Flyway).

## Configuration

### Enable Async Processing
Async configuration should already be enabled in `AsyncConfig.java`:
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    // Configuration here
}
```

### Customize Body Logging
Edit `AuditLogFilter.java` to change which methods log bodies:
```java
private boolean shouldLogBodies(String httpMethod) {
    return "POST".equalsIgnoreCase(httpMethod) ||
           "PUT".equalsIgnoreCase(httpMethod) ||
           "PATCH".equalsIgnoreCase(httpMethod) ||
           "DELETE".equalsIgnoreCase(httpMethod);
}
```

### Customize Excluded Paths
Edit `AuditLogFilter.java` to change which paths are skipped:
```java
private boolean shouldSkipLogging(String uri) {
    return uri.startsWith("/api/images/") ||
           // Add more exclusions here
}
```

## Security Considerations

1. **Sensitive Data**: Request/response bodies are logged. Be careful with:
   - Passwords (should be hashed before logging)
   - API keys
   - Personal information
   - Payment details

2. **Storage**: Audit logs can grow large. Consider:
   - Regular cleanup of old logs
   - Archiving to external storage
   - Database partitioning

3. **Access Control**: All audit endpoints require `ADMIN` or `SUPER_ADMIN` role

## Performance

- **Async Processing**: Logging doesn't block requests
- **Indexed Queries**: Common queries are optimized
- **Body Truncation**: Bodies limited to 10,000 chars
- **Selective Logging**: Static resources excluded

## Monitoring Use Cases

1. **Security Monitoring**: Track anonymous access, failed auth attempts
2. **Error Tracking**: Find all 4xx/5xx errors
3. **Performance Analysis**: Find slow endpoints
4. **User Activity**: Track user actions
5. **Compliance**: Maintain audit trail for regulations
6. **Debugging**: Reproduce issues by examining request/response

## Example: Finding Suspicious Activity

```http
# Find multiple failed login attempts from same IP
GET /api/v1/audit?endpoint=/api/v1/auth/login&minStatusCode=400&ipAddress=192.168.1.100

# Find all anonymous POST requests (potential attacks)
GET /api/v1/audit?isAnonymous=true&httpMethod=POST

# Find slow requests that might indicate issues
GET /api/v1/audit?minResponseTime=5000
```

## Future Enhancements

Potential additions:
- Retention policies (auto-delete old logs)
- Export to CSV/JSON
- Real-time alerting for suspicious patterns
- Dashboard with charts/graphs
- Log aggregation with external tools (ELK, Splunk)
