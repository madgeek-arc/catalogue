# Contributing

Contributions are welcome: bug reports, feature requests and pull requests.

## Reporting issues

Use [GitHub Issues](https://github.com/madgeek-arc/catalogue/issues) for bugs, feature requests and
questions. For bugs, include the version, the environment, the steps to reproduce, and the expected
and actual behaviour.

## Branching

- `main` carries the released 9.x line and its next `-SNAPSHOT` version.
- Major lines are prepared on `version/<major>` branches (this one is `version/10.0.0`). Features and
  fixes go on `feature/<name>` / `fix/<name>` branches, with pull requests into the target line.
- Without write access to the repository, fork it, create the branch in your fork and open the
  pull request from there.

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `refactor:`, `build:`,
`chore:`, `docs:`). Mark breaking changes with `!` or a `BREAKING CHANGE:` footer. The changelog is
grouped by these types.

## Pull requests

- Describe the change and its motivation, and link the related issue.
- At least one maintainer review is required before merging.
- Changes to `domain`/`dto` change the generated TypeScript typings. Mention them for frontend
  maintainers.
- Changes to REST paths or `catalogue-lib.*` properties affect every consuming application. Update `docs/`.

## Required checks

- `mvn clean verify` must pass.
- Validation changes need cases in `ModelResponseValidatorTest`.

## Releases and versioning

- [Semantic Versioning](https://semver.org/).
- Release with `maven-release-plugin` (`mvn release:prepare release:perform`): tags `v<version>`,
  commits prefixed `chore(release):`, artefacts deployed to the madgik repository (`pom.xml`
  `distributionManagement`).
