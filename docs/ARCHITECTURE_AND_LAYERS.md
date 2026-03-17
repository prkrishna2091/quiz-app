# Architecture and Layers

## 1) High-Level Architecture

The app follows a layered Spring Boot architecture:

1. **Configuration Layer** (`config`)  
   Security filter chain, CORS, JWT request filter.
2. **Controller Layer** (`controller`)  
   REST endpoints for auth, quiz generation, and health.
3. **Service Layer** (`service`, `service.impl`)  
   Business logic for Google token verification, JWT operations, caption extraction, and Gemini quiz generation.
4. **Contract Layer** (`dto`, `request`, `response`)  
   Request/response objects exchanged internally and with clients.
5. **Utility Layer** (`util`)  
   Prompt template generation.

Main entry point: `src/main/java/com/codapt/quizapp/QuizAppApplication.java`.

---

## 2) Package-by-Package Documentation

## `com.codapt.quizapp.config`

### `SecurityConfiguration`
- Enables web security.
- Configures CORS for local frontend origins (`5173`, `3000`).
- Sets session policy to stateless.
- Allows public access to:
  - `/api/auth/**`
  - `/health`, `/favicon.ico`
  - `/actuator/health`, `/actuator/info`
  - `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`
- Secures all other endpoints.
- Registers `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.

### `JwtAuthenticationFilter`
- Reads `Authorization` header.
- Accepts `Bearer <token>` format.
- Validates token using `JwtService`.
- Sets authenticated principal in `SecurityContext` when token is valid.
- Ignores malformed token and continues filter chain (request later fails auth where needed).

---

## `com.codapt.quizapp.controller`

### `AuthController` (`/api/auth`)
- Endpoint: `POST /api/auth/google`
- Accepts Google ID token in request body (`TokenRequest`).
- Calls `GoogleAuthService.verifyToken(...)`.
- Generates app JWT via `JwtService.generateToken(...)`.
- Returns `JwtResponse` on success.
- Returns HTTP 401 on auth failures.

### `QuizController` (`/api/quiz`)
- Endpoint: `POST /api/quiz/generate`
- Validates YouTube URL in request.
- Calls `YoutubeCaptionService.downloadCaptions(...)`.
- Calls `GeminiService.getQuizFromGemini(...)` with extracted caption.
- Returns `YoutubeQuizResponse` containing:
  - caption
  - quiz
  - videoTitle
  - channelName
  - videoLength

### `HealthController`
- Endpoints:
  - `GET /health`
  - `GET /health/`
  - `GET /favicon.ico` (returns 204)
- Exposes build metadata and health status.
- Uses `HealthEndpoint` and cached status refreshed every 30 seconds.

### `GlobalExceptionHandler`
- Global handlers for:
  - `NoResourceFoundException` -> 404
  - `HttpMessageNotReadableException` -> 400
  - `IllegalArgumentException` -> 400
  - `RuntimeException` -> 400
  - generic `Exception` -> 500
- Returns consistent JSON error body with timestamp/path/message.

---

## `com.codapt.quizapp.service`

### `GoogleAuthService`
- Verifies Google ID token using Google verifier SDK.
- Validates token audience against configured Google client ID.
- Returns Google payload if valid.
- Throws `GoogleAuthException` for invalid/verification issues.

### `JwtService`
- Generates JWT containing `sub`, `name`, and `email` claims.
- Parses token claims.
- Exposes methods to extract user email and validate expiration/signature.

### `GeminiService` + `GeminiServiceImpl`
- Generates quiz output from transcript text.
- Builds strict JSON prompt from `PromptGenerator` template.
- Calls Spring AI `ChatClient` backed by Google GenAI model.
- Handles unsupported model errors with contextual runtime messages.

### `YoutubeCaptionService` + `YoutubeCaptionServiceImpl`
- Invokes bundled `yt-dlp.exe` with `--dump-json`.
- Parses YouTube metadata: title, channel, duration.
- Resolves subtitle URL from manual subtitles first, then auto captions.
- Downloads caption payload and cleans transcript text.
- Returns `YoutubeCaptionDetails`.

---

## `com.codapt.quizapp.dto`

- `TokenRequest`: input for Google login endpoint.
- `JwtResponse`: JWT response payload.
- `YoutubeQuizRequest`: input for quiz generation.
- `YoutubeCaptionDetails`: internal result object from caption service.
- `YoutubeQuizResponse`: final API response for quiz generation.

---

## `com.codapt.quizapp.request` and `com.codapt.quizapp.response`

- `PromptRequest` and `PromptResponse` define generic prompt data structures.
- These classes are useful for typed request/response shape mapping and future direct model integrations.

---

## `com.codapt.quizapp.util`

### `PromptGenerator`
- Holds reusable prompt template.
- Template enforces:
  - question count,
  - difficulty,
  - strict JSON output format.

