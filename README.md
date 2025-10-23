# skat

Monorepo with **Backend (Java 21, Spring Boot)** at the root level and **Frontend (Angular 18)** in the `frontend` folder.

## Structure

```
.
├── .github/workflows/build.yml
├── frontend/              # Angular 18 App
├── src/                   # Spring Boot Backend (Java 21)
│   ├── main/
│   └── test/
├── target/
├── pom.xml
└── README.md
```

- **Backend:** Spring Boot (Java 21), REST API
- **Frontend:** Angular 18, consumes backend API
- **Tests:** JUnit 5 + AssertJ (Given-When-Then), **@SpringBootTest** (preferred over `@WebMvcTest`), **Testcontainers** for integration/DB tests

## Quickstart

### Backend
```bash
# Requires Java 21
./mvnw spring-boot:run
# or
./mvnw test
```

### Frontend
```bash
cd frontend
npm ci
npm start
```

## Documentation
- [ARCHITECTURE.md](./ARCHITECTURE.md)
- [BACKEND.md](./BACKEND.md)
- [FRONTEND.md](./FRONTEND.md)
- [TESTING.md](./TESTING.md)
- ADRs: see `docs/architecture/decisions/`
