# Recipe Manager API

[![Java CI](https://github.com/toantran980/Recipe-Manager/actions/workflows/ci.yml/badge.svg)](https://github.com/toantran980/Recipe-Manager/actions/workflows/ci.yml)
[![Live Demo](https://img.shields.io/badge/Live%20Demo-online-brightgreen)](https://recipe-manager-jh73.onrender.com)
![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](<https://img.shields.io/badge/Spring%20Boot-4.0.7-brightgreen>)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![License](https://img.shields.io/badge/License-MIT-green)

A secure REST API for managing personal recipe collections with JWT authentication, built with Spring Boot and PostgreSQL.

## Features

- **Authentication** — Register, login, logout with JWT tokens
- **Recipe CRUD** — Create, read, update, delete recipes with ownership enforcement
- **Search & Filter** — By title, description, category, prep time, ingredients
- **Pagination** — Configurable page/size for listing endpoints
- **Image Upload** — Base64-encoded image storage per recipe
- **API Docs** — Swagger/OpenAPI at `/swagger-ui/index.html`
- **Health Checks** — Actuator endpoint at `/api/health`
- **Rate Limiting** — IP-based login attempt throttling
- **Token Blacklisting** — Secure logout with Redis-backed invalidation

## Tech Stack

| Layer            | Technology                       |
| ---------------- | -------------------------------- |
| Language         | Java 25                          |
| Framework        | Spring Boot 4.0.7                |
| Build            | Maven                            |
| Database         | PostgreSQL 16 (Spring Data JPA) — **Use Neon/Supabase for production** (Render free tier expires in 30 days)  |
| Cache/Session    | Redis 7                          |
| Security         | Spring Security + JWT (jjwt)     |
| Containerization | Docker / Docker Compose          |
| Testing          | JUnit 5, Mockito, H2 (in-memory) |
| CI/CD            | GitHub Actions                   |

## Quick Start

### Prerequisites

- Docker Desktop (recommended) **or** Java 25 + PostgreSQL 16 + Redis 7

### Option 1: Docker Compose (Recommended)

```bash
docker compose up -d
```

| Service    | URL                                         |
| ---------- | ------------------------------------------- |
| API        | http://localhost:8080                       |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Health     | http://localhost:8080/api/health            |

| Service    | URL                                                  |
| ---------- | ---------------------------------------------------- |
| **Production API** | https://recipe-manager-jh73.onrender.com |
| **Swagger UI** | https://recipe-manager-jh73.onrender.com/swagger-ui/index.html |
| **Health** | https://recipe-manager-jh73.onrender.com/api/health |

### Option 2: Local Development

```bash
# 1. Start PostgreSQL & Redis (adjust as needed)
docker run -d --name postgres -e POSTGRES_DB=recipe -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 2. Configure environment
cp .env.example .env
# Edit .env with your credentials

# 3. Run
./mvnw spring-boot:run
```

## Configuration

| Variable                       | Description                | Default                                     |
| ------------------------------ | -------------------------- | ------------------------------------------- |
| `SPRING_PROFILES_ACTIVE`     | `dev` or `prod`        | `dev`                                     |
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC URL        | `jdbc:postgresql://localhost:5432/recipe` |
| `SPRING_DATASOURCE_USERNAME` | DB username                | `postgres`                                |
| `SPRING_DATASOURCE_PASSWORD` | DB password                | `postgres`                                |
| `SPRING_DATA_REDIS_HOST`     | Redis host                 | `localhost`                               |
| `SPRING_DATA_REDIS_PORT`     | Redis port                 | `6379`                                    |
| `JWT_SECRET`                 | Base64-encoded 256-bit key | **Required**                          |
| `JWT_EXPIRATION_MS`          | Token TTL (ms)             | `86400000` (24h)                          |

Generate a secure secret:

```bash
openssl rand -base64 32
```

## API Endpoints

### Authentication

| Method   | Path                   | Description        |
| -------- | ---------------------- | ------------------ |
| `POST` | `/api/auth/register` | Register new user  |
| `POST` | `/api/auth/login`    | Login, returns JWT |
| `POST` | `/api/auth/logout`   | Invalidate token   |

### Recipes (require `Authorization: Bearer <token>`)

| Method     | Path                        | Description                 |
| ---------- | --------------------------- | --------------------------- |
| `POST`   | `/api/recipes`            | Create recipe               |
| `GET`    | `/api/recipes`            | List recipes (with filters) |
| `GET`    | `/api/recipes/{id}`       | Get single recipe           |
| `PUT`    | `/api/recipes/{id}`       | Update recipe               |
| `DELETE` | `/api/recipes/{id}`       | Delete recipe               |
| `POST`   | `/api/recipes/{id}/image` | Upload image                |

### Health & Documentation

| Method | Path                          | Description              |
| ------ | ----------------------------- | ------------------------ |
| `GET`  | `/api/health`                 | Health check             |
| `GET`  | `/api-docs`                   | OpenAPI JSON spec        |
| `GET`  | `/swagger-ui/index.html`      | Swagger UI (interactive) |

### Recipe Query Parameters

| Param           | Type   | Description              |
| --------------- | ------ | ------------------------ |
| `search`      | string | Search title/description |
| `category`    | string | Filter by category       |
| `maxPrepTime` | int    | Max prep time (minutes)  |
| `ingredient`  | string | Filter by ingredient     |
| `page`        | int    | Page number (0-based)    |
| `size`        | int    | Page size (default 20)   |

## Testing

```bash
# Unit + integration tests (uses H2 in-memory)
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

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

Health check: `GET https://recipe-manager-jh73.onrender.com/api/health`  
Swagger: `https://recipe-manager-jh73.onrender.com/swagger-ui/index.html`

### Local Development (unchanged)
Use Docker Compose with local PostgreSQL/Redis — see [Quick Start](#quick-start).

## Project Structure

```
src/main/java/com/example/recipemanager/
├── config/           # Security, Redis configuration
├── controller/       # REST endpoints (Auth, Recipe, Health)
├── dto/              # Request/response records
├── entity/           # JPA entities (User, Recipe)
├── exception/        # Custom exceptions & global handler
├── repository/       # Spring Data JPA repositories
├── security/         # JWT service, filter, auth principal
├── service/          # Business logic (Auth, Recipe)
└── RecipemanagerApplication.java
```

## CI/CD Pipeline

GitHub Actions (`.github/workflows/ci.yml`):

- Build & test on every push/PR
- Maven dependency caching
- Test reports uploaded as artifacts

## License

MIT — see [LICENSE](LICENSE) for details.

---

**Original College Project** 
James Nguyen  • Toan Tran

**Post-Graduation Maintenance** — All updates after May 15, 2026 by Toan Tran for skill development.
