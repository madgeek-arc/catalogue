# Catalogue — Development

## Tools

| Tool | Version | Evidence |
|---|---|---|
| JDK | 21 | `pom.xml` |
| Maven | 3.9+ (no wrapper) | repository root |

## Commands

```bash
mvn clean verify          # compile, xjc (no schemas yet), TypeScript typings, tests
mvn install -DskipTests   # install the SNAPSHOT for local host applications
```

Verified: `mvn verify` passes at `1451f78`. It produces `target/catalogue-9.1.2-SNAPSHOT.jar` and
`target/catalogue-model.d.ts`.

## Code generation

- **xjc**: put XSDs in `src/main/resources/schema/`. Classes are generated into
  `gr.uoa.di.madgik.xsd2java` (bindings in `src/main/resources/xjb/binding.xjb`, `-Xxew`).
  There are currently no schemas.
- **TypeScript**: `typescript-generator-maven-plugin` (`process-classes`) maps `ui.domain`,
  `ui.domain.types` and `dto` to `target/catalogue-model.d.ts` (interfaces and enums), generated for
  frontends to use.

## Working with the registry

Catalogue depends on `registry-core-rest` and `registry-core-helper` (`registry-core.version`). After
changing the registry, run `mvn install -DskipTests` there and rebuild catalogue, then the host.

## Conventions

Conventional Commits (the history uses `feat:`, `fix:`, `refactor:`, `build:`, `chore:`). Apache 2.0
headers on Java files.
