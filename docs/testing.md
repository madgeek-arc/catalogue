# Catalogue — Testing

| Class | Test cases | Covers |
|---|---|---|
| `service/ModelResponseValidatorTest` | 30 (in `@Nested` classes, so surefire's summary line for the outer class shows 0) | Field-type validation (phone, email, patterns, numbers, dates, vocabularies, nested sections), integration of the validator with test model and resource JSON |
| `ui/UiElementsTest` | 1 | UI model construction |

Fixtures: `src/test/resources/test-model.json`, `test-resource.json`. `application.yml` is empty.

## Running

```bash
mvn test
mvn test -Dtest='ModelResponseValidatorTest*'
```

Result at `1451f78`: 31 test cases, 0 failures. No external services are needed.

## Not covered

Controllers, `GenericResourceManager` against a registry, `DefaultDataRequestService`, `LogsController`.
There's no coverage tool and no CI configuration in the repository.
