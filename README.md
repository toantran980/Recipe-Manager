# Recipe Manager API

[![Java CI](https://github.com/toantran980/Recipe-Manager/actions/workflows/ci.yml/badge.svg)](https://github.com/toantran980/Recipe-Manager/actions/workflows/ci.yml)
[![Live Demo](https://img.shields.io/badge/Live%20Demo-online-brightgreen)](https://recipe-manager-jh73.onrender.com)
![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](<https://img.shields.io/badge/Spring%20Boot-4.0.7-brightgreen>)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue)
![License](https://img.shields.io/badge/License-MIT-green)

A secure REST API for managing personal recipe collections with JWT authentication, built with Spring Boot and PostgreSQL.

## Features

- **Authentication** — Register, login, logout with JWT tokens
- **Email Verification** — Secure email verification system for new users
- **Password Reset** — Forgot password functionality with secure token-based reset
- **Recipe CRUD** — Create, read, update, delete recipes with ownership enforcement
- **Enhanced Recipe Fields** — Cooking time, servings, difficulty, cuisine, instructions, nutrition info
- **Recipe Tags** — Flexible tagging system for better organization
- **Search & Filter** — By title, description, category, prep time, ingredients, cuisine, difficulty
- **Sorting** — Sort recipes by title, prep time, created date, category (ascending/descending)
- **Pagination** — Configurable page/size for listing endpoints
- **Recipe Duplication** — Clone existing recipes with one click
- **Bulk Operations** — Bulk delete multiple recipes at once
- **Recipe Export/Import** — Export recipes as JSON and import recipes from JSON
- **Image Upload** — Base64-encoded image storage per recipe
- **User Profile Management** — Update user profile and change password
- **Recipe Ratings** — Rate and review recipes (infrastructure ready)
- **Recipe Favorites** — Save favorite recipes (infrastructure ready)
- **API Docs** — Swagger/OpenAPI at `/swagger-ui/index.html`
- **Health Monitoring** — Spring Boot Actuator endpoints at `/api/v1/health`
- **Rate Limiting** — IP-based login attempt throttling
- **Token Blacklisting** — Secure logout with Redis-backed invalidation
- **Security Hardening** — Cache key isolation to prevent cross-user data leakage
- **API Versioning** — Versioned API endpoints (v1) for future compatibility
- **CORS Support** — Configured for frontend integration
- **Structured Logging** — Comprehensive logging with SLF4J

## Tech Stack

| Layer            | Technology                       |
| ---------------- | -------------------------------- |
| Language         | Java 25                          |
| Framework        | Spring Boot 4.0.7                |
| Build            | Maven                            |
| Database         | PostgreSQL 18 (Spring Data JPA) — **Use Neon/Supabase for production** (Render free tier expires in 30 days)  |
| Cache/Session    | Redis 7                          |
| Security         | Spring Security + JWT (jjwt)     |
| Monitoring       | Spring Boot Actuator             |
| Containerization | Docker / Docker Compose          |
| Testing          | JUnit 5, Mockito, H2 (in-memory) |
| CI/CD            | GitHub Actions                   |

## Quick Start

### Prerequisites

- Docker Desktop (recommended) **or** Java 25 + PostgreSQL 18 + Redis 7

### Option 1: Docker Compose (Recommended)

```bash
docker compose up -d
```

| Service    | URL                                         |
| ---------- | ------------------------------------------- |
| API        | http://localhost:8080                       |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Health     | http://localhost:8080/api/v1/health              |

| Service    | URL                                                       |
| ---------- | --------------------------------------------------------- |
| **Production API** | https://recipe-manager-jh73.onrender.com              |
| **Swagger UI** | https://recipe-manager-jh73.onrender.com/swagger-ui/index.html |
| **Health** | https://recipe-manager-jh73.onrender.com/api/v1/health |

### Option 2: Local Development

```bash
# 1. Start PostgreSQL & Redis (adjust as needed)
docker run -d --name postgres -e POSTGRES_DB=recipe -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:18-alpine
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 2. Configure environment
cp .env.example .env
# Edit .env with your credentials

# 3. Run
./mvnw spring-boot:run
```

## Configuration

### Environment Variables

| Variable                       | Description                | Default                                     |
| ------------------------------ | -------------------------- | ------------------------------------------- |
| `SPRING_PROFILES_ACTIVE`      | `dev` or `prod`            | `dev`                                     |
| `SPRING_DATASOURCE_URL`       | PostgreSQL JDBC URL        | `jdbc:postgresql://localhost:5432/recipe` |
| `SPRING_DATASOURCE_USERNAME`  | DB username                | `postgres`                                |
| `SPRING_DATASOURCE_PASSWORD`  | DB password                | `postgres`                                |
| `SPRING_DATA_REDIS_HOST`      | Redis host                 | `localhost`                               |
| `SPRING_DATA_REDIS_PORT`      | Redis port                 | `6379`                                    |
| `JWT_SECRET`                  | Base64-encoded 256-bit key | **Required in production**                |
| `JWT_EXPIRATION_MS`           | Token TTL (ms)             | `86400000` (24h)                          |
| `PORT`                        | Server port                | `8080`                                    |
| `CORS_ALLOWED_ORIGINS`        | CORS allowed origins       | `http://localhost:3000,http://localhost:8080` |

### Configuration Files

- `application.properties` - Common settings shared across all environments
- `application-dev.properties` - Development-specific configuration
- `application-prod.properties` - Production-specific configuration (values from environment variables)

### Secret Generation

Generate a secure JWT secret for production:

```bash
openssl rand -base64 32
```

## API Endpoints

### Authentication

| Method   | Path                        | Description        |
| -------- | --------------------------- | ------------------ |
| `POST` | `/api/v1/auth/register`   | Register new user  |
| `POST` | `/api/v1/auth/login`      | Login, returns JWT |
| `POST` | `/api/v1/auth/logout`     | Invalidate token   |
| `POST` | `/api/v1/auth/forgot-password` | Request password reset |
| `POST` | `/api/v1/auth/reset-password`  | Reset password with token |
| `POST` | `/api/v1/auth/verify-email`    | Verify email with token |

### Recipes (require `Authorization: Bearer <token>`)

| Method     | Path                              | Description                 |
| ---------- | --------------------------------- | --------------------------- |
| `POST`   | `/api/v1/recipes`                | Create recipe               |
| `GET`    | `/api/v1/recipes`                | List recipes (with filters) |
| `GET`    | `/api/v1/recipes/{id}`           | Get single recipe           |
| `PUT`    | `/api/v1/recipes/{id}`           | Update recipe               |
| `DELETE` | `/api/v1/recipes/{id}`           | Delete recipe               |
| `POST`   | `/api/v1/recipes/{id}/image`     | Upload image                |
| `POST`   | `/api/v1/recipes/{id}/duplicate` | Duplicate recipe            |
| `DELETE` | `/api/v1/recipes/bulk`           | Bulk delete recipes         |
| `GET`    | `/api/v1/recipes/{id}/export`   | Export recipe as JSON       |
| `POST`   | `/api/v1/recipes/import`         | Import recipe from JSON     |

### User Management (require `Authorization: Bearer <token>`)

| Method   | Path                        | Description              |
| -------- | --------------------------- | ------------------------ |
| `GET`  | `/api/v1/users/profile`    | Get user profile         |
| `PUT`  | `/api/v1/users/profile`    | Update user profile      |
| `POST` | `/api/v1/users/change-password` | Change password      |

### Health & Documentation

| Method | Path                          | Description              |
| ------ | ----------------------------- | ------------------------ |
| `GET`  | `/api/v1/health`              | Health check             |
| `GET`  | `/api-docs`                   | OpenAPI JSON spec        |
| `GET`  | `/swagger-ui/index.html`      | Swagger UI (interactive) |

### Recipe Query Parameters

| Param           | Type   | Description              |
| --------------- | ------ | ------------------------ |
| `search`      | string | Search title/description |
| `category`    | string | Filter by category       |
| `maxPrepTime` | int    | Max prep time (minutes)  |
| `ingredient`  | string | Filter by ingredient     |
| `sortBy`      | string | Sort by field (title, prepTime, createdAt, category) |
| `sortDirection` | string | Sort direction (asc, desc) |
| `page`        | int    | Page number (0-based)    |
| `size`        | int    | Page size (default 20)   |

## Testing

```bash
# Unit + integration tests (uses H2 in-memory)
./mvnw test

# Run specific integration test
./mvnw test -Dtest=RecipeIntegrationTest

# Run with coverage
./mvnw test jacoco:report
```

### Test Coverage
- Unit tests for service layer with mocked dependencies
- Integration tests with real database (H2 in-memory)
- API endpoint testing with Spring Security context
- Custom validator testing

## Deployment (Render + Neon)

**⚠️ Render's free PostgreSQL expires in 30 days.** Use **Neon** (permanent free tier) for the database.

### Prerequisites
- GitHub repo connected to Render
- [Neon account](https://neon.tech) (free, serverless PostgreSQL)
- [Render account](https://render.com)

### Step-by-Step

1. **Create Neon PostgreSQL**
   - New Project → `recipe-manager` → Free tier
   - Copy **Pooled Connection String** (starts with `postgresql://`)

2. **Create Render Redis**
   - Dashboard → New → Redis → `recipe-manager-redis` → Free
   - Copy **Internal Connection String** (extract host, port 6379)

3. **Deploy Web Service via Blueprint**
   - Render Dashboard → New → Blueprint → Connect repo
   - Blueprint name: `recipe-manager-prod`
   - Applies `render.yaml` (web service only)

4. **Set Environment Variables** (Web Service → Environment)

| Variable | Value |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | *Neon pooled connection string* |
| `SPRING_DATASOURCE_USERNAME` | *From Neon connection string* |
| `SPRING_DATASOURCE_PASSWORD` | *From Neon connection string* |
| `JWT_SECRET` | `openssl rand -base64 32` |
| `REDIS_HOST` | *Render Redis host* |
| `REDIS_PORT` | `6379` |

5. **Save → Auto-deploys**

Health check: `GET https://recipe-manager-jh73.onrender.com/api/v1/health`  
Swagger: `https://recipe-manager-jh73.onrender.com/swagger-ui/index.html`

### Local Development (unchanged)
Use Docker Compose with local PostgreSQL/Redis — see [Quick Start](#quick-start).

## Recent Updates

### Security & Configuration Improvements (2024)
- **PostgreSQL Migration**: Migrated from MongoDB to PostgreSQL 18 for better ACID compliance and production compatibility
- **Profile-based Configuration**: Implemented environment-specific configurations (`dev`, `prod`) with secure secret management
- **Spring Boot Actuator**: Added health monitoring endpoints at `/api/v1/health` for production observability
- **Security Hardening**: Fixed cache key isolation in RecipeService to prevent cross-user data leakage via Redis caching
- **Redis Configuration**: Updated RedisConfig to use modern Jackson2JsonRedisSerializer instead of deprecated GenericJackson2JsonRedisSerializer
- **JWT Security**: Enhanced JWT secret management with proper Base64 encoding and environment variable support
- **Docker Compose**: Updated to use PostgreSQL 18-alpine with health checks and proper dependency management
- **Swagger/OpenAPI**: Enabled API documentation in both development and production environments with proper security configuration
- **Email Verification**: Added email verification system for new user registrations
- **Password Reset**: Implemented secure password reset functionality with token-based reset
- **User Profile Management**: Added endpoints for profile updates and password changes

### Feature Enhancements
- **Enhanced Recipe Fields**: Added cookingTime, servings, difficulty, cuisine, instructions, nutritionInfo fields
- **Recipe Tags System**: Implemented flexible tagging system for better recipe organization
- **Recipe Ratings Infrastructure**: Created RecipeRating entity and repository for future rating system
- **Recipe Favorites Infrastructure**: Created RecipeFavorite entity and repository for future favorites system
- **Sorting Options**: Added sorting by title, prepTime, createdAt, category with ascending/descending options
- **Recipe Duplication**: Added endpoint to duplicate existing recipes
- **Bulk Operations**: Implemented bulk delete functionality for multiple recipes
- **Recipe Export/Import**: Added JSON-based export and import functionality for recipes
- **Database Indexes**: Added performance indexes on user_id, category, title, createdAt, cuisine, difficulty
- **Audit Timestamps**: Added createdAt and updatedAt timestamps to recipes and users
- **API Versioning**: Implemented v1 API versioning for future compatibility
- **CORS Configuration**: Added configurable CORS support for frontend integration
- **File Upload Limits**: Configured 5MB file upload limits for recipe images
- **Structured Logging**: Added comprehensive logging with SLF4J across service layer
- **Connection Pooling**: Configured HikariCP with optimized connection pool settings
- **Custom Validators**: Created custom validation infrastructure for business logic validation
- **Integration Tests**: Added comprehensive integration tests with H2 in-memory database

### Infrastructure Updates
- **Render Deployment**: Configured for Render cloud deployment with Neon PostgreSQL and Render Redis
- **CI/CD Pipeline**: Enhanced GitHub Actions workflow with Maven dependency caching and test artifact uploads
- **Docker Optimization**: Improved Dockerfile for production builds and proper port configuration

## Project Structure

```
src/main/java/com/example/recipemanager/
├── benchmark/        # Performance benchmarking utilities
├── config/           # Security, Redis, CORS configuration
├── controller/       # REST endpoints (Auth, Recipe, User, Health)
├── dto/              # Request/response records
├── entity/           # JPA entities (User, Recipe, RecipeRating, RecipeFavorite, Role)
├── exception/        # Custom exceptions & global handler
├── repository/       # Spring Data JPA repositories
├── security/         # JWT service, filter, auth principal
├── service/          # Business logic (Auth, Recipe, User)
├── validation/       # Custom validators
└── RecipemanagerApplication.java
```

## CI/CD Pipeline

GitHub Actions (`.github/workflows/ci.yml`):

- Build & test on every push/PR
- Maven dependency caching
- Test reports uploaded as artifacts
- Multi-version Java compatibility testing

## Performance

The project includes benchmarking utilities in the `benchmark` package for monitoring API latency and performance metrics. Run benchmarks using the LatencyBenchmark utility to measure endpoint response times under various load conditions.

## License

MIT — see [LICENSE](LICENSE) for details.

---

**Original College Project** 
James Nguyen  • Toan Tran

**Post-Graduation Maintenance** — All updates after May 15, 2024 by Toan Tran for skill development.
