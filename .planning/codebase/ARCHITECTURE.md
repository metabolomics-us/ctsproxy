# Architecture

**Analysis Date:** 2026-03-09

## Pattern Overview

**Overall:** Multi-module Maven project with a Spring Boot backend (Scala) serving as a proxy/facade over an upstream CTS (Chemical Translation Service) API, with an Angular SPA frontend.

**Key Characteristics:**
- Three-module Maven build: `casetojson` (serialization), `ctsclient` (API client library), `web` (Spring Boot app + Angular frontend)
- Backend acts as a caching proxy, forwarding requests to the upstream CTS service at `http://cts` (configurable)
- Local SMILES conversion via CDK library to avoid upstream dependency for InChIKey-to-SMILES conversions
- Angular frontend built by `frontend-maven-plugin` and served as static resources from the Spring Boot JAR
- Caffeine-based in-memory caching on all REST endpoints (30-second TTL)

## Layers

**Frontend (Angular SPA):**
- Purpose: Provides UI for single and batch chemical identifier conversions
- Location: `web/frontend/src/app/`
- Contains: Angular components, services, routes
- Depends on: Backend REST API at `/rest/*` and proxy at `/service/*`
- Used by: End users via browser

**Web Controllers:**
- Purpose: Expose REST API endpoints and serve the Angular SPA
- Location: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/`
- Contains: `CtsController` (main REST API), `ProxyController` (reverse proxy), `ViewController` (SPA routing)
- Depends on: `CtsService` (client API trait), `SmilesConversionService`, Spring caching
- Used by: Frontend Angular app

**Web Services:**
- Purpose: Local chemical conversion logic using CDK
- Location: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/`
- Contains: `CdkSmilesService` (MOL-to-SMILES via CDK), `SmilesConversionService` (orchestrates InChIKey-to-SMILES)
- Depends on: `CtsService` (to fetch MOL blocks), CDK library
- Used by: `CtsController`

**Client API (ctsclient):**
- Purpose: Typed Scala client for the upstream CTS REST API
- Location: `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/`
- Contains: `CtsService` trait (API contract), `CtsClient` (RestTemplate implementation), `Types` (case classes)
- Depends on: Spring `RestTemplate`, Jackson, upstream CTS service
- Used by: Web controllers and services

**Serialization (casetojson):**
- Purpose: Auto-configures Jackson ObjectMapper with Scala module support and a custom RestTemplate
- Location: `casetojson/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/casetojson/config/`
- Contains: `CaseClassToJSONSerializationAutoConfiguration` (Spring auto-config)
- Depends on: Jackson, Jackson-Scala module, commons-io
- Used by: All modules via Spring Boot auto-configuration

## Data Flow

**Single Conversion (standard):**

1. User enters a chemical name and target identifier type in the Angular UI
2. `TranslationService` calls `GET /rest/convert/{from}/{to}/{searchTerm}`
3. `CtsController.convertSimple()` delegates to `CtsClient.convert()` which calls the upstream CTS API
4. Response is cached by Caffeine (30s TTL) and returned as JSON

**Single Conversion (SMILES target):**

1. `CtsController.convertSimple()` detects `to=SMILES`
2. If `from=InChIKey`: calls `SmilesConversionService.inchikeyToSmiles()` directly
3. Otherwise: converts `from -> InChIKey` via upstream CTS, then for each InChIKey fetches MOL block via `CtsClient.inchiKey2Mol()`, then converts MOL to SMILES locally via `CdkSmilesService.molToSmiles()`
4. Falls back to upstream CTS SMILES conversion if CDK conversion fails

**Batch Conversion:**

1. User enters multiple search terms (one per line) and multiple target types
2. `BatchConversionComponent` iterates over each term+target pair sequentially
3. Each call goes through the same `TranslationService.convert()` path
4. Results stream into the UI as each conversion completes

**Compound Property Lookup (frontend-only):**

1. For "Exact Mass", "Molecular Formula", "Molecular Weight" conversions from InChIKey
2. `TranslationService` calls `GET /service/compound/{inchikey}` (proxy path)
3. `ProxyController` forwards to the upstream CTS service
4. Frontend extracts the specific property from the compound response

**Reverse Proxy:**

1. Any request to `/service/**` hits `ProxyController`
2. The controller reconstructs the full URL with the upstream target (`cts.proxy.url`)
3. Forwards the request via `RestTemplate` and returns the raw byte response

**State Management:**
- Backend: Stateless with Caffeine cache (30s expiry)
- Frontend: Component-local state using Angular signals (`signal()`)

## Key Abstractions

**CtsService trait:**
- Purpose: Defines the contract for all CTS API operations
- Location: `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/api/CtsService.scala`
- Pattern: Trait (interface) with a single implementation `CtsClient`
- Methods: `convert`, `score`, `inchiKey2Mol`, `expandFormula`, `compoundInfo`, `compoundSynonyms`, `compoundBiologicalCount`, `chemifyQuery`, `sourceIdNames`, `targetIdNames`, `mol2Inchi`, `inchi2Mol`, `smiles2Inchi`, `inchiCode2InchiKey`

**Type Case Classes:**
- Purpose: Typed representations of all API request/response payloads
- Location: `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/types/Types.scala`
- Key types: `ConversionResult`, `ScoreResult`, `MoleculeResponse`, `FormulaResponse`, `CompoundResponse`, `InChIPairResponse`, `InChIResponse`, `BatchRequest`

**TranslationService (frontend):**
- Purpose: Central service orchestrating all conversion logic in the frontend
- Location: `web/frontend/src/app/services/translation.service.ts`
- Pattern: Injectable singleton with routing logic for different conversion types (scoring vs. compound property vs. general)

## Entry Points

**Spring Boot Application:**
- Location: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/CtsProxy.scala`
- Triggers: `object CtsProxy extends App` -- starts Spring Boot with SERVLET web type
- Responsibilities: Bootstrap the application, configure beans, enable caching

**Angular Application:**
- Location: `web/frontend/src/main.ts`
- Triggers: Browser loads `index.html` which bootstraps Angular
- Responsibilities: Render the SPA, route between pages

**REST API Endpoints (`/rest/*`):**
- Location: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala`
- Key endpoints:
  - `GET /rest/convert/{from}/{to}/{searchTerm}` - Main conversion
  - `GET /rest/fromValues` - Available source identifiers
  - `GET /rest/toValues` - Available target identifiers
  - `GET /rest/score/{from}/{value}/{algorithm}` - Scored conversion
  - `GET /rest/inchikeytosmiles/{inchikey}` - InChIKey to SMILES
  - `GET /rest/expandformula/{formula}` - Formula expansion
  - `GET /rest/inchikeytomol/{inchikey}` - InChIKey to MOL
  - `GET /rest/synonyms/{inchikey}` - Compound synonyms
  - `GET /rest/countBiological/{inchikey}` - Biological count
  - `POST /rest/moltoinchi` - MOL to InChI
  - `POST /rest/smilestoinchi` - SMILES to InChI
  - `POST /rest/inchicodetoinchikey` - InChI to InChIKey
  - `POST /rest/inchicodetomol` - InChI to MOL

**Proxy Endpoint (`/service/**):**
- Location: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ProxyController.scala`
- Triggers: Any HTTP request to `/service/**`
- Responsibilities: Forward requests to upstream CTS backend

**SPA Route Handler:**
- Location: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ViewController.scala`
- Triggers: Browser navigation to `/`, `/batch`, `/services`
- Responsibilities: Serve `index.html` for Angular HTML5 routing

## Error Handling

**Strategy:** Mixed -- exceptions in backend, try/catch in frontend

**Patterns:**
- `CtsController`: Throws `ResponseStatusException` for unsupported conversions (SMILES source, ChemSpider). SMILES conversion failures fall back to upstream CTS API with `logger.warn`.
- `CtsClient`: Custom `ResponseErrorHandler` on RestTemplate wraps HTTP errors as `RestClientException` with status code and body.
- `ProxyController`: Catches `HttpStatusCodeException` and returns the upstream error response verbatim.
- Frontend components: Catch errors in `async` methods and push messages to `errors` signal for display.

## Cross-Cutting Concerns

**Logging:** `scala-logging` (`LazyLogging` trait) wrapping SLF4J. Debug-level request/response logging via `LoggingInterceptor` on RestTemplate.

**Caching:** Spring `@Cacheable` annotations on all `CtsController` endpoints and `SmilesConversionService`. Caffeine backend with 30-second TTL configured in `web/src/main/resources/application.yml`.

**CORS:** Global CORS enabled via `CtsCors` WebMvcConfigurer (`addMapping("/**")`). Additionally `@CrossOrigin(origins = Array("*"))` on `CtsController` and `ViewController`.

**Serialization:** Custom Jackson ObjectMapper configured via `CaseClassToJSONSerializationAutoConfiguration` with Scala module, JavaTimeModule, non-null inclusion, and lenient deserialization settings. Auto-configured via Spring Boot's `AutoConfiguration.imports` mechanism.

**Content Negotiation:** Default content type set to `application/json`, accept header respected.

---

*Architecture analysis: 2026-03-09*
