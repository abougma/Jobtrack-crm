# Stack technique — JobTrack

## Backend
- Java 21, Spring Boot 3.4.x
- Spring Web, Spring Data JPA, Spring Security
- JWT access token (15 min) + refresh token rotatif (30 jours)
- Bean Validation, Flyway, PostgreSQL 17
- MapStruct (optionnel, mapping manuel accepté), springdoc-openapi
- Tests : JUnit 5, Mockito, Testcontainers PostgreSQL, MockMvc

## Frontend
- React 18 + TypeScript + Vite
- React Router 7
- TanStack Query 5
- React Hook Form + Zod
- Tailwind CSS 3
- Recharts
- dnd-kit (Kanban)
- Tests : Vitest + React Testing Library
- ESLint + Prettier

## DevOps
- Docker Compose : `api`, `web`, `db`
- GitHub Actions : build, tests, lint
- Variables d'environnement, aucun secret dans le code

## Commandes
```bash
# backend
cd backend && mvn verify

# frontend
cd frontend && npm ci && npm run lint && npm run test -- --run && npm run build

# tout l'environnement
cp .env.example .env && docker compose up --build
```

## Variables d'environnement
| Variable | Rôle | Défaut dev |
|---|---|---|
| `DATABASE_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/jobtrack` |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Identifiants DB | `jobtrack` / `jobtrack-dev-only` |
| `JWT_SECRET` | Secret HMAC (>= 32 octets) | valeur de dev uniquement |
| `JWT_ACCESS_TOKEN_MINUTES` | Durée access token | `15` |
| `JWT_REFRESH_TOKEN_DAYS` | Durée refresh token | `30` |
| `CORS_ORIGIN` | Origine frontend autorisée | `http://localhost:5173` |
| `SERVER_PORT` | Port API | `8080` |
| `VITE_API_URL` | URL API côté frontend | `http://localhost:18080/api/v1` |
