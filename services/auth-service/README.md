# MatchSentinel Auth Service

Authentication and identity microservice for MatchSentinel.

## Features
- User registration and login
- JWT access tokens + refresh tokens
- Email verification flow
- Password hashing (BCrypt)
- Login rate limiting
- Liquibase-managed schema

## Endpoints (base: /api/auth)
- POST /register
- POST /login
- POST /refresh
- POST /verify-email
- POST /introspect
- GET /me (requires Authorization: Bearer <token>)

## Configuration
Set these via environment variables in production:
- DB_URL, DB_USER, DB_PASSWORD
- AUTH_JWT_SECRET (at least 32 chars)
- AUTH_CORS_ORIGINS (comma-separated)
- AUTH_EMAIL_VERIFICATION_MINUTES (optional, default 30)

Local defaults live in `src/main/resources/application.properties`.

## Run
```
./mvnw spring-boot:run
```

## Test
```
./mvnw test
```

## Notes
- Do not commit real secrets to a public repo.
- The verification token is currently returned in the register response (dev convenience).
