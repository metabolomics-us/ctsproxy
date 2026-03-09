# Codebase Concerns

**Analysis Date:** 2026-03-09

## Tech Debt

**Deprecated Docker Plugin:**
- Issue: The `com.spotify:docker-maven-plugin:0.4.13` is long-abandoned (last release 2017). Spotify themselves recommend migrating to `com.google.cloud.tools:jib-maven-plugin` or `io.fabric8:docker-maven-plugin`.
- Files: `pom.xml` (lines 244-312)
- Impact: Plugin may break with newer JDK or Docker versions; no security patches.
- Fix approach: Replace with `jib-maven-plugin` or `io.fabric8:docker-maven-plugin`.

**Deprecated maven-antrun-plugin for wait-for-it Download:**
- Issue: Downloads `wait-for-it.sh` from GitHub at compile time via `maven-antrun-plugin:1.7`. This is fragile (depends on external URL availability) and the antrun plugin version is old.
- Files: `pom.xml` (lines 216-232)
- Impact: Build fails if GitHub is unreachable. The raw GitHub URL could change or be rate-limited.
- Fix approach: Vendor `wait-for-it.sh` into the repository or use a Docker healthcheck instead.

**Null-initialized @Autowired Fields (Scala Anti-pattern):**
- Issue: All `@Autowired` fields are declared as `val fieldName: Type = null`. This is a Scala anti-pattern that defeats null-safety and makes the code harder to reason about. Any access before Spring injection produces a NullPointerException with no clear error.
- Files:
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala` (lines 24, 27)
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ProxyController.scala` (lines 17, 21)
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/SmilesConversionService.scala` (lines 13, 16)
  - `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClient.scala` (lines 16-19)
- Impact: Potential runtime NPE; code is harder to test in isolation.
- Fix approach: Use constructor injection instead. Spring Boot supports Scala constructor injection with `@Autowired` on the constructor or primary constructor parameters.

**Commented-out Code:**
- Issue: Dead commented-out endpoint `extidCount` remains in `CtsController`.
- Files: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala` (lines 128-132)
- Impact: Clutters the codebase, confuses developers about intended functionality.
- Fix approach: Remove the commented code; it is preserved in git history if needed.

**Duplicated filterIllegal Logic in Frontend:**
- Issue: The `filterIllegal` method is identically implemented in both `SingleConversionComponent` and `BatchConversionComponent`.
- Files:
  - `web/frontend/src/app/components/single-conversion/single-conversion.component.ts` (lines 115-120)
  - `web/frontend/src/app/components/batch-conversion/batch-conversion.component.ts` (lines 166-172)
- Impact: Changes to filtering logic must be made in two places; risk of drift.
- Fix approach: Move `filterIllegal` into `TranslationService` or a shared utility.

**Duplicated Download UI in Frontend:**
- Issue: The download options card (export style, export type, top hit checkbox, download button) is copy-pasted identically in both single and batch conversion templates.
- Files:
  - `web/frontend/src/app/components/single-conversion/single-conversion.component.html` (lines 78-101)
  - `web/frontend/src/app/components/batch-conversion/batch-conversion.component.html` (lines 111-134)
- Impact: UI changes must be synchronized across two templates.
- Fix approach: Extract a shared `DownloadOptionsComponent`.

**CSV Export Does Not Respect TSV Delimiter:**
- Issue: `DownloadService` always uses commas as delimiters regardless of whether `exportType` is `'csv'` or `'tsv'`. The `type` parameter only affects the file extension and MIME type.
- Files: `web/frontend/src/app/services/download.service.ts` (lines 44-104)
- Impact: TSV downloads contain comma-separated values, which is incorrect.
- Fix approach: Use `\t` as delimiter when `type === 'tsv'`.

## Security Considerations

**Wildcard CORS Configuration:**
- Risk: CORS is configured with `origins = Array("*")` on both `CtsController` and `ViewController`, and also globally via `CtsCors.addCorsMappings`. This allows any website to make API requests to the CTS proxy.
- Files:
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/CtsProxy.scala` (line 46)
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala` (line 19)
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ViewController.scala` (line 7)
- Current mitigation: This is likely intentional for a public scientific API.
- Recommendations: If the API is intended to be public, this is acceptable. If not, restrict to known origins.

**No Rate Limiting:**
- Risk: No rate limiting on any endpoint. The proxy forwards requests to the upstream CTS service without throttling.
- Files: All controllers in `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/`
- Current mitigation: Caching via `@Cacheable` reduces repeated calls for the same data.
- Recommendations: Add rate limiting (e.g., Spring Boot `bucket4j` or a reverse proxy like nginx) to protect the upstream CTS service.

**No Input Validation on Path Variables:**
- Risk: Path variables like `from`, `to`, `searchTerm`, `inchikey`, and `formula` are passed directly to the upstream CTS API without sanitization or length limits.
- Files:
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala` (all endpoints)
  - `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClient.scala` (all methods)
- Current mitigation: Spring's URL encoding provides basic protection.
- Recommendations: Add validation for expected formats (e.g., InChIKey regex `^[A-Z]{14}-[A-Z]{10}-[A-Z]$`).

**Proxy Controller Forwards All Headers:**
- Risk: `ProxyController` forwards all request headers (except `Host`) to the upstream CTS backend, potentially leaking auth tokens or internal headers.
- Files: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ProxyController.scala` (lines 33-42)
- Current mitigation: `Host` header is removed.
- Recommendations: Allowlist specific headers to forward rather than forwarding all.

## Performance Bottlenecks

**Sequential Batch Conversions:**
- Problem: Batch conversion in the frontend sends requests sequentially (one at a time) in a nested loop over search terms and target types.
- Files: `web/frontend/src/app/components/batch-conversion/batch-conversion.component.ts` (lines 126-143)
- Cause: Each conversion awaits completion before starting the next. For 100 terms with 3 target types = 300 sequential HTTP requests.
- Improvement path: Send requests in parallel with a concurrency limit (e.g., 5-10 concurrent requests) using `Promise.all` with batching.

**Unbounded Cache Growth:**
- Problem: Caching is enabled via `@EnableCaching` with Caffeine, but no cache configuration (max size, TTL, eviction policy) is defined anywhere.
- Files:
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/CtsProxy.scala` (line 18, `@EnableCaching`)
  - `web/pom.xml` (Caffeine dependency, line 29)
- Cause: Without explicit cache configuration, Caffeine uses defaults which may not bound memory.
- Improvement path: Add `spring.cache.caffeine.spec` in `application.properties` or create a `CacheManager` bean with explicit `maximumSize` and `expireAfterWrite`.

**Duplicate Cache Key for inchikey_to_smiles:**
- Problem: The cache name `"inchikey_to_smiles"` is used on both `SmilesConversionService.inchikeyToSmiles()` and `CtsController.inchikeyToSmiles()`. The controller method wraps the service method, creating redundant caching.
- Files:
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala` (line 79)
  - `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/SmilesConversionService.scala` (line 18)
- Cause: Same cache name on nested calls means the outer cache stores the wrapped result while the inner cache stores the raw result.
- Improvement path: Remove the `@Cacheable` annotation from the controller method since the service already caches.

## Fragile Areas

**ProxyController Exception Handling:**
- Files: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ProxyController.scala` (lines 47-67)
- Why fragile: Only catches `HttpStatusCodeException`. Other exceptions (connection refused, timeout, DNS failure) will produce unhandled 500 errors with stack traces exposed to the client.
- Safe modification: Add a catch-all for `Exception` that returns a clean 502 Bad Gateway response.
- Test coverage: `CtsProxyForwardingTest` only tests happy-path forwarding; no error scenario tests.

**CtsClient Manual JSON Deserialization:**
- Files: `ctsclient/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClient.scala` (lines 21-33)
- Why fragile: The `convert` method manually destructures `Seq[Map[String, Any]]` and casts `item("result").asInstanceOf[Seq[String]]`. If the upstream API changes its response shape, this throws a `ClassCastException` at runtime with no helpful error message.
- Safe modification: Use proper typed deserialization with case classes, or add defensive checks.
- Test coverage: Tests exist but rely on live external API responses.

**SMILES Conversion Fallback Chain:**
- Files: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsController.scala` (lines 45-77)
- Why fragile: `convertToSmiles` has a multi-step chain (from -> InChIKey -> MOL -> SMILES via CDK) with a fallback to the old CTS API. Failures at any step are silently caught and fall back. This makes debugging conversion failures very difficult.
- Safe modification: Add structured logging with the full chain state at each fallback point.
- Test coverage: No unit test for the `convertToSmiles` method specifically; integration tests depend on live API.

**ViewController Hard-coded Route List:**
- Files: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ViewController.scala` (line 13)
- Why fragile: Routes `/`, `/batch`, `/services` are hard-coded. Adding a new Angular route requires updating this Scala file as well.
- Safe modification: Use a catch-all pattern like `/{path:[^.]*}` that forwards all non-file requests to `index.html`.
- Test coverage: No tests for the ViewController.

## Test Coverage Gaps

**Zero Frontend Tests:**
- What's not tested: The entire Angular frontend has no test files (`.spec.ts` or `.test.ts`).
- Files: All files in `web/frontend/src/app/`
- Risk: Regressions in conversion logic, download formatting, UI interactions, and input validation go undetected.
- Priority: High - especially for `TranslationService` and `DownloadService` which contain business logic.

**Backend Tests Depend on Live External API:**
- What's not tested: All backend tests (`CtsControllerTest`, `CtsProxyForwardingTest`, `CtsClientTest`, `SmilesConversionServiceTest`) make real HTTP calls to `https://oldcts.fiehnlab.ucdavis.edu` and the configured proxy target.
- Files:
  - `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsControllerTest.scala`
  - `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/CtsProxyForwardingTest.scala`
  - `ctsclient/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClientTest.scala`
  - `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/SmilesConversionServiceTest.scala`
- Risk: Tests fail when the external service is down, slow, or returns different data. Tests cannot run offline or in CI without network access.
- Priority: High - these are integration tests masquerading as unit tests. Add mock-based unit tests for core logic.

**Cache Test is Ignored:**
- What's not tested: The cache verification test in `CtsControllerTest` is marked as `ignore`.
- Files: `web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsControllerTest.scala` (line 69)
- Risk: Cache behavior is completely untested; cache configuration bugs would not be caught.
- Priority: Medium.

**No Tests for ProxyController Error Handling:**
- What's not tested: Error scenarios in the reverse proxy (upstream down, timeout, malformed responses).
- Files: `web/src/main/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/ProxyController.scala`
- Risk: Proxy silently fails or exposes internal errors.
- Priority: Medium.

**No Tests for DownloadService Export Logic:**
- What's not tested: CSV/TSV generation, table vs list format, top-hit filtering.
- Files: `web/frontend/src/app/services/download.service.ts`
- Risk: The TSV delimiter bug (see Tech Debt) went undetected because there are no tests.
- Priority: High.

## Dependencies at Risk

**Bootstrap Included but Unused by Angular Material:**
- Risk: `bootstrap@~5.3.0` is listed as a dependency in `web/frontend/package.json` but the frontend uses Angular Material exclusively for UI components. Bootstrap CSS may conflict with Material styles.
- Files: `web/frontend/package.json` (line 24)
- Impact: Unnecessary bundle size; potential CSS conflicts.
- Migration plan: Remove Bootstrap unless specific utility classes are used; check stylesheets for Bootstrap class references.

**Angular 21 (Pre-release/Cutting Edge):**
- Risk: Angular `^21.2.0` is a very recent major version. Ecosystem libraries and tooling may have compatibility gaps.
- Files: `web/frontend/package.json`
- Impact: Potential breaking changes in minor updates; community support for issues may be limited.
- Migration plan: Monitor Angular changelog; pin exact versions in `package.json` if stability is a concern.

## Missing Critical Features

**No application.properties/application.yml:**
- Problem: No Spring Boot configuration file is checked into the repository. All configuration relies on defaults and `@Value` annotations with fallback defaults (e.g., `cts.proxy.url:http://cts`, `cts.old.url:https://oldcts.fiehnlab.ucdavis.edu/service`).
- Files: No `application.properties` or `application.yml` found in `web/src/main/resources/` (only `logback.xml` exists)
- Blocks: New developers cannot easily discover what configuration properties are available or required. Cache TTL, server port, and other Spring Boot settings cannot be tuned without code changes.

**No Health Check Endpoint:**
- Problem: Although `spring-boot-starter-actuator` is included in `ctsclient/pom.xml`, it is not included in the `web` module's `pom.xml`. The web application has no health or readiness endpoint.
- Files: `web/pom.xml`, `ctsclient/pom.xml` (line 36)
- Blocks: Container orchestration (Docker, Kubernetes) cannot determine if the application is healthy or if the upstream CTS service is reachable.

---

*Concerns audit: 2026-03-09*
