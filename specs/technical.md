# Project Chimera — Technical Specification
> **Version:** 1.1.0  
> **Status:** Ratified  
> **Source:** SRS v2026 §6.2, §3  
> **Changelog:** 1.1.0 — HITL Dashboard operator contract (§6); aligns with `specs/functional.md` US-019 and Judge/OCC models in §1–§4.

---

## 1. Java Record DTOs

All data transfer between Planner, Workers, and Judge MUST use Java Records.
No mutable POJOs or generic Maps for agent payloads (e.g. do not use
`Map<String, Object>` on DTOs; encode opaque structured blobs as JSON strings).

```java
// --- Core Task DTO (matches SRS §6.2 Schema 1) ---
public record AgentTask(
    UUID taskId,
    String taskType,           // "generate_content" | "reply_comment" | "execute_transaction"
    String priority,           // "high" | "medium" | "low"
    TaskContext context,
    String assignedWorkerId,
    Instant createdAt,
    String status              // "pending" | "in_progress" | "review" | "complete"
) {}

public record TaskContext(
    String goalDescription,
    List<String> personaConstraints,
    List<String> requiredResources  // e.g. ["mcp://twitter/mentions/123"]
) {}

// --- Worker Result DTO ---
public record AgentResult(
    UUID taskId,
    UUID workerId,
    boolean success,
    float confidenceScore,     // 0.0 – 1.0
    String payload,            // JSON string of generated content
    int stateVersion           // OCC version at time of task start
) {}

// --- Trend Data DTO ---
// rawMetadataJson: opaque JSON document from MCP (maps to JSONB raw_metadata in persistence)
public record TrendData(
    String platform,
    String topic,
    float viralityScore,
    Instant fetchedAt,
    String rawMetadataJson
) {}

// --- Persona DTO (from SOUL.md) ---
public record AgentPersona(
    UUID id,
    String name,
    List<String> voiceTraits,
    List<String> directives,
    String backstory,
    String characterReferenceId
) {}

// --- Commerce DTO ---
public record TransactionRequest(
    UUID agentId,
    String toAddress,
    float amountUsdc,
    String reason,
    int stateVersion
) {}
```

---

## 2. API Contracts (JSON)

### POST /api/tasks — Create Task
**Request:**
```json
{
  "task_type": "generate_content",
  "priority": "high",
  "context": {
    "goal_description": "Create a TikTok video about trending Ethiopian fashion",
    "persona_constraints": ["Gen-Z tone", "No politics"],
    "required_resources": [
      "mcp://news/ethiopia/fashion/latest",
      "mcp://memory/agent-001/recent"
    ]
  }
}
```
**Response `201`:**
```json
{
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "pending",
  "assigned_worker_id": null,
  "created_at": "2026-03-21T10:00:00Z"
}
```

---

### GET /api/tasks/{taskId}/status
**Response `200`:**
```json
{
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "review",
  "confidence_score": 0.82,
  "hitl_required": true,
  "hitl_reason": "medium_confidence",
  "state_version": 7
}
```

---

### POST /api/judge/review — Submit Human Review Decision (HITL)
**Normative contract:** Field semantics, OCC rules, and audit requirements are defined in **§6 HITL Dashboard**. `decision` MUST be one of `APPROVE` | `REJECT` | `ESCALATE` (see §6.4).

**Request (illustrative):**
```json
{
  "review_id": "880e8400-e29b-41d4-a716-446655440003",
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "decision": "APPROVE",
  "reviewer_id": "human-reviewer-001",
  "expected_state_version": 7,
  "notes": "Looks good, approved for downstream runtime (dashboard does not publish)"
}
```
**Response `200`:**
```json
{
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "review_id": "880e8400-e29b-41d4-a716-446655440003",
  "decision": "APPROVE",
  "committed": true,
  "new_state_version": 8,
  "audit_event_id": "ae-550e8400-20260322T140001Z"
}
```
**Response `409` (OCC conflict):** `expected_state_version` does not match current global state; reviewer must refresh queue item.

---

### POST /api/commerce/transact — Request On-Chain Transaction
**Request:**
```json
{
  "agent_id": "agent-001",
  "to_address": "0xABC123...",
  "amount_usdc": 5.00,
  "reason": "Pay for image generation",
  "state_version": 7
}
```
**Response `200` (CFO approved):**
```json
{
  "approved": true,
  "tx_hash": "0xDEF456...",
  "daily_spend_remaining": 45.00
}
```
**Response `403` (CFO rejected):**
```json
{
  "approved": false,
  "reason": "BudgetExceededException: daily limit $50 USDC would be exceeded",
  "daily_spend_remaining": 2.50
}
```

---

### MCP Tool Definition — post_content (SRS §6.2 Schema 2)
```json
{
  "name": "post_content",
  "description": "Publishes text and media to a connected social platform.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "platform": {
        "type": "string",
        "enum": ["twitter", "instagram", "threads"]
      },
      "text_content": {
        "type": "string",
        "description": "Body of the post."
      },
      "media_urls": {
        "type": "array",
        "items": { "type": "string" }
      },
      "disclosure_level": {
        "type": "string",
        "enum": ["automated", "assisted", "none"]
      },
      "character_reference_id": {
        "type": "string",
        "description": "Enforces visual character consistency (FR 3.1)."
      }
    },
    "required": ["platform", "text_content", "disclosure_level"]
  }
}
```

---

## 3. Database Schema (ERD)

```mermaid
erDiagram
    AGENT_PERSONA {
        uuid id PK
        string name
        string soul_md_path
        string wallet_address
        float daily_budget_usdc
        timestamp created_at
    }
    CAMPAIGN {
        uuid id PK
        uuid agent_id FK
        string goal_description
        string status
        string priority
        timestamp created_at
    }
    TREND {
        uuid id PK
        string platform
        string topic
        float virality_score
        timestamp fetched_at
        jsonb raw_metadata
    }
    CONTENT_JOB {
        uuid id PK
        uuid campaign_id FK
        uuid trend_id FK
        string task_type
        string status
        float confidence_score
        string rejection_reason
        int state_version
        timestamp created_at
        timestamp approved_at
    }
    VIDEO_ASSET {
        uuid id PK
        uuid job_id FK
        string script_text
        string media_url
        string platform_target
        string publish_status
        string disclosure_level
        timestamp published_at
    }
    TRANSACTION_LOG {
        uuid id PK
        uuid agent_id FK
        string to_address
        float amount_usdc
        string tx_hash
        string status
        timestamp executed_at
    }
    AGENT_LOG {
        uuid id PK
        uuid job_id FK
        string agent_role
        string event_type
        jsonb payload
        timestamp recorded_at
    }

    AGENT_PERSONA ||--o{ CAMPAIGN : "runs"
    CAMPAIGN ||--o{ CONTENT_JOB : "generates"
    TREND ||--o{ CONTENT_JOB : "inspires"
    CONTENT_JOB ||--o{ VIDEO_ASSET : "produces"
    CONTENT_JOB ||--o{ AGENT_LOG : "records"
    AGENT_PERSONA ||--o{ TRANSACTION_LOG : "executes"
```

---

## 4. Redis Key Schema

| Key Pattern | Type | TTL | Purpose |
|---|---|---|---|
| `task_queue` | Redis Stream | None | Planner → Worker task distribution |
| `review_queue` | Redis Stream | None | Worker → Judge result queue |
| `agent:{id}:state` | Hash | None | GlobalState per agent (includes `state_version`) |
| `agent:{id}:memory:short` | List | 1 hour | Episodic short-term memory |
| `agent:{id}:daily_spend` | String (float) | 24 hours | CFO daily spend tracker |
| `trend:cache:{platform}` | Hash | 4 hours | Raw trend data cache |
| `hitl:queue` | Sorted Set | None | HITL pending items (score = timestamp) |

---

## 5. Non-Functional Requirements Targets

| NFR | Target | SRS Ref |
|---|---|---|
| Concurrent agents | ≥ 1,000 without Orchestrator degradation | NFR 3.0 |
| Interaction latency | ≤ 10 seconds (DM reply, end-to-end, excl. HITL) | NFR 3.1 |
| HITL SLA | Human action within 2 hours; escalate after 4 hours | NFR 1.1 |
| Budget cap | Max $50 USDC/day per agent (configurable) | FR 5.2 |
| Sensitive topic | 100% escalation to HITL, zero auto-publish | NFR 1.2 |

---

## 6. Human-in-the-Loop Dashboard — Technical Contract

This section specifies the **operator control plane** that consumes Judge-routed work and emits **durable review decisions**. It is **downstream of** the FastRender swarm boundary: **Worker → `review_queue` → Judge** establishes routing; the dashboard **does not** participate in Worker execution or MCP tool invocation for publish.

**User story:** `specs/functional.md` **US-019**. **Architecture:** `research/architecture_strategy.md` §4 (HITL flow, SLA).

### 6.1 Purpose

- Present **only** items the **Judge** has placed in the HITL queue (`hitl:queue` per §4, or equivalent persistent projection).
- Accept **APPROVE**, **REJECT**, or **ESCALATE** from an authenticated **Human Reviewer** role.
- Persist an **audit-grade** decision record and drive **Chimera-internal** state transitions (commit, retry, escalation handoff)—**never** direct social publish or third-party API calls from the dashboard tier.

### 6.2 Trigger conditions (queue membership)

A **HITL review item** MUST be materialized when **any** of the following holds after Judge evaluation of an `AgentResult` (§1):

| Condition | Rule |
|---|---|
| **Medium-confidence band** | `confidenceScore` ∈ **[0.70, 0.90]** (inclusive bounds as in NFR 1.1 / US-010). |
| **Sensitive topic** | `policyFlags` indicate mandatory HITL (US-011), **independent of** confidence. |
| **Operational escalation** | System policy re-queues a previously approved item for re-review (optional; if used, new `reviewId`). |

Items with **confidenceScore < 0.70** without a sensitive-topic flag MUST **not** occupy the HITL dashboard queue for “approve-to-publish” purposes; they follow automated reject/retry. Items with **confidenceScore > 0.90** MUST **not** appear **unless** US-011 (or equivalent policy) forces HITL.

### 6.3 Review item contract (logical schema)

Each queue entry MUST be representable as a JSON object with at least:

| JSON field | Type | Required | Description |
|---|---|---|---|
| `review_id` | UUID | yes | Primary key for this review episode. |
| `task_id` | UUID | yes | Correlates to `AgentTask.taskId` / pipeline job. |
| `agent_id` | UUID | yes | Owning agent / persona. |
| `content_summary_or_preview` | string | yes | Sanitized summary or truncated preview; not an executable payload. |
| `confidence_score` | number | yes | **Float in [0.0, 1.0]**; aligns with `AgentResult.confidenceScore`. |
| `policy_flags` | string or object | yes | Structured policy signals; if object-shaped on the wire, treat as a documented sub-schema; **Java DTOs** SHOULD use a JSON string for opaque flag bags (§1). |
| `created_at` | string (RFC 3339) | yes | Enqueue time. |
| `state_version` | integer | yes | **Expected** global OCC version when the item was enqueued; reviewer submits `expected_state_version` matching this (or current) per §6.5. |

**Example — GET /api/hitl/queue (illustrative):**
```json
{
  "items": [
    {
      "review_id": "880e8400-e29b-41d4-a716-446655440003",
      "task_id": "550e8400-e29b-41d4-a716-446655440000",
      "agent_id": "770e8400-e29b-41d4-a716-446655440002",
      "content_summary_or_preview": "TikTok script: Ethiopian street fashion — 320 chars truncated…",
      "confidence_score": 0.82,
      "policy_flags": "{\"sensitive_topic\":false,\"disclosure\":\"assisted\"}",
      "created_at": "2026-03-22T13:00:00Z",
      "state_version": 7
    }
  ]
}
```

### 6.4 Review decision contract

Each reviewer submission MUST include:

| JSON field | Type | Required | Description |
|---|---|---|---|
| `review_id` | UUID | yes | Target queue item. |
| `task_id` | UUID | yes | Must match the item's `task_id`. |
| `decision` | string enum | yes | `APPROVE` \| `REJECT` \| `ESCALATE`. |
| `reviewer_id` | string | yes | Stable operator identity (subject from IdP or service account). |
| `expected_state_version` | integer | yes | OCC guard: see §6.5. |
| `notes` | string | no | Free text for audit; MUST NOT contain secrets. |
| `decided_at` | string (RFC 3339) | no | Server MAY set if omitted. |

**Semantics:**

- **APPROVE** — Authorize runtime to proceed (e.g. mark job approved, enqueue publish **through existing Chimera/MCP paths**). Dashboard response MUST NOT imply that HTTP response body equals “posted to platform.”
- **REJECT** — Deny output; runtime MUST transition to reject/retry consistent with US-010 (Planner signal, no auto-publish).
- **ESCALATE** — Hand off to tier-2 process; item leaves default reviewer queue or changes ownership; **no** publish authorization implied.

### 6.5 `state_version` validation (OCC)

- The control plane MUST compare `expected_state_version` on submit against **`GlobalState` / `CONTENT_JOB.state_version`** (or authoritative source defined at implementation time).
- **Match:** persist decision, bump `state_version` monotonically, append **audit event** (§6.6).
- **Mismatch:** respond **`409 Conflict`**; **no** side effect on publish or global commit; reviewer refreshes item.

This mirrors **US-012** / `AgentResult.stateVersion` discipline across the Planner / Worker / Judge boundary.

### 6.6 Auditability requirements

- Every decision MUST append an **append-only audit record** containing at minimum: `audit_event_id`, `review_id`, `task_id`, `agent_id`, `decision`, `reviewer_id`, `expected_state_version`, `new_state_version` (if any), `decided_at`, and redacted `notes`.
- Audit records MUST be queryable for compliance (EU AI Act transparency alignment per US-011 / NFR 2.1).
- Corrections MUST **not** mutate prior audit rows; use compensating events if policy allows reversal.

**Implementation hint:** `AGENT_LOG` or dedicated `HITL_AUDIT` table (ERD extension left to implementation); `event_type` e.g. `hitl_decision`.

### 6.7 Non-goals

- **Product UX:** Pixel-perfect consumer UI, marketing sites, or mobile apps.
- **Direct publish:** Any API or UI action that calls Twitter/Instagram/etc. from the dashboard process.
- **Arbitrary integrations:** Webhooks to unapproved third parties, embedded LLM calls in the dashboard tier for content rewriting, or vendor SDKs in the dashboard codebase.
- **Substituting Judge:** Operators cannot bypass Judge routing rules or force auto-publish for sub-threshold confidence without a **new** Judge-evaluated artifact.
- **Financial execution:** On-chain or budget mutation from the dashboard; **CFO Judge** paths remain separate (§2 commerce APIs).

---
