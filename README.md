# Catalogue

A Spring Boot library on top of the [registry](https://github.com/madgeek-arc/registry) that manages
**form models** (sections, fields, field types, display rules) and the **items** created from them.
It validates submitted items against their model (field types, required fields, patterns, dates,
vocabularies, URLs), exposes generic CRUD/search REST endpoints, and generates TypeScript typings of
the UI model for frontends.

## Role

Supplies the form/model engine that backends use to define survey-like data models and store
validated responses as registry resources. It needs a registry implementation in the same
application (for example `registry-starter-service`). Its REST controllers are registered in the host
application and appear in the host's springdoc OpenAPI document.

## Securing the REST API

The library has no authentication or authorisation, and every application that includes it gets its
REST controllers, including a log-file reader (`/logs`) and a generic outbound HTTP request endpoint
(`/request/default`). **The consuming application is responsible for guarding those paths** in its
own security configuration. See [docs/security.md](docs/security.md).

## Version lines

These docs describe `main` (9.1.x). The `version/10.0.0` line moves to Spring Boot 4 and registry 5.0.0.

## Prerequisites

- A Spring Boot 3 application on Java 21 that provides the registry service beans (`ResourceService`,
  `ResourceTypeService`, `SearchService`, `ParserService`, ...).
- Property `catalogue-lib.validation.base-url` when validation is enabled (the default).

## Quick start

```xml
<dependency>
    <groupId>gr.uoa.di.madgik</groupId>
    <artifactId>catalogue</artifactId>
    <version>9.1.1</version>
</dependency>
```

```properties
catalogue-lib.validation.base-url=https://<host>/<context-path>
# catalogue-lib.jaxb.include-packages=com.example.domain
```

The auto-configuration registers the controllers, the `ModelService` and the generic item
services, and the registry loads the bundled `model` resource type (`resourceTypes/model.json`).
**The consuming application must guard the exposed endpoints**: see [docs/security.md](docs/security.md).

## Build

```bash
mvn clean verify     # compiles, runs tests, generates target/catalogue-model.d.ts
mvn install
```

## Documentation

- [Architecture](docs/architecture.md) · [Integration](docs/integration.md) · [API](docs/api.md)
- [Configuration](docs/configuration.md) · [Security](docs/security.md)
- [Development](docs/development.md) · [Testing](docs/testing.md)
- [Contributing](CONTRIBUTING.md) · [Changelog](CHANGELOG.md)

There are no deployment or operations guides, because the library is not deployed on its own.

## Licence

Apache License 2.0, see [LICENSE](LICENSE). Maintained by the madgeek-arc team
(<https://github.com/madgeek-arc/catalogue>).
