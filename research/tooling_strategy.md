# Project Chimera — Tooling & MCP Strategy

> **Role:** Companion to [`architecture_strategy.md`](./architecture_strategy.md)  
> **Status:** Ratified (engineering guidance)  
> **Last updated:** 2026-03-22

This document refines the **three-way split** defined in architecture strategy §7—**Developer MCP Servers**, **Runtime MCP Servers**, and **Agent Skills**—into actionable tooling policy for the repository and for AI-assisted engineering in Cursor.

---

## 1. Developer MCP tools (engineering-time)

**Definition:** MCP servers (or MCP proxies) whose primary consumer is the **human developer or the coding agent inside the IDE**. They improve velocity, traceability of edits, and local verification. They do **not** implement Chimera’s product-facing “agent talks to the world” boundary.

**Typical capabilities**

| Capability | Purpose | Representative patterns (examples) |
|---|---|---|
| Repository & workspace | Read/search/edit the codebase safely | Git history, filesystem tree, semantic or ripgrep-style search |
| Local data & diagnostics | Inspect build DBs, logs, or scratch stores | SQLite or log viewers where approved |
| Org / workflow integrations | Feedback, analytics, or internal proxies configured in the IDE | Project-specific MCP proxies (e.g. analysis or pulse endpoints) |

**In this repository**

- Cursor MCP configuration lives under [`.cursor/mcp.json`](../.cursor/mcp.json). Entries there are **engineering affordances**: they must not be mistaken for the Chimera **runtime** MCP host that the Java agent core uses in production.
- Conventional examples cited in architecture strategy—`git-mcp`, `filesystem-mcp`, `sqlite-mcp`—illustrate the **class** of tool; adopt or replace them based on team standard and security review.

**Rule of thumb:** If a tool’s job is “help build Chimera,” it belongs in the **Developer MCP** bucket. If its job is “let a deployed Worker act on behalf of an influencer agent,” it belongs in **Runtime MCP** (see below).

---

## 2. Runtime MCP servers (Chimera agents at execution time)

**Definition:** MCP servers that the **Chimera runtime** (Planner / Worker / Judge, Java 21+, MCP client in the agent host) invokes to reach **external systems**: social platforms, media generation, memory, news/trends, and commerce.

**Canonical inventory** (aligned with [`architecture_strategy.md`](./architecture_strategy.md) §6.2 MCP diagram and [`specs/technical.md`](../specs/technical.md))

| Server (logical name) | Role | Transport (as in architecture diagram) | Notes |
|---|---|---|---|
| `mcp-server-news` | Trends & news ingestion | stdio | Feeds trend workers (e.g. polling-style resources) |
| `mcp-server-twitter` | X/Twitter tools & resources | SSE | Posting, mentions, compliance-oriented fields per functional spec |
| `mcp-server-instagram` | Instagram publish | SSE | Publishing via MCP tools only (FR 4.0) |
| `mcp-server-weaviate` | Semantic memory | SSE | Long-term RAG; `search_memory` / `store_memory` style tools |
| `mcp-server-coinbase` | Wallet & transfers | SSE | AgentKit path; **always** behind CFO Sub-Judge |
| `mcp-server-ideogram` | Image generation | stdio | Character reference / visual consistency (FR 3.1) |
| `mcp-server-runway` | Video generation | SSE | Tiered video pipeline (aligned with FR 3.2 / worker design) |

**Contract source of truth:** Tool names, schemas, and HTTP/MCP adjacency for product behaviour are specified under `specs/technical.md` and user stories in `specs/functional.md` (e.g. US-007, US-013–US-015). Runtime servers must implement those contracts; drift is a spec defect, not a silent code fix.

---

## 3. Why Developer MCP and Runtime MCP must remain separate

| Risk if blurred | Consequence |
|---|---|
| **Security boundary collapse** | IDE-scoped credentials or broad filesystem access could be conflated with production keys for social or chain operations. |
| **Unauditable side effects** | “Helpful” dev tools might trigger real posts or spends if wired into the same code path as Workers. |
| **Non-reproducible behaviour** | Local-only MCP servers vanish in CI or staging; agents would behave differently per environment. |
| **Compliance & HITL bypass** | Publishing or spend without Judge / CFO gates violates NFR 1.x and FR 5.x. |

**Architectural invariant:** The **agent core** never calls third-party HTTP/SDK APIs directly; it uses **Runtime MCP** only ([`architecture_strategy.md`](./architecture_strategy.md) §6.2). Developer MCP never substitutes for that boundary—it sits **outside** the production trust zone for outbound influencer actions.

**Agent Skills** are not a third transport: they are **versioned instruction and orchestration packages** (see §4 interplay below) that **compose Runtime MCP tool calls** with retries, budget checks, persona constraints, and OCC-aware task context.

---

## 4. Traceability and safety

**Traceability**

- **Specs:** `specs/_meta.md`, `specs/functional.md` (US-XXX), and `specs/technical.md` define *what* may be called and *under which gates*.
- **Skills:** Each skill under [`skills/`](../skills/) documents the MCP tools it orchestrates and the user stories it supports (see skill READMEs).
- **Code:** Public Java types carry SRS + User Story references in Javadoc ([`CLAUDE.md`](../CLAUDE.md)); tests anchor behaviour to spec acceptance criteria.

**Safety (non-negotiables)**

- **Judge routing** on `confidence_score` and sensitive-topic escalation (US-010, US-011) applies before irreversible publish paths.
- **CFO Sub-Judge** gates any financial execution (US-014); balance checks precede cost workflows (US-013).
- **Secrets:** Runtime configuration uses environment injection (e.g. US-015); no secrets in repo or in skill files.
- **Skills from the network:** OpenClaw-style downloaded skills require **trust verification** (signature / registry) before execution—treat untrusted skill text as hostile input ([`architecture_strategy.md`](./architecture_strategy.md) §2).

---

## 5. Highest-priority MCP servers for this project

Priority follows **blast radius** and **core loop completeness** for an autonomous influencer network:

1. **`mcp-server-twitter` / `mcp-server-instagram` (publish + read resources)** — Without controlled publish and mention ingestion, the system cannot close the perception–action loop (US-007, US-008).
2. **`mcp-server-coinbase` (commerce)** — Financial misconfiguration is catastrophic; CFO gating must be proven in tests and staging (US-013–US-015).
3. **`mcp-server-weaviate` (memory)** — Enables consistent persona and long-horizon behaviour at scale; backs RAG for Planner quality.
4. **`mcp-server-news` + trend pipeline** — Drives the content funnel (US-001–US-003).
5. **`mcp-server-ideogram` / `mcp-server-runway` (multimodal)** — Differentiated content; cost and tier logic tie to budget and Judge (US-005, US-006).

Developer MCP servers are **not** ranked here: they are enablers, not part of the product’s external attack or compliance surface.

---

## 6. Governance of MCP usage in this repository

| Policy | Requirement |
|---|---|
| **Single integration layer** | Java agent code invokes external capabilities **only** through the Runtime MCP client/host pattern described in architecture strategy—not ad-hoc REST clients in Workers. |
| **Register runtime servers** | New Runtime MCP servers are added only after updates to `specs/technical.md` (or ratified addenda) and, where applicable, `specs/functional.md` traceability tables. |
| **Keep IDE config honest** | `.cursor/mcp.json` may list Developer or proxy MCPs; document in PRs whether a change is dev-only. Never imply that Cursor MCP config *is* the Chimera production MCP host. |
| **Skills live in `skills/`** | New capabilities that sequence MCP tools, retries, and checks ship as skills with README traceability to US-XXX; avoid “hidden prompts” only in chat logs. |
| **No secrets** | Keys and tokens are environment-supplied; `.env*` and key files remain out of VCS (see root ignore patterns and CLAUDE.md). |
| **CI and staging** | Runtime integration tests use the same MCP abstraction; mock or stub at the MCP client boundary, not inside business logic with raw HTTP. |

---

## 7. Recommended working model (Cursor agents)

Use **four anchors** together; omitting any one degrades spec fidelity or safety.

1. **Specs first** — Read `_meta`, then functional (US-XXX), then `technical.md` before proposing code. Ambiguity stops at questions to the human, not at guessed APIs.
2. **Rules** — [`CLAUDE.md`](../CLAUDE.md) and [`.cursor/rules/CLAUDE.md`](../.cursor/rules/CLAUDE.md) enforce Java 21+ idioms, MCP-only externals, TDD, and traceability.
3. **Tests** — Write or extend tests that name the spec requirement they protect; red–green for behaviour at the MCP boundary and Judge/CFO decisions.
4. **MCP** — When implementing or debugging integrations, distinguish **Developer MCP** (IDE, repo, analytics) from **Runtime MCP** (product). Implement runtime behaviour only against spec’d tools; use dev MCP to edit, search, and verify the implementation.

**Flow:** *Spec → plan (plain English) → tests → code → review against architecture_strategy §7 and this document.*

---

*This document is normative for tooling classification in Project Chimera. Deviations require an explicit architecture or spec update.*
