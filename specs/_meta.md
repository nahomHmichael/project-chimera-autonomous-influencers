# Project Chimera — Meta Specification
> **Version:** 1.0.0  
> **Status:** Ratified  
> **Last Updated:** 2026-03-21

## Vision

Project Chimera is an **Autonomous Influencer Network** — a fleet of AI agents that
research trends, generate multimodal content, and manage social engagement without
human intervention, except at defined safety gates.

## Strategic Objective

Transition from automated content scheduling to persistent, goal-directed
**Autonomous Influencer Agents** capable of perception, reasoning, creative
expression, and economic agency at scale (1,000+ concurrent agents).

## Core Architectural Pillars

1. **Spec-Driven Development** — No implementation code is written without a
   ratified specification. The `specs/` directory is the source of truth.
2. **FastRender Swarm** — All internal coordination uses the Planner/Worker/Judge
   pattern (SRS §3.1). No monolithic agent logic.
3. **MCP-Only External Integration** — The agent core never calls third-party APIs
   directly. All external interaction flows through MCP Servers (SRS §4.4 FR 4.0).
4. **Java 21+ Idioms** — Virtual Threads for concurrency. Java Records for all DTOs.
   Optimistic Concurrency Control for state management.
5. **Human-in-the-Loop Safety** — Confidence-scored routing: >0.90 auto-approve,
   0.70–0.90 async human review, <0.70 auto-reject. Sensitive topics always escalate.

## Hard Constraints

| Constraint | Rule |
|---|---|
| Language | Java 21+ only. No Python in core agent logic. |
| DTOs | Must use Java Records. No mutable POJOs for agent payloads. |
| External APIs | Must go through MCP layer. Zero direct API calls in agent code. |
| Tests | JUnit 5 only. Tests must exist before implementation (TDD). |
| Concurrency | Virtual Threads (`newVirtualThreadPerTaskExecutor`). No legacy `Thread` class. |
| Financial txns | Must pass CFO Judge gate before Coinbase AgentKit execution. |
| Content publish | Must pass Judge confidence score gate before any social post. |

## Out of Scope (v1.0)

- Mobile application
- Real-time video streaming
- Multi-language persona support (v2 roadmap)
- Decentralised orchestration (v3 roadmap)

## Linked Specifications

- [`specs/functional.md`](./functional.md) — User stories and functional requirements
- [`specs/technical.md`](./technical.md) — API contracts, schemas, ERD
- [`specs/openclaw_integration.md`](./openclaw_integration.md) — OpenClaw network protocol
- [`research/architecture_strategy.md`](../research/architecture_strategy.md) — Architectural decisions
- [`research/tooling_strategy.md`](../research/tooling_strategy.md) — Developer MCP vs runtime MCP vs skills
