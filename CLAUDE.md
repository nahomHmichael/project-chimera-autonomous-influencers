# Project Chimera — AI Agent Context & Prime Directives

## Project Context

This is **Project Chimera**, an Autonomous Influencer Network built on the
**FastRender Swarm Architecture** (Planner/Worker/Judge pattern) using **Java 21+**
and the **Model Context Protocol (MCP)** for all external integrations.

The codebase is structured around Spec-Driven Development. The `specs/` directory
is the source of truth for all behaviour. The `skills/` directory defines agent runtime
capabilities. The `research/` directory contains architectural decisions.

## PRIME DIRECTIVE

**NEVER generate implementation code without first reading the relevant spec in `specs/`.**

Before writing any class, method, or test:
1. Read `specs/_meta.md` for hard constraints
2. Read `specs/functional.md` for the relevant user story
3. Read `specs/technical.md` for the exact DTO, API contract, or schema
4. Explain your implementation plan BEFORE writing any code

## Java-Specific Directives

- **Records ONLY** for all DTOs. Never use mutable POJOs or generic `Map<String, Object>`
  for agent payloads. Example: `public record AgentTask(...) {}`
- **Virtual Threads ONLY** for concurrency: `Executors.newVirtualThreadPerTaskExecutor()`
  Never use `new Thread()`, `ExecutorService.newFixedThreadPool()`, or legacy thread pools.
- **JUnit 5 ONLY** for all tests. Use `@ExtendWith(MockitoExtension.class)`.
- **No direct API calls** in agent logic. All external calls go through the MCP Client layer.
- Java 21+ idioms: use pattern matching, switch expressions, sealed classes where appropriate.

## Architecture Rules

- Planner pushes `AgentTask` records to Redis `task_queue` (Redis Stream)
- Workers pop from `task_queue`, produce `AgentResult` records, push to `review_queue`
- Judge pops from `review_queue`, validates OCC `state_version`, routes by `confidence_score`
- CFO Sub-Judge validates ALL `TransactionRequest` records before Coinbase AgentKit calls
- HITL threshold: `> 0.90` auto-approve, `0.70–0.90` async human, `< 0.70` reject/retry

## Traceability Rule

Every generated class MUST include a Javadoc comment referencing its SRS section.
Example: `/** Planner Service — SRS §3.1.1 / US-001, US-002 */`

## What NOT to do

- Do NOT use `Thread.sleep()` in production code (only allowed in stub/mock tests)
- Do NOT call Twitter/Instagram/Coinbase APIs directly — use MCP Tools
- Do NOT hardcode API keys — always use `System.getenv()`
- Do NOT write tests after implementation — tests define the contract first (TDD)
