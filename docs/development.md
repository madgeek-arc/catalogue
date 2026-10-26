# Catalogue — Development

## Tools

| Tool | Version | Evidence |
|---|---|---|
| JDK | 25 | `pom.xml` |
| Maven | 3.9+ (no wrapper) | repository root |

## Commands

```bash
mvn clean verify          # compile, TypeScript typings, tests
mvn install -DskipTests   # install the SNAPSHOT for local host applications
```

Verified: `mvn clean install` passes on `fix/report-issues` (86 tests). It produces
`target/catalogue-10.0.0-SNAPSHOT.jar` and `target/catalogue-model.d.ts`.

## Code generation

- **TypeScript**: `typescript-generator-maven-plugin` (`process-classes`) maps `domain`,
  `domain.types` and `dto` to `target/catalogue-model.d.ts` (interfaces and enums), generated for
  frontends to use.
- **Configuration metadata**: the Spring Boot configuration processor runs at compile time
  (`<proc>full</proc>`, needed on JDK 23+) and publishes `META-INF/spring-configuration-metadata.json`
  for `catalogue-lib.*`.

## Working with the registry

Catalogue depends on `registry-core-rest` and `registry-core-helper`, with versions from
`registry-bom` (`registry.version`). After changing the registry, run `mvn install -DskipTests` there
and rebuild catalogue, then the host.

## Conventions

Conventional Commits (the history uses `feat:`, `fix:`, `refactor:`, `build:`, `chore:`). Apache 2.0
headers on Java files. Outbound requests built from request data go through `PublicAddressGuard`.
