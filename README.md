# CTS Proxy

Angular frontend and Spring Boot backend proxy for the [Chemical Translation Service (CTS)](https://cts.fiehnlab.ucdavis.edu). Allows users to convert chemical identifiers (InChIKey, SMILES, CAS, etc.) between formats via single or batch conversion.

## Prerequisites

- Java 17+
- Maven 3.6+
- Node.js 18+ and npm 10+

## Project Structure

```
ctsproxy/
├── casetojson/          # CAS-to-JSON utility library
├── ctsclient/           # CTS REST client library
├── web/
│   ├── src/main/scala/  # Spring Boot backend (Scala)
│   ├── src/main/resources/application.yml
│   └── frontend/        # Angular 21 frontend
└── pom.xml              # Parent Maven POM
```

## Development Setup

### 1. Build dependencies

From the project root, install the shared libraries:

```bash
mvn clean install -pl casetojson,ctsclient
```

### 2. Start the backend

```bash
mvn spring-boot:run -pl web
```

This starts the Spring Boot server on `http://localhost:8080`. It proxies requests to the upstream CTS service configured in `web/src/main/resources/application.yml`.

### 3. Start the frontend

```bash
cd web/frontend
npm install
npm start
```

This runs `ng serve` on `http://localhost:4200` with a dev proxy that forwards `/rest` and `/service` requests to `http://localhost:8080` (configured in `web/frontend/proxy.conf.json`).

### Running tests

```bash
# Frontend tests
cd web/frontend
npm test

# Backend tests
mvn test
```

## Production Build

```bash
mvn clean install
```

This builds the full application including the Angular frontend, which gets packaged into the Spring Boot JAR.

## Swarm Deployment

1. Push image to repo
2. SSH to swarm manager node
3. Pull image from repo
4. Update the service:

```bash
docker service update --force cts_ctsproxy
```

## Contact

- Diego - backend and frontend
