# Skat Backend

Spring Boot REST API backend for the Skat application.

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.5.6
- **Database**: PostgreSQL (production), H2 (development)
- **Build Tool**: Maven

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- PostgreSQL (for production mode)

## Getting Started

### Running with H2 In-Memory Database (Development)

The easiest way to start the application for development is using the H2 in-memory database:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

### Running with PostgreSQL (Production)

1. Ensure PostgreSQL is running and create a database:
   ```sql
   CREATE DATABASE skatdb;
   ```

2. Update the database credentials in `src/main/resources/application.properties` if needed:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/skatdb
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

3. Start the application:
   ```bash
   mvn spring-boot:run
   ```

### Building the Application

To build the application and create an executable JAR:

```bash
mvn clean package
```

The JAR file will be created in the `target/` directory.

### Running the JAR

After building, you can run the application using:

```bash
# With H2 (development)
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# With PostgreSQL (production)
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Running Tests

Execute the test suite:

```bash
mvn test
```

## API Endpoints

### Hello World Endpoint

- **URL**: `/api/hello`
- **Method**: `GET`
- **Response**: Plain text "Hallo"

Example:
```bash
curl http://localhost:8080/api/hello
```

## Configuration Profiles

### Development Profile (`dev`)

Uses H2 in-memory database. Configuration file: `application-dev.properties`

- Database: H2 in-memory
- H2 Console: Enabled at `/h2-console`
- SQL logging: Enabled

### Default Profile (Production)

Uses PostgreSQL database. Configuration file: `application.properties`

- Database: PostgreSQL
- Connection pooling: HikariCP (default)

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/skat/backend/
│   │   │       ├── SkatBackendApplication.java    # Main application class
│   │   │       └── controller/
│   │   │           └── HelloWorldController.java  # REST controller
│   │   └── resources/
│   │       ├── application.properties              # Production config
│   │       └── application-dev.properties          # Development config
│   └── test/
│       └── java/
│           └── com/skat/backend/
│               └── controller/
│                   └── HelloWorldControllerTest.java  # Controller tests
├── pom.xml                                         # Maven configuration
└── README.md                                       # This file
```

## Troubleshooting

### Port Already in Use

If port 8080 is already in use, you can change it by adding to your run command:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Database Connection Issues

- Verify PostgreSQL is running: `pg_isready`
- Check PostgreSQL logs for connection errors
- Verify database credentials in `application.properties`

## Development

### Adding New Dependencies

Add dependencies to `pom.xml` and run:

```bash
mvn clean install
```

### Code Style

The project follows standard Java coding conventions. Ensure your IDE is configured to use:
- Tab size: 4 spaces
- Charset: UTF-8
