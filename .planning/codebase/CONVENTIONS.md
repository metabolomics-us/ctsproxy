# Coding Conventions

**Analysis Date:** 2026-03-09

## Languages

This is a dual-language project:
- **Backend:** Scala 2.13 on Spring Boot 3.5 (Java 17 target)
- **Frontend:** TypeScript (strict mode) with Angular 21 and Angular Material 21

## Naming Patterns

### Scala Backend

**Packages:**
- Use reverse-domain Java convention: `edu.ucdavis.fiehnlab.ctsrest.{module}.{layer}`
- Examples: `edu.ucdavis.fiehnlab.ctsrest.client.core`, `edu.ucdavis.fiehnlab.ctsrest.web.controllers`

**Classes:**
- PascalCase for classes, traits, and objects
- Suffix controllers with `Controller`: `CtsController`, `ProxyController`, `ViewController`
- Suffix services with `Service`: `CdkSmilesService`, `SmilesConversionService`
- Suffix test classes with `Test`: `CtsClientTest`, `CtsControllerTest`
- Suffix Spring configurations with `Config` or `Configuration`: `CtsProxyConfig`, `CaseClassToJSONSerializationAutoConfiguration`

**Methods:**
- camelCase for all methods: `convertSimple`, `inchiKey2Mol`, `compoundSynonyms`
- Use chemical shorthand in method names: `mol2Inchi`, `smiles2Inchi`, `inchiCode2InchiKey`
- Getter-style methods omit `get` prefix: `sourceIdNames()`, `targetIdNames()`

**Case Classes (DTOs):**
- PascalCase for class name, camelCase for fields
- Use `Response` suffix for API response types: `MoleculeResponse`, `FormulaResponse`, `ScoreResult`
- Use `Request` suffix for request bodies: `BatchRequest`, `InChIConversionRequest`
- Defined in `Types.scala` as a single file for all shared types

**Vals/Vars:**
- camelCase: `baseUrl`, `restTemplate`, `proxyTargetUrl`

### TypeScript Frontend

**Files:**
- Kebab-case with dot-separated type: `single-conversion.component.ts`, `translation.service.ts`
- Component files grouped in directories by component name

**Classes:**
- PascalCase: `SingleConversionComponent`, `TranslationService`, `DownloadService`

**Methods/Properties:**
- camelCase: `getFromValues`, `convertSingle`, `queryString`
- Private methods prefixed with nothing special, use TypeScript `private` keyword

**Interfaces/Types:**
- PascalCase: `ConversionResultItem`, `QueryLike`, `ResultsMap`
- Export type aliases for simple union types: `export type ExportStyle = 'table' | 'list'`

## Code Style

### Scala Formatting
- No automated formatter detected (no scalafmt config)
- 2-space indentation in source files
- Case class fields indented and aligned with closing paren on own line
- Import grouping: Java/Scala stdlib, then Spring, then project imports (not strictly enforced)

### TypeScript Formatting
- **Prettier** with config at `web/frontend/.prettierrc`
- Key settings:
  - `printWidth: 100`
  - `singleQuote: true`
  - HTML files use Angular parser
- **No ESLint** detected

### TypeScript Compiler
- Strict mode enabled in `web/frontend/tsconfig.json`
- `noImplicitOverride: true`
- `noImplicitReturns: true`
- `noFallthroughCasesInSwitch: true`
- Angular strict templates enabled

## Import Organization

### Scala
**Order (informal, not enforced):**
1. `com.*` - Third-party libraries (Jackson, typesafe-logging)
2. `edu.ucdavis.*` - Project internal imports
3. `org.springframework.*` - Spring framework
4. `java.*` / `scala.*` - Standard library

Use wildcard imports for project types: `import edu.ucdavis.fiehnlab.ctsrest.client.types._`

### TypeScript
**Order:**
1. Angular core (`@angular/core`, `@angular/forms`, etc.)
2. Angular Material (`@angular/material/*`)
3. Third-party (`rxjs`)
4. Project-local relative imports (`../../services/translation.service`)

No path aliases configured.

## Dependency Injection

### Scala (Spring)
- Use `@Autowired val` with `null` initialization (Scala+Spring idiom):
  ```scala
  @Autowired
  val client: CtsService = null
  ```
- Use `@Value` annotation for configuration properties:
  ```scala
  @Value("${cts.old.url:https://oldcts.fiehnlab.ucdavis.edu/service}")
  val baseUrl = ""
  ```
- Beans defined via `@Configuration` classes with `@Bean` methods
- Component scanning via `@SpringBootApplication` on `CtsProxy` class

### TypeScript (Angular)
- Use constructor injection with `private` modifier:
  ```typescript
  constructor(
    private translation: TranslationService,
    private downloadService: DownloadService
  ) {}
  ```
- Services use `@Injectable({ providedIn: 'root' })` for singleton registration
- Components use standalone `imports` array (no NgModule)

## Component Architecture (Angular)

**Standalone components only** - no NgModules. Each component declares its dependencies via `imports` array:
```typescript
@Component({
  selector: 'app-single-conversion',
  imports: [FormsModule, MatFormFieldModule, MatSelectModule, ...],
  templateUrl: './single-conversion.component.html',
  styleUrl: './single-conversion.component.scss',
})
```

**State management:** Use Angular signals (`signal()`, `input()`, `input.required()`) for reactive state:
```typescript
fromValues = signal<string[]>([]);
loading = signal(false);
results = signal<ResultsMap | null>(null);
```

**Async operations:** Use `async/await` with `firstValueFrom()` to convert observables to promises:
```typescript
const data = await firstValueFrom(this.http.get<string[]>('/rest/toValues'));
```

## Error Handling

### Scala Backend
**Controller-level:** Throw `ResponseStatusException` for client errors:
```scala
throw new ResponseStatusException(HttpStatus.BAD_REQUEST, s"Conversion from '$from' is not supported")
```

**Service-level:** Use try/catch with fallback behavior:
```scala
try {
  Some(smilesConversionService.inchikeyToSmiles(inchikey))
} catch {
  case e: Exception =>
    logger.warn(s"CDK conversion failed for InChIKey $inchikey: ${e.getMessage}")
    None
}
```

**Proxy controller:** Catch `HttpStatusCodeException` and forward upstream error responses:
```scala
catch {
  case ex: HttpStatusCodeException =>
    new ResponseEntity[Array[Byte]](ex.getResponseBodyAsByteArray, ex.getResponseHeaders, ex.getStatusCode)
}
```

**Null checks:** Explicit null checks for external API responses:
```scala
if (molResponse == null || molResponse.molecule == null || molResponse.molecule.trim.isEmpty) {
  throw new IllegalArgumentException(s"No MOL block found for InChIKey: $inchikey")
}
```

### TypeScript Frontend
**Component-level:** Catch in try/catch, accumulate errors in signal:
```typescript
catch (err: any) {
  this.errors.update((e) => [...e, err.message || String(err)]);
}
```

## Logging

### Scala
**Framework:** `com.typesafe.scala-logging.LazyLogging` trait
- Mix in `LazyLogging` to get a `logger` field
- Use string interpolation: `logger.info(s"RESPONSE: ${response}")`
- Use `logger.debug` for routine operations, `logger.warn` for fallback paths
- Logback configuration at `web/src/main/resources/logback.xml`

### TypeScript
- No structured logging framework
- Occasional `console.log`/`println` in tests only

## Caching

Use Spring `@Cacheable` annotation on controller and service methods:
```scala
@Cacheable(Array("simple_convert"))
@GetMapping(path = Array("/convert/{from}/{to}/{searchTerm}"))
def convertSimple(...): Seq[ConversionResult] = { ... }
```

Cache configuration in `web/src/main/resources/application.yml`:
- Backend: Caffeine
- TTL: 30 seconds (`expireAfterWrite=30s`)
- Named caches for each endpoint

## REST API Conventions

**Base path:** `/rest` for proxied CTS API, `/service` for raw reverse proxy
**HTTP methods:**
- `@GetMapping` for reads (conversions, lookups)
- `@PostMapping` with `consumes = Array("application/json")` for structural conversions (MOL, InChI)

**CORS:** Wide-open `@CrossOrigin(origins = Array("*"))` on controllers, plus global CORS via `WebMvcConfigurer`

**Content negotiation:** Default to `APPLICATION_JSON`, ignore Accept header for non-JSON

## Comments

**When to comment:**
- Section dividers for GET vs POST: `/* ---------------------------POST REQUESTS-------------------------------*/`
- Explain non-obvious workarounds: `// Add MOL header since the old CTS doesn't recognize it as valid otherwise`
- Creation attribution (legacy): `// Created by diego on 2/15/2017`

**ScalaDoc:** Minimal. No systematic documentation of public APIs.

## Module Design

**Trait-based abstraction:** Define service interface as a Scala trait, implement in concrete class:
- `CtsService` trait in `ctsclient/.../api/CtsService.scala`
- `CtsClient` implementation in `ctsclient/.../core/CtsClient.scala`

**Single-file types:** All shared DTOs in one file: `ctsclient/.../types/Types.scala`

**Spring auto-configuration:** `casetojson` module uses `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` for auto-discovery

---

*Convention analysis: 2026-03-09*
