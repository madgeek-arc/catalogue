# Catalogue — Configuration

Properties are bound by `src/main/java/gr/uoa/di/madgik/catalogue/config/CatalogueLibProperties.java`
(`@ConfigurationProperties("catalogue-lib")`) and set in the host application.

| Property | Required | Default | Description |
|---|---|---|---|
| `catalogue-lib.validation.enabled` | No | `true` | Validate created and updated items against their model |
| `catalogue-lib.validation.base-url` | **Yes** when validation is enabled | — | Base URL for relative vocabulary lookups during validation. Startup fails if it's missing (`CatalogueLibProperties#validate`) |
| `catalogue-lib.jaxb.include-packages` | No | empty | Extra packages to scan for JAXB classes. Their `enum`s become vocabularies |
| `catalogue-lib.jaxb.generated-classes-package-name` | No | `gr.uoa.di.madgik.xsd2java` | Package of xjc-generated classes, always scanned |

`base-url` carries a `@jakarta.validation.constraints.Null` annotation, which contradicts its
required status. It has no effect, because the properties class isn't `@Validated`.

## Registry settings

Catalogue reads no `registry.*` keys itself, but its `model` resource type is loaded through the
host's registry `ResourceTypeInit` (`registry.resource-type-init.location`, default
`classpath:resourceTypes`). That default expands to `classpath:resourceTypes/*.json`, which scans only
the first `resourceTypes` folder on the class path. A host with its own `resourceTypes/` folder
therefore hides `model.json` unless it sets
`registry.resource-type-init.location=classpath*:resourceTypes`.

## Example (placeholders only)

```properties
catalogue-lib.validation.enabled=true
catalogue-lib.validation.base-url=https://<host>/<context-path>
catalogue-lib.jaxb.include-packages=<com.example.domain>
```

## Environments

- Tests: `src/test/resources/application.yml` is empty. Tests construct objects directly.
- Hosts: values are set per deployment through environment variables or an external configuration.
  They aren't stored in the repository.

## Secrets

None. Vocabulary endpoints are called without credentials. (`ModelResponseValidator`)
