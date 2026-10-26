# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| 10.0.x | :white_check_mark: |
| 9.1.x | :white_check_mark: |
| < 9.1 | :x: |

10.0.x is the `version/10.0.0` line (Spring Boot 4) and 9.1.x the `main` line (Spring Boot 3). Until 10.0.0 is released, fixes for it land on `version/10.0.0`.

## Reporting a Vulnerability

Report vulnerabilities privately through GitHub: open the repository's **Security** tab and choose
**Report a vulnerability** ([direct link](https://github.com/madgeek-arc/catalogue/security/advisories/new)).
Don't report them in public issues, pull requests or discussions.

Include:

- the affected version or commit;
- steps to reproduce, or a proof of concept;
- the impact you expect.

The maintainers follow up in the private advisory, fix the issue in the supported versions, and publish
the advisory once a fixed release is available.

## Scope

This repository covers the library, including the REST controllers it registers in the host
application. Those controllers have no authentication of their own, so guarding their paths is the
host application's responsibility: an unguarded path in a host application is that application's
issue. Flaws in the library's own validation and outbound requests are in scope.

[docs/security-model.md](docs/security-model.md) describes the component's security model and the
settings a deployment should use.
