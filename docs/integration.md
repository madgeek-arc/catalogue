# Catalogue — Integrations

| Direction | Counterpart | Protocol | Purpose | Configuration key | Evidence path/symbol |
|---|---|---|---|---|---|
| consumed | registry 5.0.0 (service beans in the same application) | Java: `GenericResourceService`, `ResourceTypeService`, `SearchService`, `ResourceService`, `ParserService` | Persist and search models, create and update resource types | the host's `registry.*` | `config/CatalogueConfiguration#modelService`, `service/DefaultModelService` |
| provided | registry (`ResourceValidator`) | Java SPI | Validate resources against their model on create/update | `catalogue-lib.model.validation.*` | `service/ModelResponseValidator` |
| provided | registry (resource-type initialisation) | Classpath resource `resourceTypes/model.json` | Registers the `model` type | `registry.resource-type-init.location` (registry side, default `classpath*:resourceTypes`) | `src/main/resources/resourceTypes/model.json` |
| provided | registry database | Flyway repeatable migration | Rewrites resource-type `class` properties from the 9.x package names | the registry's Flyway | `src/main/resources/db/migration/R__Catalogue_Migrate_model_package_in_resource_type_class.sql` |
| provided | Frontends / HTTP clients via the host | REST JSON | Models, resource types, deprecated items, data requests | host `server.*` | `controller/*` |
| consumed | Vocabulary endpoints referenced in models | HTTP GET (`WebClient`, 10 s connect and response timeouts) | Check vocabulary values | `catalogue-lib.model.validation.base-url`, `.enabled` | `service/ModelResponseValidator#validateVocabulary` |
| consumed | URLs submitted in URL fields | HTTP HEAD, then GET when HEAD is refused (10 s timeouts). Public addresses only, redirects followed only to public addresses | Check that submitted URLs respond | `catalogue-lib.model.validation.allow-private-addresses` | `service/ModelResponseValidator#validateUrl`, `http/PublicAddressGuard` |
| consumed | Endpoint given in a `DataRequest` | HTTP, any method, caller-supplied headers and body. Public addresses only, redirects not followed | Fetch remote data and extract values with JSONPath/XPath | `catalogue-lib.data-request.*` | `service/DefaultDataRequestService#retrieve` |
| provided | Frontend build | `target/catalogue-model.d.ts` | Shared TypeScript model types | — | `pom.xml` typescript-generator |

## Failure behaviour

| Situation | Behaviour | Evidence |
|---|---|---|
| No model for the resource type | Validation skipped with `WARN` | `ModelResponseValidator#validate` |
| Several models for the resource type | Validation skipped with `WARN` | same |
| Validation errors | `ValidationException` → 400 problem detail | `GenericExceptionController` |
| URL field value points to a non-public address | Validation error ("is not allowed"), no request sent | `ModelResponseValidator#validateUrl`, `ModelResponseValidatorTest$UrlAddressGuard` |
| Vocabulary or URL endpoint unreachable | Reported as a validation failure for the field | `ModelResponseValidator` |
| Data request to a non-public address or unresolvable host | 400, no request sent | `DefaultDataRequestService`, `DefaultDataRequestServiceTest` |
| Data request target returns 400/500 | Mapped to `ResourceException(400)`. Other statuses propagate | `DefaultDataRequestService#retrieve` |
| Data request target slow | Fails after `catalogue-lib.data-request.connect-timeout` (10 s) or `response-timeout` (30 s) | `DefaultDataRequestServiceTest` |
| Automatic resource-type update after a model update fails | Logged at `ERROR`. The model update stands | `DefaultModelService#updateMappedResourceTypeAsync` |
