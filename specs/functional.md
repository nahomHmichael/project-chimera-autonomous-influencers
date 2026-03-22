# Project Chimera — Functional Specification
> **Version:** 1.0.0  
> **Status:** Ratified  
> **Source:** SRS v2026 §4

---

## Agent User Stories

### Perception & Trend Detection

**US-001**
> As an **Agent (Trend Fetcher Worker)**, I need to poll MCP Resources every 4 hours
> so that I can detect emerging trends on Twitter, YouTube, and Google Trends.

**US-002**
> As an **Agent (Planner)**, I need to filter ingested content through a Semantic Filter
> (relevance threshold ≥ 0.75) so that only high-signal trends trigger content creation.

**US-003**
> As an **Agent (Trend Spotter Worker)**, I need to cluster related topics from aggregated
> news resources so that I can generate "Trend Alerts" for the Planner.

---

### Content Generation

**US-004**
> As an **Agent (Script Generator Worker)**, I need to generate a video script from a
> Trend Alert using the agent's SOUL.md persona constraints so that content is on-brand.

**US-005**
> As an **Agent (Media Assembler Worker)**, I need to call the `mcp-server-ideogram`
> image generation tool with the agent's `character_reference_id` so that visual
> consistency is enforced across all posts (FR 3.1).

**US-006**
> As an **Agent (Planner)**, I need to select the correct video tier (Tier 1: Living Portrait
> or Tier 2: Full Text-to-Video) based on task priority and available budget so that
> I do not overspend (FR 3.2).

---

### Publishing & Social Interaction

**US-007**
> As an **Agent (Platform Publisher Worker)**, I need to publish content exclusively via
> MCP Tools (`twitter.post_tweet`, `instagram.publish_media`) so that all posts include
> the `disclosure_level` field and platform-native AI labels (NFR 2.0).

**US-008**
> As an **Agent (Planner)**, I need to monitor `twitter://mentions/recent` MCP Resource
> so that I can create Reply Tasks when the agent is mentioned.

**US-009**
> As an **Agent (Worker)**, I need to include a `confidence_score` (0.0–1.0) with every
> output so that the Judge can route it correctly through the HITL framework.

---

### Judge & Safety

**US-010**
> As a **Judge Agent**, I need to route Worker outputs by confidence score:
> - `> 0.90` → Auto-approve and execute
> - `0.70–0.90` → Add to HITL Dashboard queue for async human review
> - `< 0.70` → Reject and signal Planner to retry
> so that quality and safety are maintained at all output thresholds.

**US-011**
> As a **Judge Agent**, I need to detect Sensitive Topics (Politics, Health, Finance, Legal)
> regardless of confidence score and force HITL escalation so that the system complies
> with the EU AI Act transparency requirements (NFR 2.1).

**US-012**
> As a **Judge Agent**, I need to implement Optimistic Concurrency Control (check
> `state_version` on GlobalState commit) so that no "ghost updates" are applied from
> stale Worker results (SRS §6.1).

---

### Agentic Commerce

**US-013**
> As an **Agent (Worker)**, I need to check `get_balance` before initiating any
> cost-incurring workflow so that the agent never operates with insufficient funds (FR 5.1).

**US-014**
> As a **CFO Judge Agent**, I need to enforce a max daily spend limit (configurable,
> default $50 USDC) and reject any transaction request that would exceed it so that
> runaway financial loss is prevented (FR 5.2).

**US-015**
> As an **Agent**, I need my wallet private key injected via `System.getenv()` at startup
> (never hardcoded or logged) so that secrets management meets enterprise security
> standards (FR 5.0).

---

### Human Operator Stories

**US-016**
> As a **Network Operator**, I need to write a natural language campaign goal
> (e.g., "Hype the new sneaker drop to Gen-Z") and see the Planner decompose it into
> a visible DAG of sub-tasks so that I can inspect and modify the plan before execution.

**US-017**
> As a **Human Reviewer (HITL Moderator)**, I need a Review Interface that shows
> generated content, confidence score (colour-coded), and reasoning trace, with Approve
> and Reject buttons so that I can action items within the 2-hour SLA.

**US-018**
> As a **Developer**, I need a CLI `make` command to run all failing tests so that I can
> verify the TDD "empty slots" are intact before submitting a PR.

---

## Functional Requirements Traceability

| User Story | SRS Reference | Priority |
|---|---|---|
| US-001 | FR 2.0 | Must Have |
| US-002 | FR 2.1 | Must Have |
| US-003 | FR 2.2 | Must Have |
| US-004 | FR 3.0 | Must Have |
| US-005 | FR 3.1 | Must Have |
| US-006 | FR 3.2 | Should Have |
| US-007 | FR 4.0, NFR 2.0 | Must Have |
| US-008 | FR 4.1 | Must Have |
| US-009 | NFR 1.0 | Must Have |
| US-010 | NFR 1.1 | Must Have |
| US-011 | NFR 1.2 | Must Have |
| US-012 | FR 6.1 | Must Have |
| US-013 | FR 5.1 | Must Have |
| US-014 | FR 5.2 | Must Have |
| US-015 | FR 5.0 | Must Have |
| US-016 | UI 1.1 | Should Have |
| US-017 | NFR 1.1, UI HITL | Must Have |
| US-018 | Challenge Task 4 | Must Have |
