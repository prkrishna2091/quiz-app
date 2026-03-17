# Quiz App - Project Documentation

This repository contains a Spring Boot application that:
- verifies Google ID tokens,
- issues app JWT tokens,
- accepts a YouTube URL,
- extracts captions + video metadata,
- generates a quiz using Gemini AI,
- returns a combined response.

## Documentation Index

- `docs/ARCHITECTURE_AND_LAYERS.md` - package-by-package and layer-by-layer documentation.
- `docs/API_AND_FUNCTIONALITY.md` - endpoint contracts, auth usage, request/response examples.
- `docs/CONFIGURATION_AND_OPERATIONS.md` - configuration properties, security flow, error handling, logs, and run/test guide.

## Quick Start

```powershell
./mvnw.cmd clean test
./mvnw.cmd spring-boot:run
```

Open:
- Swagger UI: `http://localhost:8451/swagger-ui.html`
- Health: `http://localhost:8451/health`
- Actuator health: `http://localhost:8451/actuator/health`

