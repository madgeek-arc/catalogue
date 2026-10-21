# Catalogue — Security

## Authentication and authorisation

The library defines no security configuration. All its controllers are added to the application that
includes it, and there is no property to turn them off.

## Responsibility of the consuming application

Guarding the library's REST paths is the responsibility of the application that includes it. Cover
every path in the application's security configuration: `/forms/**`, `/models/**`, `/resourceTypes`,
`/items/**` and `/request/default` (relative to the context path; `FormsController` answers both at the
root and under `/forms`). A rule that only lists some of them leaves the rest open.

| Endpoint | Risk | Evidence | Recommended rule |
|---|---|---|---|
| `POST /request/default` | Outbound HTTP request with any method, caller-supplied headers and body, response returned to the caller. Limited to public addresses by default | `controller/DataRequestController`, `service/DefaultDataRequestService` | Authenticated users at minimum |
| `POST/PUT/DELETE /forms/models**` and `/models**` (including delete-all) | Changing models changes the validation rules for every resource | `FormsController` | Admin only |
| `POST /forms/resourceTypes`, `/resourceTypes` | Creates registry resource types (and their database views) | `FormsController` | Admin only |
| `POST/PUT/DELETE /items**` | Generic writes to **any** registry resource type named in `resourceType` | `GenericItemController` (deprecated) | Restrict per resource type |

## Outbound requests from submitted data

| Request | Target | Protection | Evidence |
|---|---|---|---|
| Data request | URL in the request body | Only `http`/`https` URLs whose host resolves to public addresses. Loopback, private, link-local (including cloud metadata), carrier-grade NAT, multicast and IPv6 unique-local addresses get 400. Redirects aren't followed. 10 s connect and 30 s response timeouts | `DefaultDataRequestService`, `PublicAddressGuard`, `DefaultDataRequestServiceTest` |
| URL field validation | Submitted URL values | Same address check, applied to the URL and to every redirect hop. 10 s timeouts | `ModelResponseValidator#validateUrl`, `ModelResponseValidatorTest$UrlAddressGuard` |
| Vocabulary validation | Vocabulary URL from the model, or `catalogue-lib.model.validation.base-url` | Not address-checked: the host comes from configuration, and the submitted value only becomes a path segment | `ModelResponseValidator#validateVocabulary` |

`catalogue-lib.data-request.allow-private-addresses` and
`catalogue-lib.model.validation.allow-private-addresses` turn the address check off for deployments
that need internal targets.

## Input validation

Model-driven validation covers types, patterns, required fields, dates, URLs and vocabularies, and
rejects fields not defined in the model.. When no model, or more than one, exists for a
resource type, validation is skipped with a `WARN`.

## Error handling

`GenericExceptionController` handles exceptions for every controller of the application at highest
precedence. Unexpected exceptions return 500 with a generic message. Details are only logged.

## Logging and personal data

- With `DEBUG`, the full resource being validated is logged (`ModelResponseValidator#validate`).
  Avoid this in production for types holding personal data.
- Error responses include a trace ID.

## Secrets

The library holds none.
