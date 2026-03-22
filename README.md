# Project Chimera — Autonomous Influencer Network

Spec-driven Java 21+ scaffold for a **Planner / Worker / Judge** swarm with **MCP-only** external integration, contract-first tests, and AI-agent governance files.

## Quickstart (humans & coding agents)

1. Read [`CLAUDE.md`](./CLAUDE.md) (prime directive: specs before code).
2. Read [`specs/_meta.md`](./specs/_meta.md), then [`specs/functional.md`](./specs/functional.md) (US-XXX), then [`specs/technical.md`](./specs/technical.md).
3. For OpenClaw-style network messaging, see [`specs/openclaw_integration.md`](./specs/openclaw_integration.md).
4. Architecture rationale: [`research/architecture_strategy.md`](./research/architecture_strategy.md); MCP vs skills: [`research/tooling_strategy.md`](./research/tooling_strategy.md).

## Build commands (Makefile)

| Target | Purpose |
|--------|---------|
| `make setup` | `mvnw` clean install, skip tests |
| `make lint` | Checkstyle |
| `make test` | JUnit 5 + contract tests under `tests/` |
| `make spec-check` | Assert core spec and `CLAUDE.md` files exist and are non-empty |
| `make verify` | `mvn verify` + Checkstyle |

On Windows without GNU Make, use `mvnw.cmd` directly (e.g. `mvnw.cmd test`).

## Repository map

| Area | Role |
|------|------|
| `specs/` | Source of truth: functional + technical + OpenClaw integration |
| `research/` | Ratified architecture and tooling decisions |
| `skills/` | Runtime skill **contracts** (README per skill); Java `Skill` SPI lives under `src/main/java/com/chimera/skill/` |
| `src/main/java/com/chimera/` | Planner, workers, judge, models (records), MCP gateway, streams |
| `tests/` | Contract tests (JUnit 5); may fail until scaffolds are implemented (TDD) |

## CI and contract-test status

GitHub Actions runs **`make spec-check`**, **`make lint`**, and **`make test`**. Some tests are **expected to fail** until `DefaultJudge`, `TrendFetcherWorker`, and `META-INF/services` Skill registrations are completed—this is intentional TDD. Green **`spec-check`** + **`lint`** still validate governance and style.

## AI review

[`.coderabbit.yaml`](./.coderabbit.yaml) aligns automated review with specs and Java conventions. Cursor duplicates of agent rules: [`.cursor/rules/CLAUDE.md`](./.cursor/rules/CLAUDE.md).

## License

See repository license file (if present).
