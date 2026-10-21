# Catalogue — Security

## Authentication and authorisation

The library defines no security configuration. All its controllers are added to the host
application and are open unless the host's security rules cover them, and there is no property to
turn them off.

## Responsibility of the consuming application

Guarding the library's REST paths is the responsibility of the application that includes it. Cover
every path in the application's security configuration: `/forms/**`, `/items/**`, `/ui/**`, `/request/default` and `/logs/**` (relative to
the context path). A rule that only lists some of them leaves the rest open. At least the endpoints
in the table below need restricting.

| Endpoint | What it does | Evidence | Recommended host rule |
|---|---|---|---|
| `GET /logs?filepath=<path>` | Downloads a file from the server | `controller/LogsController#fetchLogFile`, `utils/FileUtils#readFile` | Admin only, or disable |
| `POST /logs/level/**` | Changes log levels at runtime | `LogsController` | Admin only |
| `POST /request/default` | Sends an outbound HTTP request described by the caller and returns the extracted response | `controller/DataRequestController`, `service/DefaultDataRequestService#retrieve` | Authenticated users at minimum |
| `POST/PUT/DELETE /forms/models**` (including delete-all) | Changing models changes validation rules for every item | `FormsController` | Admin only |
| `POST/PUT/DELETE /items**` | Generic writes to **any** registry resource type named in `resourceType` | `GenericItemController` | Restrict per resource type |

## Outbound requests during validation

Vocabulary and URL fields are validated by issuing HTTP GET requests (5 s response timeout) to
URLs derived from the model and from submitted values (`ModelResponseValidator`). Restrict outbound
network access, and use relative vocabulary paths resolved against `catalogue-lib.validation.base-url`.

## Input validation

Model-driven validation covers types, patterns, required fields, dates, URLs and vocabularies, and
rejects fields not defined in the model.. When no model exists for a resource type,
validation is skipped.

## Logging and personal data

- With `DEBUG`, the full resource being validated is logged (`ModelResponseValidator#validate`).
  Avoid this in production for types holding personal data.
- `CatalogueLoggingAspect` logs method entry and exit at `TRACE`.
- Error responses include a trace ID. Stack traces are hidden for `NoResourceFoundException`
  (changelog).

## Secrets

The library holds none.
