# Project Chimera — OpenClaw / Agent Social Network Integration
> **Version:** 1.0.0  
> **Status:** Ratified (bonus specification)  
> **Last Updated:** 2026-03-22  
> **References:** `research/architecture_strategy.md` §1.2–§2, §6–§7; `specs/_meta.md`; `specs/technical.md`; SRS v2026 (MCP-only external I/O, Planner/Worker/Judge)

---

## 1. Purpose of the integration

Project Chimera is a **node** in a broader autonomous-agent ecosystem (OpenClaw-style agent social networks). This document defines how Chimera **publishes availability and operational status** outward so other agents, orchestrators, and registries can discover what the fleet is doing, what it has produced, and whether it is safe to collaborate.

**In-repo alignment:**

- Internal execution remains **Planner → `task_queue` → Workers → `review_queue` → Judge** (`research/architecture_strategy.md` §3).
- **OpenClaw-facing messages are emitted only after Chimera has a stable, auditable fact** (e.g. post-Judge routing decision, or a defined failure path), not from raw Worker stdout.
- **Publishing transport is never implemented as direct HTTP/SDK calls from agent core Java.** All network I/O to OpenClaw registries, Submolts, or mirrors flows through **Runtime MCP tools** (see §10).

**Roles Chimera plays on the network** (from architecture strategy): Content Producer, Status Broadcaster, Skill Consumer, Reputation Participant, Economic Agent. This spec focuses on **Status Broadcaster**, **Content Manifest** (for remix/discovery), **Skill handshake** (inbound skills), and **Error broadcast** (orchestrator rerouting).

---

## 2. Status model and heartbeat contract

### 2.1 Status enumeration

Heartbeat payloads use a single primary `agent_status` aligned with architecture strategy §2:

| Value | Meaning | Typical internal correlate |
|--------|---------|----------------------------|
| `IDLE` | No active campaign task bound to publish path | Planner idle or queue drained |
| `RESEARCHING` | Trend / context gathering in flight | Trend Fetcher or MCP resource phase |
| `GENERATING` | Script / media assembly in flight | Script or Media Worker active |
| `AWAITING_APPROVAL` | Output in Judge/HITL path | `review_queue` or HITL queue (`specs/technical.md` §2) |

Optional `sub_status` (string, registry-defined vocabulary) may refine the state without breaking consumers (e.g. `sub_status`: `"mcp_poll"`, `"hitl_dashboard"`).

### 2.2 Heartbeat semantics

- **Cadence:** Default **15 minutes** for availability heartbeats; **immediate** emit on transitions `IDLE` ↔ `RESEARCHING` ↔ `GENERATING` ↔ `AWAITING_APPROVAL` and on terminal outcomes that affect collaboration (publish committed, hard failure).
- **Correlation:** Every heartbeat includes `agent_id` (UUID, matches `AGENT_PERSONA.id` / technical ERD) and `sequence` (monotonic **64-bit integer per agent**, persisted in PostgreSQL or Redis alongside `state_version` patterns).
- **Freshness:** Receivers should treat heartbeats older than **2× cadence** as **stale** unless superseded by a signed snapshot (§6).
- **OCC hint:** Include `state_version` from `GlobalStateSnapshot` / `AgentResult` model (`specs/technical.md` §1) so external orchestrators can detect that Chimera’s view of campaign state may have moved on.

### 2.3 Required heartbeat fields (logical contract)

| Field | Type | Required | Notes |
|--------|------|----------|--------|
| `schema_version` | string (semver) | yes | e.g. `"1.0.0"` |
| `message_type` | string | yes | constant `"chimera.heartbeat"` |
| `agent_id` | uuid | yes | |
| `sequence` | integer | yes | strictly increasing per agent |
| `emitted_at` | string (RFC 3339) | yes | UTC |
| `agent_status` | enum string | yes | §2.1 |
| `state_version` | integer | yes | OCC snapshot at emit time |
| `confidence_summary` | object | optional | Last known Judge-facing score band (not raw secrets) |
| `capabilities` | array of string | yes | Logical skill ids (e.g. `skill_fetch_trends`) Chimera advertises |

---

## 3. Content manifest contract

A **content manifest** describes machine-readable metadata about **approved or candidate** outputs so other agents can reference, rank, or remix work without receiving full media blobs in the manifest itself.

**Grounding:**

- Ties to `CONTENT_JOB`, `VIDEO_ASSET`, and `TREND` entities (`specs/technical.md` §3).
- `confidence_score` and `state_version` mirror Judge/OCC fields.
- Large or binary assets are referenced by **URI** (HTTPS or MCP resource URI style per technical examples), not embedded opaque maps—aligns with “JSON strings for opaque blobs” on DTOs; manifests stay structured JSON.

### 3.1 Manifest lifecycle

| Phase | When emitted | Consumer expectation |
|--------|----------------|----------------------|
| `draft` | Worker output landed in review path | May change or be rejected |
| `candidate` | Judge score ≥ 0.70 but HITL pending | Not publicly final |
| `approved` | Judge path committed (auto or HITL) | Durable reference for remix/citation |
| `published` | MCP publish tool succeeded | Includes platform identifiers |
| `withdrawn` | Compliance / human revoke | Must not be used for new automation |

### 3.2 Required manifest fields (logical contract)

| Field | Type | Required | Notes |
|--------|------|----------|--------|
| `schema_version` | string | yes | |
| `message_type` | string | yes | `"chimera.content_manifest"` |
| `manifest_id` | uuid | yes | New id per manifest revision |
| `agent_id` | uuid | yes | |
| `content_job_id` | uuid | yes | Links to `CONTENT_JOB` |
| `phase` | enum string | yes | §3.1 |
| `emitted_at` | string (RFC 3339) | yes | |
| `state_version` | integer | yes | At commit or draft snapshot |
| `confidence_score` | number | yes | 0.0–1.0 when known |
| `topic` | string | yes | Human-readable theme |
| `trend_refs` | array | optional | Stable ids or URIs to trend sources |
| `formats` | array of string | yes | e.g. `["script","video","image"]` |
| `platform_targets` | array of string | optional | e.g. `twitter`, `instagram` |
| `asset_refs` | array of object | yes | `{ "role", "uri", "content_type" }` |
| `disclosure_level` | string | yes | Matches MCP `post_content` enum (`specs/technical.md` §2) |
| `payload_digest` | string | optional | SHA-256 of canonical JSON payload for dedup |

---

## 4. Skill handshake / trust model

Chimera **consumes** downloadable skill definitions from the OpenClaw ecosystem (`research/architecture_strategy.md` §1.2). In-repo, executable workflows are **`com.chimera.skill.Skill` implementations** registered via SPI (`skills/` README contracts, `specs/technical.md` / architecture §7).

### 4.1 Trust tiers

| Tier | Meaning | Chimera behaviour |
|------|---------|-------------------|
| `registry` | Skill published by configured OpenClaw registry with signature | Eligible for automatic execution inside Judge budget/OCC rules |
| `community` | Unsigned or third-party URL | **Blocked** from execution unless HITL explicitly approves skill install |
| `local` | Bundled in repo / build | Trusted as code review + supply chain |

### 4.2 Handshake steps (logical)

1. **Resolve** skill id + version requirement (semver range) from manifest or orchestrator directive.
2. **Fetch** skill package **only through MCP** (e.g. filesystem fetch tool, approved HTTP gateway MCP—not raw `HttpClient` in core).
3. **Verify** package integrity (§6): signature, digest, optional transparency log anchor if registry provides it.
4. **Register** SPI or in-memory adapter so Planner references **skill id** not arbitrary file paths.
5. **Execute** only after CFO/Judge gates if the skill triggers spend or publish side effects.

### 4.3 Refusal signals

If verification fails, Chimera emits an **error broadcast** (§5) with `error_code`: `SKILL_TRUST_FAILURE` and does not load instruction text into the LLM context.

---

## 5. Error broadcast structure

When a task fails in a way that **external orchestrators** should observe (MCP outage, budget block, OCC conflict, skill trust failure), Chimera publishes a structured **error broadcast**.

### 5.1 Required fields

| Field | Type | Required | Notes |
|--------|------|----------|--------|
| `schema_version` | string | yes | |
| `message_type` | string | yes | `"chimera.error_broadcast"` |
| `agent_id` | uuid | yes | |
| `sequence` | integer | yes | Shares monotonic sequence with heartbeats or separate `error_sequence`—choose one per deployment and document |
| `emitted_at` | string (RFC 3339) | yes | |
| `severity` | enum | yes | `warning`, `error`, `critical` |
| `error_code` | string | yes | Stable machine code (see §5.2) |
| `task_id` | uuid | optional | If tied to `AgentTask.taskId` |
| `worker_role` | string | optional | e.g. `"Trend Fetcher Worker"` |
| `state_version` | integer | optional | If OCC-related |
| `retryable` | boolean | yes | Hint for orchestrator |
| `safe_message` | string | yes | No secrets, no PII |
| `details_ref` | string | optional | Opaque reference to internal `AGENT_LOG` row or object store |

### 5.2 Suggested `error_code` values

| Code | Meaning |
|------|---------|
| `MCP_UNAVAILABLE` | MCP server down or timeout |
| `OCC_CONFLICT` | `state_version` mismatch / commit rejected |
| `BUDGET_EXCEEDED` | CFO / `BudgetExceededException` path |
| `JUDGE_REJECT` | Confidence below threshold / policy |
| `SKILL_TRUST_FAILURE` | Signature or registry check failed |
| `HITL_TIMEOUT` | Item exceeded SLA without resolution |

### 5.3 Internal audit mirror

Each broadcast should have a matching **`AGENT_LOG`** entry (`event_type`: `openclaw_error_broadcast`, `payload` JSONB) for traceability (`specs/technical.md` §3).

---

## 6. Security and signature validation concepts

- **Signing key:** Held outside the LLM context—e.g. orchestrator KMS, HSM, or deployment secret injected as env (consistent with `_meta.md` / architecture: no secrets in prompts).
- **Canonical form:** JSON payloads are serialized in **stable key order** before signing; `payload_digest` in manifests uses the same canonicalization.
- **Algorithm:** Ed25519 or P-256 ECDSA with **detached signature** over `payload_digest` (registry policy chooses; Chimera declares supported algs in `capabilities`).
- **Verification:** Before treating inbound skills or registry metadata as trusted, verify signature against **pinned registry keys** rotated on a published schedule.
- **Prompt-injection guard:** Never pass unsigned remote markdown into system instructions without HITL or `registry` tier success (architecture §1.2).

---

## 7. Retry and resilience behaviour

| Scenario | Policy |
|----------|--------|
| MCP transient failure | Exponential backoff with jitter (base 1s, max 5 attempts) for **heartbeat** only; task-level retries delegated to Planner/Judge |
| Publish manifest after HITL | At-least-once emit; consumers dedupe on `manifest_id` + `payload_digest` |
| Sequence gaps | Consumers may request **reconciliation** via registry MCP tool (out of band to this spec) |
| Network partition | Heartbeats stop; internal Redis streams retain backlog; on reconnect emit `agent_status` with `state_version` bump note |
| CFO rejection | Single error broadcast; no unbounded retry of spend |

Virtual-thread workers remain the **internal** concurrency model (`research/architecture_strategy.md` §6); OpenClaw I/O runs on MCP-isolated paths so retries do not block the Judge gate.

---

## 8. Traceability and audit requirements

- Every outward message includes **`agent_id`**, **`sequence`**, **`emitted_at`**, and **`schema_version`**.
- Javadoc and tests should reference **SRS sections** and **US-XXX** when OpenClaw emitters are implemented (per `_meta.md` and CodeRabbit policy).
- Persist sufficient data to answer: *Who emitted? On behalf of which campaign? Under which `state_version`? Which MCP tool carried the bytes?*
- HITL and sensitive-topic overrides (`architecture_strategy.md` §4) must be reflected in manifest `phase` and optional `hitl_reason` string **without** leaking reviewer PII.

---

## 9. Example JSON payloads

### 9.1 Heartbeat

```json
{
  "schema_version": "1.0.0",
  "message_type": "chimera.heartbeat",
  "agent_id": "770e8400-e29b-41d4-a716-446655440002",
  "sequence": 18442,
  "emitted_at": "2026-03-22T14:00:00Z",
  "agent_status": "GENERATING",
  "sub_status": "media_assembly",
  "state_version": 7,
  "confidence_summary": {
    "last_task_confidence": 0.84,
    "last_routing": "HITL_QUEUE"
  },
  "capabilities": [
    "skill_fetch_trends",
    "skill_generate_content",
    "skill_post_content"
  ]
}
```

### 9.2 Content manifest (approved)

```json
{
  "schema_version": "1.0.0",
  "message_type": "chimera.content_manifest",
  "manifest_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "agent_id": "770e8400-e29b-41d4-a716-446655440002",
  "content_job_id": "550e8400-e29b-41d4-a716-446655440000",
  "phase": "approved",
  "emitted_at": "2026-03-22T14:05:00Z",
  "state_version": 8,
  "confidence_score": 0.92,
  "topic": "Ethiopian fashion trends",
  "trend_refs": ["mcp://news/ethiopia/fashion/latest"],
  "formats": ["script", "video"],
  "platform_targets": ["tiktok"],
  "asset_refs": [
    {
      "role": "script",
      "uri": "https://cdn.example.invalid/chimera/jobs/550e8400/script.json",
      "content_type": "application/json"
    },
    {
      "role": "video",
      "uri": "https://cdn.example.invalid/chimera/jobs/550e8400/final.mp4",
      "content_type": "video/mp4"
    }
  ],
  "disclosure_level": "automated",
  "payload_digest": "sha256:2c26b46b68ffc68ff99b453c1d3041340812d637e6f6d929791fe8c08c4a225a"
}
```

### 9.3 Error broadcast

```json
{
  "schema_version": "1.0.0",
  "message_type": "chimera.error_broadcast",
  "agent_id": "770e8400-e29b-41d4-a716-446655440002",
  "sequence": 18443,
  "emitted_at": "2026-03-22T14:06:12Z",
  "severity": "error",
  "error_code": "OCC_CONFLICT",
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "worker_role": "Platform Publisher Worker",
  "state_version": 7,
  "retryable": true,
  "safe_message": "Publish skipped: optimistic concurrency conflict with global campaign state.",
  "details_ref": "agentlog:9f8e7d6c-5b4a-3210-fedc-ba9876543210"
}
```

### 9.4 Skill handshake (verification record — optional publish)

```json
{
  "schema_version": "1.0.0",
  "message_type": "chimera.skill_handshake",
  "agent_id": "770e8400-e29b-41d4-a716-446655440002",
  "sequence": 18440,
  "emitted_at": "2026-03-22T13:58:00Z",
  "skill_id": "skill_fetch_trends",
  "skill_version": "1.2.0",
  "trust_tier": "registry",
  "package_digest": "sha256:abc123...",
  "signature_alg": "ed25519",
  "registry_key_id": "openclaw-prod-2026-03"
}
```

---

## 10. Alignment with MCP-only integration principles

Per `specs/_meta.md` and `research/architecture_strategy.md` §6:

| Rule | Application to OpenClaw |
|------|-------------------------|
| No direct third-party APIs from agent core | Heartbeats, manifests, errors, and skill fetches are sent/received **only** via **Runtime MCP tools** (e.g. `openclaw.publish_status`, `openclaw.publish_manifest`, `openclaw.fetch_skill_package`) defined in MCP server manifests—not via embedded SDK calls in `com.chimera` workers. |
| Judge / CFO gates | No `published` manifest and no outward **approved** narrative may bypass Judge confidence rules or CFO budget rules. |
| DTO boundary | Java Records (`AgentResult`, `TrendData`, etc.) hold structured data **inside** Chimera; wire formats are JSON **documents** built in a thin adapter that calls MCP—no `Map<String,Object>` agent DTOs. |
| Skills vs MCP | Skills orchestrate MCP tools; OpenClaw **social protocol** documents are either **consumed as skills** (with handshake §4) or **emitted as JSON** through MCP publish tools. |

---

*This bonus specification extends the ratified architecture. Changes here should be reflected in `research/architecture_strategy.md` if network roles or MCP boundaries shift.*
