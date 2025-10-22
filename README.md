# Skat

A mono-repo project with Java 21 Spring Boot backend and Angular 18 frontend.

## Project Structure

```
.
├── backend/          # Spring Boot backend (Java 21)
├── frontend/         # Angular 18 frontend
└── .github/          # CI/CD workflows
    └── workflows/
        └── build.yml # Build pipeline
```

## Backend

### Technology Stack
- Java 21
- Spring Boot 3.4.2
- PostgreSQL
- Maven

### Build and Run

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### API Endpoints

- `GET /api/hello` - Returns "Hallo"

### Database Configuration

PostgreSQL configuration is in `backend/src/main/resources/application.properties`:
- URL: `jdbc:postgresql://localhost:5432/skatdb`
- Username: `postgres`
- Password: `postgres`

## Frontend

### Technology Stack
- Angular 18
- TypeScript
- Node.js 20

### Build and Run

```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:4200`

### Features

- Calls the backend `/api/hello` endpoint
- Displays the response without formatting

## Build Pipeline

The project includes a GitHub Actions workflow that:
- Builds the backend with Maven
- Runs backend tests
- Builds the frontend with npm
- Uploads build artifacts

## Development

1. Start the backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. In a new terminal, start the frontend:
   ```bash
   cd frontend
   npm start
   ```

3. Open your browser at `http://localhost:4200`
