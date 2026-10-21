# Catalogue — Integrations

| Direction | Counterpart | Protocol | Purpose | Configuration key | Evidence path/symbol |
|---|---|---|---|---|---|
| consumed | registry (service beans in the same application) | Java: `ResourceService`, `ResourceTypeService`, `SearchService`, `ParserService` | Persist and search models and items | the host's `registry.*` | `config/CatalogueConfiguration#modelService`, `service/GenericResourceManager` |
| provided | registry (resource-type initialisation) | Classpath resource `resourceTypes/model.json` | Registers the `model` type | `registry.resource-type-init.location` (registry side) | `src/main/resources/resourceTypes/model.json` |
| provided | Frontends / HTTP clients via the host | REST JSON | Models, items, vocabularies, data requests, logs | host `server.*` | `controller/*` |
| consumed | Vocabulary and URL endpoints referenced in models | HTTP GET (`WebClient`, 5 s response timeout) | Check vocabulary values and reachable URLs | `catalogue-lib.validation.base-url`, `.enabled` | `service/ModelResponseValidator` |
| consumed | Any endpoint given in a `DataRequest` | HTTP, any method, caller-supplied headers and body | Fetch remote data and extract values with JSONPath/XPath | — | `service/DefaultDataRequestService#retrieve` |
| provided | Frontend build | `target/catalogue-model.d.ts` | Shared TypeScript model types | — | `pom.xml` typescript-generator |

## Version alignment

Catalogue compiles against `registry-core` **4.2.0**. A host that embeds a different registry
version (for example a newer `registry-starter-service`) gets Maven's nearest-wins resolution, so
catalogue runs against the host's registry version. Check compatibility for each registry release a
host moves to.

## Failure behaviour

| Situation | Behaviour | Evidence |
|---|---|---|
| No model for the resource type | Validation skipped with `WARN` | `ModelResponseValidator#validate` |
| Several models for the resource type | `409 CONFLICT` | same |
| Validation errors | `ValidationException` → JSON error (`GenericExceptionController`) | `GenericExceptionController` |
| Vocabulary/URL endpoint unreachable | `WebClientRequestException`/`WebClientResponseException` handled in the validator, reported as a validation failure for the field | `ModelResponseValidator` |
| Data request target returns 400/500 | Mapped to `ResourceException(400)`. Other statuses propagate | `DefaultDataRequestService#retrieve` |
| Data request timeouts | None set on the per-request `WebClient` | same |
