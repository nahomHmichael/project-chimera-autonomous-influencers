# Project Chimera — Autonomous Influencer Network

**Project Chimera** is a **spec-driven** Java **21** codebase for an autonomous influencer fleet: **Planner → Worker → Judge** coordination (FastRender-style swarm), **MCP-only** external integration, confidence-based **HITL** routing, and **CFO-governed** commerce boundaries. This repository is optimized for **AI-assisted development** (clear specs, Cursor rules, CodeRabbit policy) and **test-first** evolution of the runtime.

---

## What you get in this repo

| Layer | Contents |
|--------|-----------|
| **Specifications** | `specs/` — functional user stories (US-XXX), technical DTO/API contracts, HITL dashboard §6, OpenClaw integration, `TransactionRequest` §7 |
| **Architecture** | `research/architecture_strategy.md`, `research/tooling_strategy.md` |
| **Java runtime scaffold** | Spring Boot host, `com.chimera` packages (planner, worker, judge, skill, mcp, streams, model), Java **records** for DTOs |
| **Contract tests** | `tests/` — JUnit 5; many tests are **intentionally red** until scaffolds gain real logic (TDD) |
| **Operator demo (optional)** | `frontend/hitl-demo/index.html` — static mock HITL dashboard (no backend) |
| **Governance** | `CLAUDE.md`, `.cursor/rules/`, `.coderabbit.yaml` |
| **CI** | GitHub Actions: `spec-check`, Checkstyle, full test suite |

---

## Prerequisites

- **JDK 21** (`JAVA_HOME` set for Maven)
- **Git**
- **GNU Make** (Git Bash / WSL / macOS / Linux) — optional; you can use **`mvnw`** / **`mvnw.cmd`** directly on Windows
- **Docker** (optional) — for container build and run

---

## Quick start (local)

```bash
git clone <your-fork-or-upstream-url>
cd project-chimera-autonomous-influencers
```

1. Read **[`CLAUDE.md`](./CLAUDE.md)** (specs before code).
2. Skim **[`specs/_meta.md`](./specs/_meta.md)** → **[`specs/functional.md`](./specs/functional.md)** → **[`specs/technical.md`](./specs/technical.md)**.

### Build and quality gates

| Command | Description |
|---------|-------------|
| `./mvnw clean verify -DskipTests` | Compile and package **without** tests |
| `make setup` | Same idea via Makefile (`./mvnw clean install -DskipTests`) |
| `make lint` | Checkstyle (`checkstyle.xml`) |
| `make test` | Full JUnit 5 suite including `tests/` |
| `make spec-check` | Ensures ratified spec files and agent rule files exist |
| `make verify` | `mvn verify` + Checkstyle |

Windows (PowerShell / CMD) without Make:

```text
mvnw.cmd clean verify -DskipTests
mvnw.cmd checkstyle:check
mvnw.cmd test
```

### Run the Spring Boot application (host)

```bash
./mvnw spring-boot:run
```

Default HTTP port **8080** (see `src/main/resources/application.properties`). The app is still largely **scaffold**; it exists so the runtime host compiles and starts for future integration work.

---

## Docker

Build a **runtime image** (multi-stage: Temurin **JDK 21** build → **JRE 21** run). Packaging uses **`-DskipTests`** so the image builds even when contract tests fail (TDD).

```bash
make docker-build
# or: docker build -t chimera:latest .
```

Run the container (maps **8080**):

```bash
make docker-run
# or: docker run --rm -p 8080:8080 chimera:latest
```

Override JVM options if needed:

```bash
docker run --rm -p 8080:8080 -e JAVA_OPTS="-Xmx512m" chimera:latest
```

**Note:** The full test suite is **not** executed inside the default image build. Use **`make test`** or CI for that.

---

## Repository layout

| Path | Role |
|------|------|
| [`specs/`](./specs/) | Source of truth for behavior and contracts |
| [`research/`](./research/) | Ratified architecture and tooling decisions |
| [`skills/`](./skills/) | Skill **contracts** (README per skill); Java `Skill` SPI under `src/main/java/com/chimera/skill/` |
| [`src/main/java/com/chimera/`](./src/main/java/com/chimera/) | Application code (Planner, workers, Judge, CFO interface, models, MCP gateway, Redis stream helpers) |
| [`tests/`](./tests/) | Contract / TDD tests (additional test root via `build-helper-maven-plugin`) |
| [`frontend/`](./frontend/) | HITL concept doc + static [`hitl-demo`](./frontend/hitl-demo/index.html) |
| [`CLAUDE.md`](./CLAUDE.md) | Agent / human prime directives (records, virtual threads, MCP-only) |
| [`.github/workflows/`](./.github/workflows/) | CI workflow |

---

## HITL operator demo (no backend)

Open **[`frontend/hitl-demo/index.html`](./frontend/hitl-demo/index.html)** in a browser for a **mock** review queue (local state only). See **[`frontend/README.md`](./frontend/README.md)** for scope and non-goals.

---

## CI and test status

GitHub Actions runs **`make spec-check`**, **`make lint`**, and **`make test`**. Until **`DefaultJudge`**, **`TrendFetcherWorker`**, **`DefaultCfoJudge`**, and Skill SPI registrations are completed, **some tests are expected to fail** — that is deliberate **TDD**. **`spec-check`** and **`lint`** still guard specs and style.

---

## How to work effectively (contributors & agents)

1. **Never implement without a spec** — if behavior is missing from `specs/`, extend the spec first.
2. **DTOs:** Java **`record`** only for agent payloads; opaque blobs as **JSON strings** (no `Map<String, Object>` on DTOs).
3. **Concurrency:** **Virtual threads** for worker-scale paths (`Executors.newVirtualThreadPerTaskExecutor()`).
4. **External systems:** **MCP only** from the agent core — no ad-hoc vendor SDKs in `com.chimera` runtime paths.
5. **Traceability:** Public types should reference **SRS** sections and **US-XXX** in Javadoc where applicable.

Automated review policy: **[`.coderabbit.yaml`](./.coderabbit.yaml)**.

---

## License

See the repository license file if one is present.
