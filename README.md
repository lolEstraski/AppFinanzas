# AppFinanzas

App personal de finanzas para correr en local, sin desplegar.

## Estructura

- `backend/` — API Spring Boot (Java 17), arquitectura hexagonal por módulo de negocio (`auth`, `categories`, `expenses`), PostgreSQL + Flyway.
- `frontend/` — App móvil React Native (Expo).

## Correr en local

1. Levantar PostgreSQL: `docker compose up -d` (desde la raíz, requiere Docker Desktop).
2. Backend: `cd backend && ./mvnw spring-boot:run` (perfil `dev` por defecto, puerto 8080).
3. Frontend: `cd frontend && npx expo start` (pendiente de crear).
