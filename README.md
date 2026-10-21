# Catalogue

A Spring Boot library on top of the [registry](https://github.com/madgeek-arc/registry) that manages
**form models** (sections, fields, field types, display rules) and validates the resources created
from them. It validates submitted resources against their model (field types, required fields,
patterns, dates, vocabularies, URLs), can derive registry resource types from models, exposes REST
endpoints for models, and generates TypeScript typings of the model for frontends.

## Component metadata

| Field | Value |
|---|---|
| Component | Catalogue (`gr.uoa.di.madgik:catalogue`) |
| Type | Supporting service (Java library embedded in a backend application, not deployed on its own) |
| Status | `version/10.0.0` line, `10.0.0-SNAPSHOT` (unreleased). The latest release, `v9.1.1`, is on the `main` (9.x) line |
| Technology | Java 25, Spring Boot 4.0 (web MVC controllers, WebFlux `WebClient`), registry 5.0.0 (`registry-bom`), Jackson 3, JAXB (through the registry), Micrometer tracing, Maven |
| API | Yes, REST controllers registered in the host application (`/forms`, `/models`, `/items`, `/request/default`); no authentication: the consuming application guards the paths |
| Swagger | Contributes to the host's springdoc OpenAPI |
| Deployment | Library (Maven artefact) |
| Documentation | [docs/](docs/) |

## Role

Supplies the form/model engine that backends use to define survey-like data models and store
validated responses as registry resources. It needs the registry service starter in the same
application (`registry-starter-service` with `registry-core-helper`).

## Securing the REST API

The library has no authentication or authorisation, and every application that includes it gets its
REST controllers, including model deletion and a generic outbound HTTP request endpoint
(`/request/default`). **The consuming application is responsible for guarding those paths** in its
own security configuration. See [docs/security.md](docs/security.md).

## Prerequisites

- A Spring Boot 4 application on Java 25 that embeds the registry 5.0.0 service starter and
  `registry-core-helper`.
- Property `catalogue-lib.model.validation.base-url` when validation is enabled (the default).

## Quick start

```xml
<dependency>
    <groupId>gr.uoa.di.madgik</groupId>
    <artifactId>catalogue</artifactId>
    <version>10.0.0-SNAPSHOT</version>
</dependency>
```

```properties
catalogue-lib.model.validation.base-url=https://<host>/<context-path>
```

The auto-configuration registers the controllers, `ModelService` and the validator, and the registry
loads the bundled `model` resource type (`resourceTypes/model.json`). **The consuming application must
guard the exposed endpoints**: see [docs/security.md](docs/security.md).

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
