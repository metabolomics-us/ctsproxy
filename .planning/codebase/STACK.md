# Technology Stack

**Analysis Date:** 2026-03-09

## Languages

**Primary:**
- Scala 2.13.18 - All backend logic (controllers, services, client, types)
- TypeScript ~5.9.2 - Angular frontend application

**Secondary:**
- HTML/SCSS - Angular component templates and styles
- XML - Maven POM build configuration

## Runtime

**Environment:**
- JVM (Java 17) - Spring Boot backend
- Node.js v22.18.0 - Frontend build toolchain (managed by frontend-maven-plugin)

**Package Manager:**
- Maven - Backend build and dependency management (multi-module POM)
- npm 10.9.3 - Frontend dependency management
- Lockfile: `web/frontend/package-lock.json` (present)

## Frameworks

**Core:**
- Spring Boot 3.5.11 - Web application framework (`pom.xml` parent)
- Angular 21.2.0 - Frontend SPA framework (`web/frontend/package.json`)

**Testing:**
- ScalaTest 3.2.19 - Backend unit/integration tests
- Vitest 4.0.8 - Frontend unit tests
- Spring Boot Test - Backend test support

**Build/Dev:**
- Maven (multi-module) - Build orchestration
- scala-maven-plugin 4.9.2 - Scala compilation within Maven
- frontend-maven-plugin 1.15.1 - Node/npm/Angular build integration (`web/pom.xml`)
- Angular CLI 21.2.0 - Frontend dev server and build
- Prettier 3.8.1 - Frontend code formatting

## Key Dependencies

**Critical:**
- `spring-boot-starter-web` - REST API server (embedded Tomcat)
- `spring-boot-starter-cache` + Caffeine - In-memory response caching
- CDK (Chemistry Development Kit) 2.12 - Local SMILES chemical structure conversions
  - `cdk-core`, `cdk-ctab`, `cdk-smiles`, `cdk-silent` (`web/pom.xml`)
- Jackson + `jackson-module-scala` - JSON serialization for Scala case classes
- `jackson-datatype-jsr310` - Java Time API support in JSON

**Infrastructure:**
- `spring-boot-starter-actuator` - Health/metrics endpoints (`ctsclient/pom.xml`)
- `commons-io 2.15.1` - IO utilities for response body reading
- `scala-logging 3.9.5` (via `com.typesafe.scala-logging`) - Logging facade
- Logback - Logging implementation (`web/src/main/resources/logback.xml`)

**Frontend:**
- Bootstrap 5.3 - CSS framework and JS bundle (`web/frontend/angular.json`)
- Angular Material 21.2.0 + Angular CDK 21.2.0 - UI components
- RxJS 7.8 - Reactive programming

## Modules

The project is a Maven multi-module build with three submodules:

| Module | Artifact | Purpose |
|--------|----------|---------|
| `casetojson/` | `casetojson` | Custom Jackson ObjectMapper and RestTemplate for Scala case class serialization |
| `ctsclient/` | `ctsclient` | HTTP client for the upstream CTS (Chemical Translation Service) API |
| `web/` | `web` | Spring Boot web app (REST controllers + embedded Angular SPA) |

Dependency chain: `web` depends on `ctsclient` depends on `casetojson`.

## Configuration

**Application Config:**
- `web/src/main/resources/application.yml` - Main Spring Boot config
  - `fiehnlab.cts.config.url` - Upstream CTS URL (default: `https://cts.fiehnlab.ucdavis.edu`)
  - `fiehnlab.cts.config.connectTimeoutMillis` - Connection timeout (default: 1000ms)
  - `fiehnlab.cts.config.readTimeoutMillis` - Read timeout (default: 120000ms)
  - `cts.proxy.url` - Reverse proxy target for `/service/**` routes (default: `http://cts`)
  - `cts.old.url` - Legacy CTS API base URL (default: `https://oldcts.fiehnlab.ucdavis.edu/service`)
  - Cache: Caffeine with 30s TTL, 13 named caches

**Logging:**
- `web/src/main/resources/logback.xml` - Console appender with pattern layout

**Frontend Dev Proxy:**
- `web/frontend/proxy.conf.json` - Proxies `/rest` and `/service` to `http://localhost:8080`

**Build:**
- `pom.xml` - Parent POM (Spring Boot 3.5.11 parent, module declarations)
- `web/pom.xml` - Frontend build integration, Spring Boot packaging
- `web/frontend/angular.json` - Angular build config (SCSS, Bootstrap assets)
- `web/frontend/tsconfig.json` - TypeScript config

## Platform Requirements

**Development:**
- JDK 17+
- Maven 3.x (uses Maven wrapper or system Maven)
- No separate Node.js install required (frontend-maven-plugin downloads Node v22.18.0)
- For frontend-only dev: `cd web/frontend && npm install && npm start` (proxies to backend on port 8080)

**Production:**
- Docker image based on `eclipse-temurin:17-jre-alpine`
- Docker registry: `eros.fiehnlab.ucdavis.edu`
- Exposed port: 8080
- JVM max heap: 2048m (`-Xmx2048m`)
- Docker build activated by `.docker` sentinel file (present in `web/` module)
- Angular build output bundled as static resources in Spring Boot JAR at `/static/`

---

*Stack analysis: 2026-03-09*
