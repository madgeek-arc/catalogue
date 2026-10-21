# Catalogue — API

Catalogue's controllers are component-scanned into the **host application** and share its base URL,
context path, security filter chain and springdoc OpenAPI document.
(`CatalogueConfiguration` `@ComponentScan("gr.uoa.di.madgik.catalogue")`)

| Item | Value |
|---|---|
| Base URL | The host's |
| Authentication | None. The consuming application guards the paths below (see [security.md](security.md)) |
| OpenAPI | Included in the host's springdoc document (`/v3/api-docs` by default) |
| Errors | JSON `ServerError` with HTTP status mapped by `GenericExceptionController` (403, 401, 409, 404, 400, 501, 422, 500), including the trace ID |

## Endpoint groups

| Group | Paths | Purpose | Controller |
|---|---|---|---|
| Models | `POST/GET /forms/models`, `GET/PUT/DELETE /forms/models/{id}`, `DELETE /forms/models` | Manage form models | `FormsController` |
| Items | `POST /items?resourceType=`, `PUT/DELETE/GET /items/{id}`, `GET /items`, `GET /items/highlighted`, `GET /items/{id}/recommendations`, `GET /items/search?resourceType=&field=&value=` | Generic CRUD, browse, highlighted search, similarity recommendations | `GenericItemController` |
| Vocabularies | `GET /ui/vocabularies/map` | Enum vocabularies from the JAXB packages | `UiController` |
| Data request | `POST /request/default` (body `DataRequest`: method, url, headers, body, params, expression, contentType) | Perform an outbound HTTP request and extract a value | `DataRequestController` |
| Logs | `POST /logs/level/root?standardLevel=`, `POST /logs/level/package/{path}?standardLevel=`, `GET /logs[?filepath=]` | Change Log4j2 levels. List `logs/`. Download a file | `LogsController` |

**Note:** `DELETE /forms/models` deletes all models. (`FormsController`). Data request
and logs are security-sensitive (see [security.md](security.md)).
