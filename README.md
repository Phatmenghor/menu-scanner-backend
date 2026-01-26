<!-- README.md -->
# E-Menu SaaS Platform - Complete User Management System

A comprehensive SaaS platform for restaurant management with complete user management, role-based access control, subscription management, and multi-tenant architecture.

## 🚀 Features

### Core User Management
- **Multi-Role System**: Platform admins, business owners, staff, and customers
- **Complete Authentication**: JWT-based with refresh tokens
- **Account Security**: Email/phone verification, 2FA support, account locking
- **Customer Loyalty**: Tier-based system (Bronze to VIP) with points and rewards

### Business & Subscription Management
- **Multi-Tenant**: Isolated business environments
- **Subscription Plans**: Free, Basic, Professional, Enterprise
- **Staff Management**: Role-based access within businesses
- **Usage Tracking**: Monitor subscription limits and usage

### Communication & Notifications
- **Email Templates**: Welcome, verification, password reset, tier upgrades
- **Telegram Integration**: Real-time notifications and updates
- **Multi-Channel**: Email and Telegram notifications with user preferences

### Security & Compliance
- **Comprehensive Audit**: All user actions logged with IP and timestamp
- **GDPR Ready**: Data processing consent, terms acceptance tracking
- **Security Events**: Failed login attempts, suspicious activity monitoring
- **Role-Based Permissions**: Fine-grained access control

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.2.5, Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security with JWT
- **Caching**: Caffeine/Redis
- **Documentation**: OpenAPI/Swagger
- **Monitoring**: Micrometer, Actuator
- **Containerization**: Docker & Docker Compose

### Package Structure
```
com.emenu/
├── config/                 # Configuration classes
├── enums/                  # All enum definitions
├── exception/              # Custom exceptions and global handler
├── features/
│   ├── usermanagement/     # User management feature
│   │   ├── controller/     # REST controllers
│   │   ├── domain/         # JPA entities
│   │   ├── dto/           # Data Transfer Objects
│   │   │   ├── request/   # Request DTOs
│   │   │   ├── response/  # Response DTOs
│   │   │   ├── filter/    # Filter DTOs
│   │   │   └── update/    # Update DTOs
│   │   ├── mapper/        # MapStruct mappers
│   │   ├── repository/    # Data repositories
│   │   ├── service/       # Business logic
│   │   ├── specification/ # JPA Specifications
│   │   └── tasks/         # Scheduled tasks
│   ├── notification/      # Notification system
│   └── audit/             # Audit logging
├── security/              # Security configuration
├── shared/                # Shared utilities
└── utils/                 # Utility classes
```

## 🚦 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 12+
- Docker (optional)

### Quick Start with Docker

1. **Clone the repository**
```bash
git clone <repository-url>
cd emenu-saas-platform
```

2. **Start with Docker Compose**
```bash
docker-compose.yml up -d
```

3. **Access the application**
- Application: http://localhost:9090
- Swagger UI: http://localhost:9090/swagger-ui.html
- API Docs: http://localhost:9090/v3/api-docs

### Manual Setup

1. **Database Setup**
```sql
CREATE DATABASE emenu_platform;
CREATE USER emenu_user WITH PASSWORD 'emenu_password';
GRANT ALL PRIVILEGES ON DATABASE emenu_platform TO emenu_user;
```

2. **Configure Application**
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/emenu_platform
    username: emenu_user
    password: emenu_password
```

3. **Run the Application**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Default Credentials (Development)
- **Platform Owner**: admin@emenu-platform.com / Admin123!@#
- **Business Owner**: demo-business@emenu-platform.com / Business123!
- **Customer**: demo-customer@emenu-platform.com / Customer123!

## 📚 API Documentation

### Authentication Endpoints
```http
POST /api/v1/auth/login          # User login
POST /api/v1/auth/register       # User registration
POST /api/v1/auth/refresh-token  # Refresh JWT token
POST /api/v1/auth/logout         # User logout
POST /api/v1/auth/verify-email   # Verify email address
POST /api/v1/auth/forgot-password # Request password reset
```

### User Management Endpoints
```http
GET    /api/v1/users           # List users (with filtering)
POST   /api/v1/users           # Create new user
GET    /api/v1/users/{id}      # Get user by ID
PUT    /api/v1/users/{id}      # Update user
DELETE /api/v1/users/{id}      # Soft delete user
GET    /api/v1/users/me        # Get current user profile
```

### Example API Calls

**Login**
```bash
curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@emenu-platform.com",
    "password": "Admin123!@#"
  }'
```

**Create User**
```bash
curl -X POST http://localhost:9090/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "email": "newuser@example.com",
    "password": "SecurePass123!",
    "firstName": "New",
    "lastName": "User",
    "userType": "CUSTOMER",
    "roles": ["CUSTOMER"]
  }'
```

## 🔧 Configuration

### Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/emenu_platform
SPRING_DATASOURCE_USERNAME=emenu_user
SPRING_DATASOURCE_PASSWORD=emenu_password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Email
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

# Telegram
APP_NOTIFICATIONS_TELEGRAM_BOT_TOKEN=your-bot-token
```

### Application Profiles
- **dev**: Development environment with debug logging
- **test**: Test environment with H2 database
- **prod**: Production environment with optimized settings

## 🔒 Security Features

### JWT Authentication
- Access tokens (24 hours)
- Refresh tokens (30 days)
- Token blacklisting on logout
- Role-based access control

### Account Security
- Email verification required
- Phone verification optional
- Account locking after failed attempts
- Password strength requirements
- Two-factor authentication support

### Audit Logging
- All user actions logged
- IP address and user agent tracking
- Security events monitoring
- Failed login attempt tracking

## 🎯 User Roles & Permissions

### Platform Roles
- **PLATFORM_OWNER**: Full system control
- **PLATFORM_MANAGER**: Platform operations
- **PLATFORM_STAFF**: Customer support
- **PLATFORM_DEVELOPER**: Technical access
- **PLATFORM_SUPPORT**: Support operations
- **PLATFORM_SALES**: Sales management

### Business Roles
- **BUSINESS_OWNER**: Business management
- **BUSINESS_MANAGER**: Operations management
- **BUSINESS_STAFF**: Limited access

### Customer Roles
- **CUSTOMER**: Regular customer
- **VIP_CUSTOMER**: Premium customer
- **GUEST_CUSTOMER**: Guest access

## 📊 Customer Loyalty System

### Tiers
- **Bronze**: 0-99 points (1.0x multiplier, 0% discount)
- **Silver**: 100-499 points (1.05x multiplier, 2% discount)
- **Gold**: 500-999 points (1.1x multiplier, 5% discount)
- **Platinum**: 1000-4999 points (1.15x multiplier, 8% discount)
- **VIP**: 5000+ points (1.2x multiplier, 10% discount)

### Point Earning
- 1 point per dollar spent (base rate)
- Tier multiplier applied
- Bonus points for special events
- Automatic tier upgrades

## 🔄 Subscription Management

### Plans
- **Free**: 1 staff, 10 menu items, 2 tables (30 days)
- **Basic**: 3 staff, 50 menu items, 10 tables ($29.99/month)
- **Professional**: 10 staff, 200 menu items, 25 tables ($79.99/month)
- **Enterprise**: Unlimited everything ($199.99/month)

### Features by Plan
- Analytics dashboard
- Custom branding
- Priority support
- Advanced reporting

## 📈 Monitoring & Health Checks

### Actuator Endpoints
- `/actuator/health` - Application health
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application info
- `/actuator/prometheus` - Prometheus metrics

### Performance Monitoring
- Request timing
- Database query monitoring
- Cache hit rates
- Memory usage

## 🧪 Testing

### Run Tests
```bash
mvn test                    # Unit tests
mvn verify                  # Integration tests
mvn test -Dtest=UserService # Specific test class
```

### Test Data Builders
```java
User testUser = UserTestDataBuilder.aUser()
    .withEmail("test@example.com")
    .asBusinessOwner()
    .withSubscription(SubscriptionPlan.PROFESSIONAL, LocalDateTime.now().plusDays(365))
    .build();
```

## 🚀 Deployment

### Docker Production
```bash
docker-compose.yml -f docker-compose.yml.prod.yml up -d
```

### Environment Setup
1. Configure production database
2. Set environment variables
3. Configure SSL certificates
4. Set up monitoring
5. Configure backup procedures

## 📝 API Response Format

### Success Response
```json
{
  "status": "success",
  "message": "Operation completed successfully",
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    ...
  }
}
```

### Error Response
```json
{
  "status": "error",
  "message": "Error description",
  "data": {
    "errorCode": "USER_NOT_FOUND",
    "timestamp": "2024-01-01T12:00:00",
    "path": "/api/v1/users/123",
    "traceId": "uuid"
  }
}
```

## 🔧 Maintenance

### Scheduled Tasks
- Account unlock after lock period
- Cleanup unverified users (7 days)
- Subscription expiry notifications
- Customer tier updates
- Token cleanup

### Database Maintenance
- Regular backups
- Index optimization
- Log rotation
- Performance monitoring

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Follow coding standards
4. Write tests
5. Submit pull request

## 📄 License

This project is proprietary software. All rights reserved.

## 🆘 Support

- Documentation: [Internal Wiki]
- Email: support@emenu-platform.com
- Issues: [Internal Issue Tracker]

---

**Happy Coding! 🎉**
