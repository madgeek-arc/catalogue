# Catalogue — Testing

| Class | Test cases | Covers |
|---|---|---|
| `service/ModelResponseValidatorTest` | 33 (in `@Nested` classes, so surefire's summary line for the outer class shows 0) | Field-type validation (phone, email, patterns, numbers, dates, vocabularies, nested sections), an integration run against the test model and resource JSON, `dependsOn` lookups using the model of the current validation, URL fields pointing to non-public addresses |
| `service/ModelResourceTypeMapperTest` | 31 | Deriving registry resource types and index fields from models |
| `service/DefaultDataRequestServiceTest` | 5 | Non-public targets refused before sending, parameter expansion and JSONPath extraction against a local server, no redirect following, response timeout |
| `http/PublicAddressGuardTest` | 28 | Address classification, blocked schemes and hosts, opt-out |
| `UiElementsTest` | 1 | UI model construction |
| `config/CatalogueConfigurationTest` | 10 | Auto-configuration: beans back off to host beans, no `ObjectMapper` contributed, host keeps `applicationTaskExecutor`, sync executor settings, web beans only in servlet applications and switchable, host exception-advice subclass replaces the library's |
| `service/DefaultModelServiceTest` | 2 | Facets that are not indexed are rejected; a model update returns before its resource type update runs |
| `service/SnippetTemplateCacheTest` | 9 | Snippet template caching and eviction on model writes |
| `domain/SnippetTemplateTest` | 4 | Snippet template rendering |

Fixtures: `src/test/resources/test-model.json`, `test-resource.json`. `application.yml` is empty.
HTTP calls in the validator tests are mocked.

## Running

```bash
mvn test
mvn test -Dtest='ModelResponseValidatorTest*'
```

Result on `version/10.0.0`: 123 tests, 0 failures. No external services are needed.

## Not covered

Controller request handling, the validator inside a running registry, the resource-type sync against a real registry, the Flyway
migration, and redirect handling of URL validation towards non-public addresses (it needs a public
host to start from). There's no coverage tool and no CI configuration in the repository.
