# Codebase Structure

**Analysis Date:** 2026-03-09

## Directory Layout

```
ctsproxy/
├── pom.xml                          # Parent POM (multi-module Maven project)
├── casetojson/                      # Module: Jackson/Scala serialization auto-config
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── resources/META-INF/spring/  # Spring auto-configuration registration
│       │   └── scala/.../casetojson/config/ # Auto-configuration class
│       └── test/
│           └── scala/.../casetojson/config/ # Configuration tests
├── ctsclient/                       # Module: CTS API client library
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── resources/application.yml    # Client config (cts.old.url)
│       │   └── scala/.../client/
│       │       ├── api/                     # Service trait (interface)
│       │       ├── core/                    # Client implementation
│       │       └── types/                   # Case class DTOs
│       └── test/
│           └── scala/.../client/core/       # Client tests
├── web/                             # Module: Spring Boot web app + Angular frontend
│   ├── pom.xml
│   ├── frontend/                    # Angular application
│   │   ├── proxy.conf.json          # Dev proxy config (localhost:8080)
│   │   ├── src/
│   │   │   ├── main.ts              # Angular bootstrap
│   │   │   ├── index.html           # SPA entry HTML
│   │   │   └── app/
│   │   │       ├── app.ts           # Root component
│   │   │       ├── app.html         # Root template
│   │   │       ├── app.config.ts    # Angular providers config
│   │   │       ├── app.routes.ts    # Route definitions
│   │   │       ├── components/      # Feature components
│   │   │       │   ├── batch-conversion/
│   │   │       │   ├── navigation/
│   │   │       │   ├── result-table/
│   │   │       │   ├── services-info/
│   │   │       │   └── single-conversion/
│   │   │       └── services/        # Angular services
│   │   │           ├── translation.service.ts
│   │   │           └── download.service.ts
│   │   └── public/                  # Static assets
│   └── src/
│       ├── main/
│       │   ├── resources/
│       │   │   ├── application.yml           # Spring Boot config
│       │   │   └── META-INF/spring/          # Auto-config registration
│       │   └── scala/.../web/
│       │       ├── CtsProxy.scala            # App entry point + config
│       │       ├── controllers/
│       │       │   ├── CtsController.scala   # REST API endpoints
│       │       │   ├── ProxyController.scala # Reverse proxy
│       │       │   └── ViewController.scala  # SPA route handler
│       │       └── services/
│       │           ├── CdkSmilesService.scala          # CDK MOL-to-SMILES
│       │           └── SmilesConversionService.scala    # InChIKey-to-SMILES orchestration
│       └── test/
│           ├── resources/application.yml     # Test config
│           └── scala/.../web/
│               ├── CtsProxyForwardingTest.scala
│               ├── controllers/CtsControllerTest.scala
│               └── services/
│                   ├── CdkSmilesServiceTest.scala
│                   └── SmilesConversionServiceTest.scala
└── .planning/                       # Planning and analysis docs
```

Note: Scala source paths follow the deep package convention `edu/ucdavis/fiehnlab/ctsrest/...` which is abbreviated as `.../` above.

## Directory Purposes

**`casetojson/`:**
- Purpose: Shared serialization module providing Jackson ObjectMapper configured for Scala case classes
- Contains: One auto-configuration class, one test, Spring Boot auto-config registration
- Key files: `casetojson/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/casetojson/config/CaseClassToJSONSerializationAutoConfiguration.scala`

**`ctsclient/`:**
- Purpose: Reusable typed client library for the upstream CTS REST API
- Contains: Service trait (interface), client implementation, response/request DTOs
- Key files:
  - `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/api/CtsService.scala` - API contract
  - `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClient.scala` - Implementation
  - `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/types/Types.scala` - All DTOs

**`web/`:**
- Purpose: The deployable Spring Boot application with embedded Angular frontend
- Contains: REST controllers, services, configuration, and the full Angular SPA
- Key files:
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/CtsProxy.scala` - Application entry point
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala` - Primary REST API
  - `web/src/main/resources/application.yml` - All Spring Boot configuration

**`web/frontend/`:**
- Purpose: Angular SPA for chemical identifier conversion UI
- Contains: Angular components, services, routes, Material Design UI
- Key files:
  - `web/frontend/src/app/app.routes.ts` - Route definitions
  - `web/frontend/src/app/services/translation.service.ts` - Core conversion logic
  - `web/frontend/src/app/services/download.service.ts` - CSV/TSV export

## Key File Locations

**Entry Points:**
- `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/CtsProxy.scala`: Spring Boot application main
- `web/frontend/src/main.ts`: Angular bootstrap

**Configuration:**
- `pom.xml`: Parent POM with module declarations, Scala version, dependency management
- `web/pom.xml`: Web module POM with frontend-maven-plugin, CDK dependencies, Spring Boot plugin
- `ctsclient/pom.xml`: Client module POM
- `casetojson/pom.xml`: Serialization module POM
- `web/src/main/resources/application.yml`: Spring Boot runtime config (cache, logging, upstream URLs)
- `ctsclient/src/main/resources/application.yml`: Default client config (`cts.old.url`)
- `web/frontend/proxy.conf.json`: Angular dev server proxy to Spring Boot backend

**Core Logic:**
- `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala`: All REST API endpoints
- `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ProxyController.scala`: Reverse proxy to upstream CTS
- `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClient.scala`: Upstream API client
- `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/SmilesConversionService.scala`: Local SMILES conversion orchestration
- `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/CdkSmilesService.scala`: CDK-based MOL-to-SMILES

**Testing:**
- `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsControllerTest.scala`
- `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/CdkSmilesServiceTest.scala`
- `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/SmilesConversionServiceTest.scala`
- `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/CtsProxyForwardingTest.scala`
- `ctsclient/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClientTest.scala`
- `casetojson/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/casetojson/config/CaseClassToJSONSerializationAutoConfigurationTest.scala`

## Naming Conventions

**Files (Scala):**
- PascalCase matching the class name: `CtsController.scala`, `CdkSmilesService.scala`
- One primary class per file

**Files (TypeScript):**
- kebab-case with type suffix: `translation.service.ts`, `single-conversion.component.ts`
- Angular CLI convention: `{name}.{type}.{ext}`

**Directories (Scala):**
- Lowercase matching Java/Scala package convention: `controllers/`, `services/`, `types/`, `core/`, `api/`

**Directories (Angular):**
- kebab-case: `batch-conversion/`, `single-conversion/`, `services-info/`

**Scala Classes:**
- PascalCase: `CtsController`, `CdkSmilesService`, `SmilesConversionService`
- Traits use PascalCase without "I" prefix: `CtsService`
- Case classes use PascalCase: `ConversionResult`, `MoleculeResponse`

**Angular Classes:**
- PascalCase with type suffix: `TranslationService`, `SingleConversionComponent`, `ResultTableComponent`

## Where to Add New Code

**New REST Endpoint:**
- Controller: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala` (add method with `@GetMapping`/`@PostMapping` and `@Cacheable`)
- If it needs upstream CTS call: Add method to `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/api/CtsService.scala` (trait) and implement in `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClient.scala`
- New response type: Add case class to `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/types/Types.scala`
- Cache name: Add to `spring.cache.cache-names` list in `web/src/main/resources/application.yml`
- Tests: `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsControllerTest.scala`

**New Backend Service:**
- Implementation: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/` (new `*.scala` file with `@Service`)
- Tests: `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/`

**New Angular Component:**
- Create directory: `web/frontend/src/app/components/{component-name}/`
- Files: `{component-name}.component.ts`, `{component-name}.component.html`, `{component-name}.component.scss`
- Add route in `web/frontend/src/app/app.routes.ts`
- Add SPA forward path in `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ViewController.scala`

**New Angular Service:**
- Location: `web/frontend/src/app/services/{name}.service.ts`
- Use `@Injectable({ providedIn: 'root' })` pattern

**New DTO/Type:**
- Scala: `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/types/Types.scala`
- TypeScript: Define interface in the relevant service file or component

## Special Directories

**`web/frontend/node/`:**
- Purpose: Node.js binary downloaded by frontend-maven-plugin
- Generated: Yes (by Maven build)
- Committed: No

**`web/frontend/node_modules/`:**
- Purpose: npm dependencies
- Generated: Yes (by npm install)
- Committed: No

**`web/frontend/dist/`:**
- Purpose: Angular production build output
- Generated: Yes (by `ng build`)
- Committed: No
- Note: Contents are copied to `target/classes/static/` during Maven build

**`*/target/`:**
- Purpose: Maven build output
- Generated: Yes
- Committed: No

**`web/frontend/.angular/`:**
- Purpose: Angular CLI cache
- Generated: Yes
- Committed: No

**`.planning/`:**
- Purpose: Project planning and analysis documents
- Generated: Semi-manual
- Committed: Yes

---

*Structure analysis: 2026-03-09*
