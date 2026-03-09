# Testing Patterns

**Analysis Date:** 2026-03-09

## Test Frameworks

### Scala Backend

**Runner:**
- ScalaTest 3.2.19
- ScalaTest Maven Plugin 2.2.0 (replaces Surefire for Scala tests)
- Config: `pom.xml` parent profile `scala-test` (auto-activates when `src/test/scala` exists)

**Assertion Library:**
- ScalaTest `Matchers` trait (should-style assertions)

**Spring Integration:**
- `@SpringBootTest` for integration tests
- `TestContextManager` for manual Spring context initialization in ScalaTest

**Run Commands:**
```bash
mvn test                    # Run all tests across all modules
mvn test -pl ctsclient      # Run tests in ctsclient module only
mvn test -pl web            # Run tests in web module only
mvn test -pl casetojson     # Run tests in casetojson module only
```

### TypeScript Frontend

**Runner:**
- Vitest 4.0.8 (listed in devDependencies)
- jsdom 28.0.0 (browser environment for tests)

**Run Commands:**
```bash
cd web/frontend && npm test   # Run frontend tests (ng test)
```

**Current State:** Vitest is configured as a dependency but no application-level `.spec.ts` test files exist in the frontend `src/` directory. Frontend is effectively untested.

## Test File Organization

### Scala

**Location:** Mirrored `src/test/scala` directory structure matching `src/main/scala`

**Naming:** `{ClassName}Test.scala` - append `Test` to the class under test

**Directory mapping:**
```
ctsclient/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/client/core/CtsClientTest.scala
web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/controllers/CtsControllerTest.scala
web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/CtsProxyForwardingTest.scala
web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/SmilesConversionServiceTest.scala
web/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/web/services/CdkSmilesServiceTest.scala
casetojson/src/test/scala/edu/ucdavis/fiehnlab/ctsrest/casetojson/config/CaseClassToJSONSerializationAutoConfigurationTest.scala
```

### TypeScript

**Expected location:** Co-located `*.spec.ts` files next to source files (Angular convention)

**Current state:** No frontend test files exist.

## Test Structure

### ScalaTest WordSpec Pattern (Primary)

Most tests use `AnyWordSpec` with `Matchers`:

```scala
@SpringBootTest(value = Array("${fiehnlab.cts.config.name}"), classes = Array(classOf[CtsProxy]), webEnvironment = WebEnvironment.RANDOM_PORT)
class CtsControllerTest extends AnyWordSpec with Matchers with LazyLogging {

  @Autowired
  val restTemplate: TestRestTemplate = null

  @LocalServerPort
  val port: Int = 0

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val baseUrl = s"http://localhost:${port}/rest"

  "CtsController" should {

    "fromValues" in {
      val response = restTemplate.getForObject[Seq[String]](baseUrl + "/fromValues", classOf[Seq[String]])
      response should not be empty
      response.size should be > 10
    }

    "testSecondCallHitsCache" ignore {
      // tests can be marked as ignored
    }
  }
}
```

### ScalaTest FunSuite Pattern (Secondary)

Used in `casetojson` module:

```scala
@SpringBootTest
class CaseClassToJSONSerializationAutoConfigurationTest extends AnyFunSuite {

  @Autowired
  val objectMapper: ObjectMapper = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  test("testObjectMapper") {
    assert(objectMapper != null)
    // ...
    assert(in.name == "tada")
  }
}
```

### Unit Test Pattern (No Spring Context)

For classes with no Spring dependencies, instantiate directly:

```scala
class CdkSmilesServiceTest extends AnyWordSpec with Matchers {

  val service = new CdkSmilesService()

  "CdkSmilesService" should {

    "convert ethanol MOL block to SMILES" in {
      val smiles = service.molToSmiles(ethanolMol)
      smiles should not be empty
      smiles should be("OCC")
    }

    "throw exception for invalid MOL block" in {
      an[Exception] should be thrownBy {
        service.molToSmiles("not a valid mol block")
      }
    }
  }
}
```

## Spring Test Context Initialization

**Critical pattern:** ScalaTest does not natively integrate with Spring's `@SpringBootTest`. Every test class must manually initialize the Spring context:

```scala
new TestContextManager(this.getClass).prepareTestInstance(this)
```

This line must appear at the class body level (not inside a test method) to inject `@Autowired` fields before tests run.

## Test Configuration

### Integration Tests (SpringBootTest)

**Web module tests** boot a full Spring context with a random port:
```scala
@SpringBootTest(value = Array("${fiehnlab.cts.config.name}"), classes = Array(classOf[CtsProxy]), webEnvironment = WebEnvironment.RANDOM_PORT)
```

**Client module tests** boot a minimal context with specific config classes:
```scala
@SpringBootTest(classes = Array(classOf[CtsClientTestConfig], classOf[CaseClassToJSONSerializationAutoConfiguration]))
```

**Test-specific Spring configurations** are defined inline in the test file:
```scala
@Configuration
class CtsClientTestConfig {
  @Bean
  def client: CtsClient = new CtsClient
}
```

### Test Properties

Application properties from `web/src/main/resources/application.yml` are used for tests. The `cts.old.url` defaults to `https://oldcts.fiehnlab.ucdavis.edu/service` via `@Value` annotation default.

## Mocking

**Framework:** No mocking framework is used.

**Approach:** All backend tests are **integration tests** that make real HTTP calls to the external CTS API (`oldcts.fiehnlab.ucdavis.edu`). There are no mocked services, no WireMock, no MockMvc.

**What this means:**
- Tests require network access to the external CTS server
- Tests are slow (each makes multiple HTTP round-trips)
- Tests are fragile (depend on external service availability and data)
- Test data uses real chemical identifiers (e.g., alanine `QNAYBMKLOCPYGJ-REOHCLBHSA-N`, ethanol `LFQSCWFLJHTTHZ-UHFFFAOYSA-N`)

## Assertion Patterns

### ScalaTest Matchers (should-style)

**Equality:**
```scala
response shouldEqual FormulaResponse("C2H6O", "CCHHHHHHO", null)
response.inchicode shouldEqual "InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3"
```

**Collection containment:**
```scala
response should not be empty
response should contain allOf ("PubChem CID", "InChIKey", "Chemical Name")
response.size should be > 10
```

**Property matching:**
```scala
client.expandFormula("H2O") should have (
  Symbol("formula") ("H2O"),
  Symbol("result") ("HHO")
)
```

**Exception testing:**
```scala
an[Exception] should be thrownBy {
  service.molToSmiles("not a valid mol block")
}
```

**NOTE:** Some tests use `===` operator which does NOT assert in ScalaTest (it returns a Boolean). These are silent non-assertions:
```scala
response.head.fromIdentifier === "chemical name"   // This does NOT fail if wrong!
response.head.searchTerm === "alanine"              // Use shouldEqual or should equal instead
```
Files affected: `ctsclient/src/test/scala/.../CtsClientTest.scala` (lines 56-58, 77, 88-92)

### ScalaTest FunSuite (assert-style)

```scala
assert(objectMapper != null)
assert(in.name == "tada")
```

## Test Data

**Chemical test fixtures:** Hardcoded inline in test classes using real chemical data:
- Ethanol InChIKey: `LFQSCWFLJHTTHZ-UHFFFAOYSA-N`
- L-alanine InChIKey: `QNAYBMKLOCPYGJ-REOHCLBHSA-N`
- Ethanol MOL block: Inline multi-line string
- Ethanol InChI code: `InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3`

No shared fixture files or factory methods exist.

## Coverage

**Requirements:** None enforced. No coverage thresholds configured.

**Coverage tools:** None configured (no JaCoCo, no scoverage plugin).

## Test Types

### Unit Tests
- `CdkSmilesServiceTest` - Tests CDK SMILES conversion without Spring context
- `CaseClassToJSONSerializationAutoConfigurationTest` - Tests Jackson serialization config

### Integration Tests (with real external calls)
- `CtsClientTest` - Tests all CTS API client methods against live server
- `CtsControllerTest` - Tests REST controller endpoints via `TestRestTemplate`
- `CtsProxyForwardingTest` - Tests reverse proxy forwarding to live CTS backend
- `SmilesConversionServiceTest` - Tests SMILES conversion pipeline against live server

### Frontend Tests
- None exist

## Test Inventory

| Module | Test File | Type | External Calls |
|--------|-----------|------|----------------|
| `casetojson` | `CaseClassToJSONSerializationAutoConfigurationTest.scala` | Unit/Spring | No |
| `ctsclient` | `CtsClientTest.scala` | Integration | Yes (CTS API) |
| `web` | `CtsControllerTest.scala` | Integration | Yes (CTS API) |
| `web` | `CtsProxyForwardingTest.scala` | Integration | Yes (CTS API) |
| `web` | `SmilesConversionServiceTest.scala` | Integration | Yes (CTS API) |
| `web` | `CdkSmilesServiceTest.scala` | Unit | No |
| `frontend` | (none) | N/A | N/A |

## Adding New Tests

### New Scala Unit Test (no Spring)
Place in mirrored test directory, extend `AnyWordSpec with Matchers`:
```scala
package edu.ucdavis.fiehnlab.ctsrest.web.services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MyNewServiceTest extends AnyWordSpec with Matchers {
  val service = new MyNewService()

  "MyNewService" should {
    "do something" in {
      service.doSomething() shouldEqual "expected"
    }
  }
}
```

### New Scala Integration Test (with Spring)
```scala
package edu.ucdavis.fiehnlab.ctsrest.web.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestContextManager

@SpringBootTest(value = Array("${fiehnlab.cts.config.name}"), classes = Array(classOf[CtsProxy]), webEnvironment = WebEnvironment.RANDOM_PORT)
class MyNewControllerTest extends AnyWordSpec with Matchers {

  @Autowired
  val restTemplate: TestRestTemplate = null

  @LocalServerPort
  val port: Int = 0

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MyNewController" should {
    "handle request" in {
      // test implementation
    }
  }
}
```

---

*Testing analysis: 2026-03-09*
