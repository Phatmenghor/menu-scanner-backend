# KS-IT School Management System

A Spring Boot application for managing school operations, users, and permissions.

## Features

- User management with role-based access control (RBAC)
- Support for multiple roles per user
- Secure JWT authentication
- RESTful API with Swagger/OpenAPI documentation
- Proper error handling and validation
- Role-specific user information for staff, students, and administrators

## System Roles

- **DEVELOPER**: System developers with full access to all features
- **ADMIN**: School administrators with access to manage users and school resources
- **STAFF**: Teachers and school staff with limited administrative capabilities
- **STUDENT**: Student accounts with access to relevant information

## Technical Stack

- Java 17
- Spring Boot 3.2.5
- Spring Security (JWT-based authentication)
- Spring Data JPA
- PostgreSQL database
- Maven for dependency management and build
- OpenAPI/Swagger for API documentation

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL 12+
- Maven 3.8+

### Setup

1. Clone the repository
2. Configure the database connection in `application.yaml`
3. Run the application using Maven:

```bash
mvn spring:run
```

Alternatively, you can build and run the JAR file:

```bash
mvn clean package
java -jar target/smart-shop-0.0.1-SNAPSHOT.jar
```

### Default Users

The system initializes with the following default users on first startup:

- Developer: developer@ksit.com / developer123
- Administrator: admin@ksit.com / admin123
- Staff: staff@ksit.com / staff123
- Student: student@ksit.com / student123
- Multi-role (Staff+Admin): headteacher@ksit.com / headteacher123

## API Documentation

API documentation is available through Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.