## API

The on-premises installation has two APIs:

1. The workspace API for getting and putting workspaces.
2. The admin API for creating, deleting, and listing workspaces.

### Workspace API

The workspace API is documented at [Structurizr - Help - Web API](https://structurizr.com/help/web-api),
and has the following endpoints:

- GET `/api/workspace/{workspaceId}` (get workspace)
- PUT `/api/workspace/{workspaceId}` (put workspace)
- PUT `/api/workspace/{workspaceId}/lock` (lock workspace)
- DELETE `/api/workspace/{workspaceId}/lock` (unlock workspace)

### Admin API

The admin API provides a way to create, delete, and list workspaces.
This API is disabled by default, and can be enabled by adding a property named
`structurizr.apiKey` to your `structurizr.properties` file - see [Configuration](03-configuration.md) for more details.

The following endpoints are available:

- GET `/api/workspace` (list all workspaces)
- POST `/api/workspace` (create workspace)
- DELETE `/api/workspace/{workspaceId}` (delete workspace)

The API key should be specified via the `X-Authorization` header.
For example, the following `curl` command could be used to get a list of workspaces:

```
curl --header "X-Authorization: 123456789" http://localhost:8080/api/workspace
```