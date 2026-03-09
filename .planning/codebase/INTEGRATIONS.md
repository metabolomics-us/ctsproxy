# External Integrations

**Analysis Date:** 2026-03-09

## APIs & External Services

**CTS (Chemical Translation Service) - Primary upstream API:**
- Purpose: Chemical identifier conversion, compound info, scoring, formula expansion
- Base URL (old): configured via `cts.old.url` (default: `https://oldcts.fiehnlab.ucdavis.edu/service`)
- Client: `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClient.scala`
- Interface: `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/api/CtsService.scala`
- Auth: None (public API)
- Transport: Spring `RestTemplate` with custom Jackson/Scala serialization
- Timeouts: connect 1000ms, read 120000ms (configurable in `application.yml`)

**CTS API endpoints consumed by CtsClient:**

| Method | Endpoint Pattern | Proxy Method |
|--------|-----------------|--------------|
| GET | `/convert/{from}/{to}/{searchTerm}` | `convert()` |
| GET | `/score/{from}/{value}/{algorithm}` | `score()` |
| GET | `/inchikeytomol/{inchikey}` | `inchiKey2Mol()` |
| GET | `/expandFormula/{formula}` | `expandFormula()` |
| GET | `/compound/{inchikey}` | `compoundInfo()` |
| GET | `/synonyms/{inchikey}` | `compoundSynonyms()` |
| GET | `/count/{inchikey}` | `compoundExtidCount()` |
| GET | `/countBiological/{inchikey}` | `compoundBiologicalCount()` |
| GET | `/conversion/fromValues` | `sourceIdNames()` |
| GET | `/conversion/toValues` | `targetIdNames()` |
| GET | `/chemify/rest/identify/{name}` | `chemifyQuery()` |
| POST | `/inchitomol` | `inchi2Mol()` |
| POST | `/moltoinchi` | `mol2Inchi()` |
| POST | `/smilestoinchi` | `smiles2Inchi()` |
| POST | `/inchicodetoinchikey` | `inchiCode2InchiKey()` |

**CTS Reverse Proxy - Passthrough forwarding:**
- Purpose: Forward `/service/**` requests directly to the full CTS backend
- Target URL: configured via `cts.proxy.url` (default: `http://cts`)
- Controller: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ProxyController.scala`
- Behavior: Full request/response passthrough (headers, body, query params)
- Used by frontend for compound property lookups (e.g., `/service/compound/{inchikey}`)

## Local Chemical Processing

**CDK (Chemistry Development Kit):**
- Purpose: Local MOL-to-SMILES conversion without external API calls
- Service: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/CdkSmilesService.scala`
- Orchestrator: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/SmilesConversionService.scala`
- Flow: InChIKey -> CTS API (get MOL block) -> CDK (MOL to SMILES)
- Fallback: If CDK conversion fails, falls back to upstream CTS API for SMILES conversion

## Data Storage

**Databases:**
- None. This is a stateless proxy/conversion service.

**Caching:**
- Caffeine in-memory cache (no external cache service)
- Configuration: `web/src/main/resources/application.yml`
- TTL: 30 seconds (`expireAfterWrite=30s`)
- Named caches: `simple_convert`, `expand_formula`, `from_values`, `to_values`, `bio_count`, `scoring`, `inchikey_mol`, `cmpd_synonyms`, `mol2inchi`, `smiles2inchi`, `inchi2inchikey`, `inchi2mol`, `inchikey_to_smiles`
- Applied via `@Cacheable` annotations on controller and service methods

**File Storage:**
- None. No file persistence.

## Authentication & Identity

**Auth Provider:**
- None. The API is fully open with `@CrossOrigin(origins = Array("*"))` on all controllers.

## Monitoring & Observability

**Health/Metrics:**
- Spring Boot Actuator (`spring-boot-starter-actuator` in `ctsclient/pom.xml`)
- Standard actuator endpoints available (health, info, etc.)

**Logging:**
- Logback via `web/src/main/resources/logback.xml` - Console appender only
- `scala-logging` (LazyLogging trait) used throughout Scala codebase
- HTTP request/response logging via `LoggingInterceptor` in `casetojson/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/casetojson/config/CaseClassToJSONSerializationAutoConfiguration.scala`

**Error Tracking:**
- None. Errors logged to console only.

## CI/CD & Deployment

**Hosting:**
- Docker container on private registry `eros.fiehnlab.ucdavis.edu`
- Image name: `ctsproxy`
- Base image: `eclipse-temurin:17-jre-alpine`

**CI Pipeline:**
- Not detected (no `.github/workflows`, Jenkinsfile, or similar CI config found)

**Docker Build:**
- Triggered by Maven `docker` profile (activated by `.docker` sentinel file in `web/`)
- Plugin: `com.spotify:docker-maven-plugin:0.4.13`
- Includes `wait-for-it.sh` for container startup orchestration
- Tags: `${project.version}` and `latest`

## Environment Configuration

**Required env vars / Spring properties:**
- `cts.old.url` - CTS legacy API URL (has default: `https://oldcts.fiehnlab.ucdavis.edu/service`)
- `cts.proxy.url` - CTS proxy target URL (has default: `http://cts`)
- `fiehnlab.cts.config.url` - CTS config URL (has default: `https://cts.fiehnlab.ucdavis.edu`)

All configuration has sensible defaults. No mandatory env vars without defaults detected.

**Secrets:**
- None required. No authentication tokens or API keys.

## Webhooks & Callbacks

**Incoming:**
- None

**Outgoing:**
- None

## REST API Surface (Exposed by this proxy)

**CtsController** (`/rest`):
- `GET /rest/convert/{from}/{to}/{searchTerm}` - Chemical identifier conversion
- `GET /rest/inchikeytosmiles/{inchikey}` - InChIKey to SMILES (local CDK)
- `GET /rest/expandformula/{formula}` - Formula expansion
- `GET /rest/fromValues` - Available source identifier types
- `GET /rest/toValues` - Available target identifier types
- `GET /rest/countBiological/{inchikey}` - Biological source counts
- `GET /rest/score/{from}/{value}/{algorithm}` - Scoring
- `GET /rest/inchikeytomol/{inchikey}` - InChIKey to MOL block
- `GET /rest/synonyms/{inchikey}` - Compound synonyms
- `POST /rest/moltoinchi` - MOL to InChI
- `POST /rest/smilestoinchi` - SMILES to InChI
- `POST /rest/inchicodetoinchikey` - InChI code to InChIKey
- `POST /rest/inchicodetomol` - InChI code to MOL

**ProxyController** (`/service/**`):
- Transparent reverse proxy to upstream CTS backend

**ViewController** (`/`, `/batch`, `/services`):
- Forwards to `index.html` for Angular SPA routing

---

*Integration audit: 2026-03-09*
