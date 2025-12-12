# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Repository overview (big picture)
This is a monorepo with two primary deliverables:
- `backend/`: Node.js + TypeScript + Express REST API (PostgreSQL via Sequelize). Provides auth, MRFC/proponent/meeting management, document storage, compliance/OCR analysis, notifications, etc.
- `app/`: Android tablet application (Kotlin) using an MVVM-style structure (UI → ViewModel → Repository → API/Room). Includes offline-first pieces (Room + WorkManager).

## Common commands (Windows / PowerShell)

### Backend (Node/TypeScript) — `backend/`
Run from repo root unless noted.

Install deps:
- `cd backend; npm install`

Run dev server (ts-node + nodemon):
- `cd backend; npm run dev`

Build + run compiled output:
- `cd backend; npm run build`
- `cd backend; npm start`

Health check:
- `curl http://localhost:3000/api/v1/health`

Lint / format:
- `cd backend; npm run lint`
- `cd backend; npm run format`

Tests:
- All tests: `cd backend; npm test`
- Single test file (Jest):
  - `cd backend; npm test -- --runTestsByPath src/__tests__/some.test.ts`
- Single test by name:
  - `cd backend; npm test -- -t "part of test name"`

Database utilities (see `backend/package.json` scripts):
- Migrations: `cd backend; npm run db:migrate`
- Seed quarters: `cd backend; npm run db:seed`
- Verify schema: `cd backend; npm run db:verify`

Notes on configuration:
- Backend uses `dotenv` and expects a `backend/.env` file (see `backend/.env.example`).
- `backend/src/server.ts` binds to `0.0.0.0` and prints URLs for localhost and Android emulator (`10.0.2.2`).

### Android app (Gradle) — `app/`
Use the Gradle wrapper from repo root:

Build debug APK:
- `./gradlew.bat :app:assembleDebug`

Run unit tests:
- `./gradlew.bat :app:testDebugUnitTest`

Run instrumented tests (requires emulator/device):
- `./gradlew.bat :app:connectedDebugAndroidTest`

Run a single unit test class:
- `./gradlew.bat :app:testDebugUnitTest --tests "com.mgb.mrfcmanager.SomeTest"`

Run a single instrumented test class:
- `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mgb.mrfcmanager.SomeInstrumentedTest`

## High-level architecture (how the pieces fit)

### Backend architecture (`backend/src/`)
Entrypoint:
- `backend/src/server.ts`:
  - Loads env via `dotenv`
  - Installs security + infra middleware (Helmet, CORS, rate limiting, compression, JSON parsing, morgan in dev)
  - Mounts the API router at `/api/v1` (version comes from `API_VERSION` env var)
  - Initializes the database and ensures a super-admin exists at startup

Core layers (typical request flow):
- `routes/` → `controllers/` → `models/` (+ `utils/` + `middleware/`)
  - `routes/`: Express route modules and router composition (imported by `server.ts` as `routes`)
  - `controllers/`: Request handlers and feature logic
  - `models/`: Sequelize models; `server.ts` imports `./models` to initialize and create the super admin
  - `middleware/`: JWT auth/authorization, error handling, audit logging, upload handling, etc.
  - `config/`: database connection/config and external service configuration

Persistence and migrations:
- DB schema lives under `backend/database/`.
- Operational DB scripts live under `backend/scripts/` and `backend/src/scripts/`.
- There is also a `backend/migrations/` directory for SQL migrations.

Document + compliance/OCR features:
- The repo’s docs describe two paths for compliance analysis:
  - fast text extraction for digital PDFs
  - OCR for scanned PDFs using `pdfjs-dist` + `tesseract.js`
- When debugging compliance/OCR behavior, search in `backend/src/controllers/` and `backend/src/utils/` for "compliance", "ocr", "pdf", and related scripts under `backend/src/scripts/` (there are dedicated `npm run test:pdf` and `npm run test:ocr` scripts).

### Android app architecture (`app/src/main/java/com/mgb/mrfcmanager/`)
The Android app is organized around an MVVM-style structure:
- `ui/`: Activities/fragments/adapters and UI screens (grouped by feature area, e.g. `ui/auth`, `ui/admin`, `ui/meeting`, `ui/user`)
- `viewmodel/`: ViewModels exposing state via LiveData and coordinating calls into repositories
- `data/`:
  - `data/remote/`: Retrofit API interfaces/DTOs/interceptors
  - `data/repository/`: repositories that bridge API + local storage
  - `data/local/`: Room database, DAOs, entities

Offline-first pieces:
- Room database entities/DAOs live under `data/local/`.
- Background sync is implemented with WorkManager (see packages like `sync/` and related managers/utilities).

Backend connectivity:
- For Android Emulator, the backend base URL is expected to be `http://10.0.2.2:3000` (not `localhost`). This is explicitly called out in the root `README.md`.

## Deployment and ops references (existing docs)
If you’re adding/adjusting deployment behavior, start from these repo files:
- `render.yaml`: Render configuration (build: `npm install && npm run build`, start: `npm start`, `rootDir: backend`)
- `DEPLOY_RENDER_QUICKSTART.md`, `RENDER_DEPLOYMENT_GUIDE.md`: Render deployment notes
- `RAILWAY_DEPLOYMENT_SUMMARY.md` and other Railway docs under repo root and `backend/`

For “current state of the project” context, the repo keeps a very large status tracker:
- `PROJECT_STATUS.md` (and an even larger `PROJECT_STATUS_NEW.md`)

## Existing AI/agent rules in this repo
- `.claude/settings.local.json` exists (Claude-specific local settings/permissions). It also embeds a prior troubleshooting plan referencing Android-side bugs (Other Matters filtering and Voice Recording observer reload behavior). Treat it as potentially outdated, but it can be a useful breadcrumb when investigating those specific issues.
