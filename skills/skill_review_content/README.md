# Skill: `skill_review_content`

> **Version:** 1.0.0  
> **Category:** Judge, HITL routing, and safety gates  
> **Status:** Contract only (no runtime implementation in this repo yet)

---

## 1. Purpose

Evaluate a Worker’s **content-bearing result** (e.g. from `skill_generate_content` or Publisher-pending packages), compute or refine **routing**, enforce **sensitive-topic escalation**, and produce a **Judge decision record** compatible with OCC—so the orchestrator can auto-execute, enqueue **HITL**, or **reject + retry** without ambiguous side effects.

---

## 2. Role in the Planner / Worker / Judge system

| Phase | Role |
|---|---|
| **Planner** | Receives **retry** signals when routing is `REJECT_RETRY`; may replan DAG or adjust constraints. |
| **Worker** | **Not** the executor of this skill; Workers supply inputs via `review_queue` (`AgentResult`). |
| **Judge** | Primary consumer: synchronous gate before auto-publish or ledger commits; applies US-010, US-011, US-012; feeds HITL dashboard (US-017). |

**CFO Sub-Judge** remains a **separate** financial gate (US-014); this skill may **flag** commerce relevance but must not authorize on-chain execution.

---

## 3. Inputs

| Field | Type | Required | Description |
|---|---|---|---|
| `task_id` | UUID | Yes | Task under review. |
| `agent_id` | string | Yes | Owning agent. |
| `worker_role` | string | Yes | e.g. `Script Generator`, `Media Assembler`, `Platform Publisher`. |
| `payload` | string (JSON) | Yes | Worker output JSON (e.g. generate_content or publish request draft). |
| `confidence_score` | float | Yes | Worker-proposed score 0.0–1.0 (US-009); Judge may adjust with justification. |
| `state_version` | int | Yes | OCC version **at Worker start**; Judge compares to `GlobalState` (US-012). |
| `current_state_version` | int | Yes | Authoritative version from `agent:{id}:state` at Judge evaluation time. |
| `campaign_context` | object | No | Goal, locale, brand safety tier. |
| `content_manifest` | object | No | Structured summary for HITL UI (topic, format, platform target). |

---

## 4. Outputs

| Field | Type | Description |
|---|---|---|
| `task_id` | UUID | Echo. |
| `routing` | enum | `AUTO_EXECUTE` \| `HITL_QUEUE` \| `REJECT_RETRY` |
| `hitl_reason` | string | e.g. `medium_confidence`, `sensitive_topic`, `policy_review` (aligns with API `hitl_reason` patterns in technical spec). |
| `judge_confidence` | float | 0.0–1.0 after Judge processing (may equal or differ from Worker score). |
| `sensitive_topic_flags` | string[] | Zero or more of `POLITICS`, `HEALTH`, `FINANCE`, `LEGAL` (US-011). |
| `occ` | object | `commit_allowed` (boolean), `reject_reason` if drift (US-012). |
| `decision_trace` | object | Machine-readable steps for US-017 (reasoning trace, classifier scores). |
| `skill_version` | string | Semver. |

Downstream mapping:

- `AUTO_EXECUTE` → proceed to approved MCP execution path (e.g. publish) **only if** OCC commit allowed.
- `HITL_QUEUE` → `hitl:queue` / dashboard (NFR 1.1 SLA).
- `REJECT_RETRY` → Planner replan; never auto-publish.

---

## 5. Validation rules

- `confidence_score` ∈ [0.0, 1.0].
- If **any** sensitive-topic flag is raised, **force** `routing = HITL_QUEUE` regardless of score (US-011, NFR 1.2).
- Routing thresholds (US-010):
  - `> 0.90` → `AUTO_EXECUTE` **only if** OCC valid and no mandatory HITL flags.
  - `0.70–0.90` → `HITL_QUEUE` (async review).
  - `< 0.70` → `REJECT_RETRY`.
- OCC: if `state_version != current_state_version`, `occ.commit_allowed = false`, `routing` must not be `AUTO_EXECUTE` with side effects; prefer `REJECT_RETRY` or `HITL_QUEUE` with explicit `state_drift` reason.
- Every decision must include `decision_trace` sufficient for moderator review (US-017).

---

## 6. Error cases

| Code / type | Meaning |
|---|---|
| `VALIDATION_ERROR` | Malformed payload or missing fields |
| `PAYLOAD_PARSE_ERROR` | Worker JSON not parseable |
| `CLASSIFIER_UNAVAILABLE` | Sensitive-topic MCP/LLM bridge down |
| `STATE_READ_ERROR` | Cannot load `current_state_version` |
| `INTERNAL_ERROR` | Unhandled failure |

On `CLASSIFIER_UNAVAILABLE`, default to **safe** routing: `HITL_QUEUE` with `hitl_reason: classifier_degraded` (never auto-publish on uncertainty).

---

## 7. Retry behavior

| Scenario | Policy |
|---|---|
| Transient state read / MCP read for classifier | Limited retries (e.g. 2) with short backoff. |
| `PAYLOAD_PARSE_ERROR` | **No** retry; `REJECT_RETRY` to Planner with parse error detail. |
| OCC drift | **No** blind retry at Judge; return structured `REJECT_RETRY` or HITL with drift explanation. |
| HITL queue insert failure | Retry queue write with idempotency key on `task_id`. |

---

## 8. Traceability (specs & user stories)

| Artifact | Reference |
|---|---|
| US-009 | Worker supplies `confidence_score`; Judge consumes (NFR 1.0) |
| US-010 | Threshold routing (NFR 1.1) |
| US-011 | Sensitive topics → mandatory HITL (NFR 1.2, EU AI Act alignment) |
| US-012 | OCC `state_version` check (FR 6.1) |
| US-017 | Review UI: content, score, trace, approve/reject (2h SLA) |
| `specs/technical.md` | `GET /api/tasks/{id}/status`, `POST /api/judge/review`, Redis `review_queue`, `hitl:queue` |

---

## 9. Example request

```json
{
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "agent_id": "agent-001",
  "worker_role": "Media Assembler",
  "payload": "{\"success\":true,\"confidence_score\":0.86,\"script\":{...},\"media\":{...}}",
  "confidence_score": 0.86,
  "state_version": 7,
  "current_state_version": 7,
  "campaign_context": {
    "goal": "Hype sneaker drop to Gen-Z",
    "locale": "en-US"
  },
  "content_manifest": {
    "topic": "Street fashion",
    "format": "short_video",
    "platform_target": "tiktok"
  }
}
```

---

## 10. Example response

```json
{
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "routing": "HITL_QUEUE",
  "hitl_reason": "medium_confidence",
  "judge_confidence": 0.86,
  "sensitive_topic_flags": [],
  "occ": {
    "commit_allowed": true,
    "reject_reason": null
  },
  "decision_trace": {
    "worker_score": 0.86,
    "threshold_band": "0.70-0.90",
    "classifier_scores": {
      "politics": 0.04,
      "health": 0.02,
      "finance": 0.06,
      "legal": 0.01
    },
    "notes": "Within medium band; require human approval before publish."
  },
  "skill_version": "1.0.0"
}
```

**Example (sensitive topic — forced HITL):**

```json
{
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "routing": "HITL_QUEUE",
  "hitl_reason": "sensitive_topic",
  "judge_confidence": 0.93,
  "sensitive_topic_flags": ["HEALTH"],
  "occ": {
    "commit_allowed": true,
    "reject_reason": null
  },
  "decision_trace": {
    "worker_score": 0.93,
    "override": "sensitive_topic_mandatory_hitl",
    "classifier_scores": {
      "health": 0.88
    }
  },
  "skill_version": "1.0.0"
}
```

---

## 11. Non-functional requirements

| NFR | Target | Source |
|---|---|---|
| HITL SLA | Human action within 2h; escalate after 4h — skill output must carry timestamps for queue ordering | NFR 1.1 |
| Safety | Zero auto-publish when sensitive-topic path fires | NFR 1.2 |
| Latency | Judge evaluation should be fast vs Worker generation; sub-second target for logic excluding optional MCP classifier | NFR 3.1 (Judge segment) |
| Auditability | `decision_trace` retained per compliance needs | US-017 |
| Scale | Stateless evaluation; no per-agent long-lived locks in skill contract | NFR 3.0 |

---

## 12. MCP tools & resources (indirect)

This skill is **logic-first**; optional MCP usage is for **classification** or **memory**, not publishing:

| Server | Usage |
|---|---|
| Lightweight LLM / policy MCP (deployment-specific) | Semantic sensitive-topic scoring (architecture §4 keyword + semantic) |
| `mcp-server-weaviate` | Optional retrieval of policy snippets or past moderation decisions |

**Forbidden indirect calls:** `twitter.post_tweet`, `instagram.publish_media`, `post_content`, Coinbase **transfer**—those belong to approved execution paths **after** routing and OCC commit, not inside the review skill’s default contract.

---

## 13. What this skill must never do

- **Auto-publish** or invoke publish MCP tools when `routing` is `HITL_QUEUE` or `REJECT_RETRY`.
- **Lower** mandatory HITL for sensitive topics to meet auto thresholds (US-011).
- Approve **financial execution** or bypass **CFO Sub-Judge** (US-014).
- Commit GlobalState with a stale `state_version` (US-012).
- Drop `confidence_score` or `decision_trace` required for HITL (US-017).
- Log full private keys, raw moderator PII, or entire copyrighted third-party content unnecessarily.
- Use `Thread.sleep` or unbounded waits for human review (HITL is async queue, not blocking sleep).
