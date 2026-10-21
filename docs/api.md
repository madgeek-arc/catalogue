# Catalogue — API

Catalogue's controllers are component-scanned into the **host application** and share its base URL,
context path, security filter chain and springdoc OpenAPI document. There is no property to leave
them out. (`CatalogueConfiguration` `@ComponentScan("gr.uoa.di.madgik.catalogue")`)

| Item | Value |
|---|---|
| Base URL | The host's |
| Authentication | None. The consuming application guards the paths below (see [security.md](security.md)) |
| OpenAPI | Included in the host's springdoc document (`/v3/api-docs` by default) |
| Errors | RFC 7807 `ProblemDetail` with the trace ID, from `GenericExceptionController` |

## Endpoint groups

`FormsController` is mapped both at the root and under `/forms`, so each of its paths exists twice
(for example `/models` and `/forms/models`).

| Group | Paths | Purpose | Controller |
|---|---|---|---|
| Models | `POST/GET /forms/models`, `GET/PUT/DELETE /forms/models/{id}`, `DELETE /forms/models` | Manage form models. `DELETE /forms/models` deletes all models | `FormsController` |
| Resource types | `POST /forms/resourceTypes` (body: a `Model`) | Create the registry resource type derived from a model | `FormsController` |
| Items (deprecated) | `POST /items?resourceType=`, `PUT/DELETE/GET /items/{id}`, `GET /items`, `GET /items/highlighted`, `GET /items/{id}/recommendations` | Generic CRUD, browse, highlighted search, recommendations. Deprecated for removal: use the registry's `/records/{resourceType}` | `GenericItemController` |
| Data request | `POST /request/default` (body `DataRequest`: method, url, headers, body, params, expression, contentType) | Perform an outbound HTTP request to a public address and extract a value with JSONPath/XPath. Non-public targets get 400 | `DataRequestController` |

## Errors

`GenericExceptionController` is a `@RestControllerAdvice` at **highest precedence** with a catch-all
handler, so it handles exceptions for every controller of the host application, not only catalogue's.
A host advice for the same exception types wins only with a higher precedence.

| Exception | Status |
|---|---|
| `ValidationException`, `ServiceException`, `UnsupportedSearchParameterException` | 400 |
| `InsufficientAuthenticationException` | 401 |
| `AccessDeniedException` | 403 |
| `ResourceNotFoundException` | 404 |
| `ResourceAlreadyExistsException` | 409 |
| `MissingResourceEmbeddingsException`, `SQLException`, `DataAccessException` | 422 |
| `UnsupportedOperationException` | 501 |
| `ResourceException`, `HttpClientErrorException` | their own status |
| `MethodArgumentNotValidException` and other Spring `ErrorResponse` exceptions | their own status |
| Anything else | 500, with a generic message. The exception is logged at `ERROR` |
