# WorkFlow SaaS

A multi-tenant SaaS platform for field service and maintenance management.

WorkFlow helps service companies manage customers, locations, equipment, service requests, technicians, work orders, scheduling, inventory, maintenance, invoicing, notifications, and company operations from a single platform.

The project is being built as a production-oriented portfolio application using Java, Spring Boot, Hibernate, PostgreSQL, Angular, Redis, Kafka, Docker, and AWS.

---

## Project Goals

The main goals of WorkFlow are:

- Build a realistic multi-tenant SaaS application
- Apply enterprise Java and Spring development practices
- Implement strict tenant data isolation
- Build role-specific interfaces and permissions
- Design real-world business workflows rather than simple CRUD operations
- Apply automated testing throughout the application
- Introduce asynchronous and event-driven processing where appropriate
- Containerize and deploy the application
- Implement observability and production-oriented infrastructure

---

## Core Users

WorkFlow supports multiple types of users, each with different responsibilities and interfaces.

### Platform Administrator

Manages the WorkFlow SaaS platform itself.

Responsibilities include:

- Tenant management
- Subscription management
- Platform configuration
- System health
- Security and audit information
- Platform-level monitoring

### Tenant Administrator

Manages a company's WorkFlow account.

Responsibilities include:

- Employees and users
- Roles and permissions
- Customers
- Locations
- Equipment
- Company settings
- Subscription and usage information

### Manager / Dispatcher

Manages daily service operations.

Responsibilities include:

- Reviewing service requests
- Prioritizing requests
- Assigning technicians
- Scheduling work
- Monitoring work orders
- Managing technician workload
- Handling escalations

### Technician

Performs field service work.

Responsibilities include:

- Viewing assigned jobs
- Starting and completing work
- Recording work performed
- Recording labor
- Recording parts used
- Uploading photos and documents
- Adding technical notes
- Reporting additional problems
- Updating equipment information

### Customer Administrator

Represents a customer organization using the platform.

Responsibilities include:

- Managing customer users
- Managing locations
- Viewing equipment
- Viewing service requests
- Viewing work orders
- Viewing invoices
- Reviewing maintenance history

### Requester

A person working for a customer organization who reports a problem.

Responsibilities include:

- Creating service requests
- Describing problems
- Uploading photos
- Adding comments
- Tracking request status
- Receiving notifications

A requester is intentionally modeled separately from the customer organization. The person who reports a problem is not necessarily the person who manages the customer's account.

---

## Core Business Workflow

A typical service workflow looks like this:

```text
Customer Employee
       |
       | Reports Problem
       v
Service Request
       |
       | Review / Prioritization
       v
Work Order
       |
       | Technician Assignment
       v
Scheduled
       |
       | Technician Starts Work
       v
In Progress
       |
       | Work Completed
       v
Completed
       |
       +--------------------+
       |                    |
       v                    v
Inventory Update       Customer Confirmation
       |                    |
       +----------+---------+
                  |
                  v
              Invoicing
                  |
                  v
             Notifications
                  |
                  v
              Audit Event
```

---

## Multi-Tenancy

WorkFlow is designed as a multi-tenant SaaS application.

Each company using WorkFlow represents a separate tenant.

Tenant data must never leak between tenants.

Tenant isolation applies to:

- Database queries
- REST APIs
- Authentication and authorization
- Cache entries
- Kafka events
- WebSocket messages
- File storage
- Reports
- Search
- Background jobs
- Audit information

Cross-tenant access will be explicitly tested.

---

## Main Domain Areas

The application will eventually contain modules such as:

- Authentication
- Tenants
- Users
- Roles and permissions
- Customers
- Customer locations
- Equipment
- Equipment history
- Service requests
- Work orders
- Scheduling
- Maintenance plans
- Inventory
- Invoicing
- Notifications
- Documents
- Auditing
- Reporting
- Subscriptions

---

## Technology Stack

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- Bean Validation
- Maven
- Flyway

### Database and Infrastructure

- PostgreSQL
- Redis
- Apache Kafka
- AWS S3
- Docker
- Docker Compose

### Frontend

- Angular
- TypeScript
- RxJS

### Testing

- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers

### Observability

- Spring Boot Actuator
- Prometheus
- Grafana

### CI/CD

- GitHub Actions
- Docker image builds
- Automated tests
- Deployment automation

---

## Architecture

The application will initially use a **modular monolith** architecture.

Microservices will not be introduced prematurely.

The backend will be organized around business modules such as:

```text
backend
└── src
    └── main
        └── java
            └── ...
                ├── auth
                ├── tenant
                ├── user
                ├── customer
                ├── equipment
                ├── service
                ├── scheduling
                ├── inventory
                ├── billing
                ├── notification
                ├── audit
                ├── reporting
                └── common
```

The architecture may evolve as the application grows.

---

## Development Roadmap

### Phase 1 — Foundation

- Project setup
- Git workflow
- Java/Spring Boot application
- PostgreSQL
- Flyway
- Initial domain model
- Basic architecture
- Docker development environment

### Phase 2 — Identity and Security

- User management
- Authentication
- JWT
- Password hashing
- Roles
- Permissions
- Tenant isolation

### Phase 3 — Core Operations

- Customers
- Locations
- Equipment
- Service requests
- Work orders
- Technicians
- Scheduling

### Phase 4 — Frontend

- Angular application
- Authentication UI
- Role-based navigation
- Dashboards
- Customer interface
- Dispatcher interface
- Technician interface
- Administration interface

### Phase 5 — Testing

- Unit tests
- Repository tests
- Controller tests
- Integration tests
- Security tests
- Tenant isolation tests
- Testcontainers

### Phase 6 — Advanced Features

- Inventory
- Maintenance plans
- Automated reminders
- Notifications
- Invoicing
- File uploads
- Audit logging
- Redis

### Phase 7 — Event-Driven Features

- Kafka
- Domain events
- Asynchronous processing
- WebSockets
- Real-time notifications

### Phase 8 — SaaS

- Subscription plans
- Subscription lifecycle
- Usage limits
- Billing integration
- Payment webhooks

### Phase 9 — Production

- Docker
- GitHub Actions
- AWS
- PostgreSQL deployment
- Redis deployment
- File storage
- Monitoring
- Prometheus
- Grafana
- Security hardening
- Backups

---

## Engineering Principles

The project will prioritize:

- Clean architecture
- Strong domain modeling
- Separation of concerns
- Secure tenant isolation
- Explicit authorization
- Automated testing
- Database integrity
- Meaningful error handling
- Observability
- Maintainability
- Production-oriented practices

The application should favor understandable, maintainable solutions over unnecessary complexity.

---

## Repository Structure

```text
workflow-saas/
│
├── backend/
│
├── frontend/
│
├── docs/
│   ├── product-specification.md
│   ├── architecture.md
│   ├── database-design.md
│   └── api-design.md
│
├── infrastructure/
│
├── .github/
│   └── workflows/
│
├── README.md
└── .gitignore
```

---

## Project Status

🚧 **In active development**

This repository tracks the complete development journey of WorkFlow, from initial domain modeling to a production-oriented SaaS platform.

---

## License

License to be determined.
