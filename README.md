# mcp-ai-client

[![build](https://github.com/vuppalapatisn/mcp-ai-client1/actions/workflows/build.yml/badge.svg)](https://github.com/vuppalapatisn/mcp-ai-client1/actions/workflows/build.yml)

Spring Boot 4 chat host that puts Google Gemini in front of the MCP tools
published by [mcp-order-server](https://github.com/vuppalapatisn/mcp-order-server).

- **Model** — Gemini via `spring-ai-starter-model-google-genai` (Gemini Developer API mode)
- **Tools** — discovered over MCP streamable HTTP from the order server
- **REST API** — documented with OpenAPI 3 / Swagger UI

## Run it

```bash
mvn spring-boot:run
```

Listens on port `8080`. Set `GEMINI_API_KEY` to a real key from
[Google AI Studio](https://aistudio.google.com/apikey) or `/api/chat` returns
`400 API_KEY_INVALID`.

| Surface | URL |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |
| Ping | http://localhost:8080/api/ping |

### Behind a TLS-intercepting corporate proxy

By default the client talks to the deployed order server over HTTPS. On a
machine whose outbound TLS is intercepted, the proxy's root CA is usually
absent from the JDK truststore and **every** outbound HTTPS call from the JVM
fails during startup:

```
javax.net.ssl.SSLHandshakeException: PKIX path building failed:
  unable to find valid certification path to requested target
```

The JVM can read the Windows certificate store instead, where the corporate CA
already lives. This changes nothing on disk:

```bash
java -Djavax.net.ssl.trustStoreType=Windows-ROOT -jar target/mcp-ai-client-1.0.0-SNAPSHOT.jar
```

Or avoid outbound TLS altogether by pointing at a locally run order server:

```bash
MCP_ORDER_SERVER_URL=http://localhost:8081 mvn spring-boot:run
```

## Configuration

Every value has a working default; only `GEMINI_API_KEY` must be supplied.

| Variable | Default | Notes |
| --- | --- | --- |
| `GEMINI_API_KEY` | `replace-me` | Required. Gemini Developer API key. |
| `GEMINI_MODEL` | `gemini-3.6-flash` | Any model id the `google-genai` SDK accepts. |
| `MCP_ORDER_SERVER_URL` | `https://mcp-order-server-1.onrender.com` | **Origin only** — see below. |
| `MCP_CLIENT_ENABLED` | `true` | `false` drops MCP entirely; chat then answers with no tools. |
| `MCP_CLIENT_INITIALIZED` | `true` | `false` defers connecting to first use instead of startup. |
| `MCP_REQUEST_TIMEOUT` | `60s` | Also bounds the startup handshake. |
| `PORT` | `8080` | Injected by Render, Heroku, Cloud Run. |
| `TESTING_API_ENABLED` | `true` | `false` removes `/api/conversations/**`. |

`MCP_ORDER_SERVER_URL` takes the origin with no path, because the client
appends the `/mcp` endpoint itself. Passing a browsable docs URL such as
`https://mcp-order-server-1.onrender.com/swagger-ui/index.html` breaks the
handshake — the order server's own Swagger UI lives there, but MCP does not.

### Startup connects eagerly

`MCP_CLIENT_INITIALIZED` defaults to `true`, so tools are listed during
startup. A misconfigured or unreachable order server fails fast and loudly
rather than surfacing later as a broken chat request.

The tradeoff is that **startup aborts if the order server is unreachable** —
the chat model depends on the tool callback resolver, so the context cannot
build without it. Two consequences:

- Deploying while the order server is down crash-loops the container. Set
  `MCP_CLIENT_INITIALIZED=false` to get up without a rebuild, then diagnose.
- A free-tier host that spins down when idle can take ~50s to answer its first
  request. `MCP_REQUEST_TIMEOUT` defaults to `60s` to absorb that cold start.

## REST API

`/api/chat` calls the model. Everything under `/api/conversations` reads and
writes conversation state directly, so it works with no API key and no order
server attached — useful for exercising context assembly and tenant isolation.

Both require `X-Tenant-Id` and `X-User-Id` headers. `/api/ping` requires none.

| Method | Path | Notes |
| --- | --- | --- |
| `GET` | `/api/ping` | Liveness. Touches no dependencies. |
| `POST` | `/api/chat` | Sends a message. Needs a valid key and a reachable order server. |
| `GET` | `/api/conversations/{id}/turns` | Stored turns for the caller's tenant. |
| `POST` | `/api/conversations/{id}/turns` | Appends a turn without calling the model. |
| `GET` | `/api/conversations/{id}/context` | The prompt the assembler would build. |
| `GET` | `/api/conversations/{id}/summary` | Current rolling summary. |
| `PUT` | `/api/conversations/{id}/summary` | Replaces the summary. |

```bash
curl http://localhost:8080/api/ping

curl -X POST http://localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Id: tenant-a' -H 'X-User-Id: user-1' \
  -d '{"conversationId":"c1","message":"what is the status of ORD-1001?"}'

curl http://localhost:8080/api/conversations/c1/context \
  -H 'X-Tenant-Id: tenant-a' -H 'X-User-Id: user-1'
```

## Docker

```bash
docker build -t mcp-ai-client .

docker run -p 8080:8080 \
  -e GEMINI_API_KEY=your-key \
  mcp-ai-client
```

The image runs as a non-root user and resolves Maven dependencies in a separate
cached layer. CI builds it, boots it, asserts `/actuator/health` and
`/v3/api-docs` respond, and pushes to GHCR on `main` and version tags.
