# Catalogue — Configuration

Properties are bound by `src/main/java/gr/uoa/di/madgik/catalogue/config/CatalogueLibProperties.java`
(`@ConfigurationProperties("catalogue-lib")`, `@Validated`) and set in the host application. The jar
publishes their metadata, so IDEs show the descriptions.

| Property | Required | Default | Description |
|---|---|---|---|
| `catalogue-lib.model.validation.enabled` | No | `true` | Validate created and updated resources against their model |
| `catalogue-lib.model.validation.base-url` | **Yes** when validation is enabled | — | Base URL for relative vocabulary lookups. Startup fails if it's missing |
| `catalogue-lib.model.validation.allow-private-addresses` | No | `false` | Let URL field values point to loopback, private, link-local and other non-public addresses. Vocabulary lookups aren't affected |
| `catalogue-lib.model.resource-type-sync.on-create` | No | `false` | Create the derived registry resource type when a model is created |
| `catalogue-lib.model.resource-type-sync.on-update` | No | `false` | Update the derived registry resource type, in the background, when a model is updated |
| `catalogue-lib.data-request.allow-private-addresses` | No | `false` | Let `POST /request/default` target non-public addresses |
| `catalogue-lib.data-request.connect-timeout` | No | `10s` | Connect timeout of data requests |
| `catalogue-lib.data-request.response-timeout` | No | `30s` | Response timeout of data requests |

## Registry settings

Catalogue reads no `registry.*` keys itself. Settings that affect it:

- `registry.resource-type-init.location` (default `classpath*:resourceTypes`) loads the bundled
  `model` resource type.
- `registry.jaxb.include-packages` builds the `JAXBContext` used for XML payloads.
- `spring.flyway.enabled` runs catalogue's repeatable migration on the registry database.

## Example (placeholders only)

```properties
catalogue-lib.model.validation.base-url=https://<host>/<context-path>
catalogue-lib.model.resource-type-sync.on-create=true
catalogue-lib.model.resource-type-sync.on-update=true
```

## Environments

- Tests: `src/test/resources/application.yml` is empty. Tests construct objects directly.
- Hosts: values are set per deployment through environment variables or an external configuration.
  They aren't stored in the repository.

## Secrets

None. Vocabulary endpoints are called without credentials. (`ModelResponseValidator`)
