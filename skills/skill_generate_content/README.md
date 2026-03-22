# Skill: `skill_generate_content`

> **Version:** 1.0.0  
> **Category:** Multimodal content production  
> **Status:** Contract only (no runtime implementation in this repo yet)

---

## 1. Purpose

Given a **Trend Alert** (or equivalent task context) and **persona constraints** from `SOUL.md`, produce a **script**, **visual assets**, and **video tier selection** (Tier 1 Living Portrait vs Tier 2 full text-to-video) within **budget and policy**—or return a structured failure for Planner/Judge. All paid or external generative steps go through **MCP tools** (Ideogram, Runway, Weaviate memory, Coinbase balance), never direct vendor SDKs in the agent core.

---

## 2. Role in the Planner / Worker / Judge system

| Phase | Role |
|---|---|
| **Planner** | Decomposes campaign goals into tasks; supplies `TaskContext` (goal, persona constraints, `required_resources`), priority, and budget hints (US-006). |
| **Worker** | **Script Generator** + **Media Assembler** stages: US-004 script, US-005 image via Ideogram + `character_reference_id`, US-006 tier selection. |
| **Judge** | Consumes Worker `AgentResult` with `confidence_score` (US-009); routes auto / HITL / retry (US-010); sensitive-topic override (US-011). |

This skill does **not** replace the Publisher: **publication** is a separate worker/skill path after Judge approval (US-007).

---

## 3. Inputs

| Field | Type | Required | Description |
|---|---|---|---|
| `task_id` | UUID | Yes | `AgentTask.taskId`. |
| `agent_id` | string | Yes | Persona / wallet context. |
| `state_version` | int | Yes | OCC version at start (US-012). |
| `goal_description` | string | Yes | Aligns with `TaskContext.goalDescription`. |
| `persona_constraints` | string[] | Yes | Voice, topics to avoid (e.g. “No politics”). |
| `trend_alert` | object | No | Summary topic, virality, source URIs from `skill_fetch_trends`. |
| `character_reference_id` | string | Yes for image path | FR 3.1 visual consistency. |
| `priority` | enum | Yes | `high` \| `medium` \| `low` — informs Tier 1 vs 2 (US-006). |
| `platform_target` | string | Yes | e.g. `tiktok`, `instagram`, `twitter` (drives format hints). |
| `estimated_budget_usdc_max` | float | No | Soft cap for Planner hint; hard enforcement is CFO + balance (US-013, US-014). |
| `required_resources` | string[] | No | MCP URIs, e.g. `mcp://memory/agent-001/recent` (technical §2). |

**Preconditions:** `get_balance` (or equivalent MCP) **before** any cost-incurring tool (US-013). Tier-2 jobs require explicit budget headroom under policy.

---

## 4. Outputs

| Field | Type | Description |
|---|---|---|
| `task_id` | UUID | Echo. |
| `success` | boolean | False if generation failed or aborted safely. |
| `confidence_score` | float | 0.0–1.0 — holistic quality + policy adherence (US-009). |
| `script` | object | `title`, `beats[]`, `cta`, `duration_seconds_estimate`, `disclosure_suggestion` (`automated` \| `assisted` \| `none`). |
| `media` | object | `image_job_ref`, `image_url` (if completed), `video_tier` (`TIER_1` \| `TIER_2`), `video_job_ref`, `video_url` (if completed). |
| `spend_usdc` | float | Actual or estimated spend for this run (for CFO ledger). |
| `state_version` | int | Echo for OCC. |
| `skill_version` | string | Semver. |
| `reasoning_trace_id` | string | For HITL UI (US-017). |

Serialized as JSON inside `AgentResult.payload` for the review queue.

---

## 5. Validation rules

- `persona_constraints` non-empty; script output must not violate listed constraints (detectable violations lower `confidence_score` or fail).
- `character_reference_id` required when requesting image generation (US-005).
- `disclosure_suggestion` must be one of allowed enum values matching [`specs/technical.md`](../../specs/technical.md) `post_content` schema.
- `confidence_score` mandatory on every completion path (US-009).
- If `priority` is `low`, default to Tier 1 unless Planner overrides with budget proof.
- Tier 2 selection must record **reason** (priority + budget) for audit.

---

## 6. Error cases

| Code / type | Meaning |
|---|---|
| `VALIDATION_ERROR` | Missing persona, character ref, or invalid priority |
| `INSUFFICIENT_BALANCE` | Pre-flight balance check failed (US-013) |
| `CFO_REJECTED` | Budget or daily cap would be exceeded (US-014) |
| `MCP_TOOL_ERROR` | Ideogram / Runway / Weaviate MCP failure |
| `GENERATION_TIMEOUT` | Vendor-side timeout |
| `POLICY_VIOLATION` | Obvious constraint breach (e.g. disallowed topic in script) |
| `INTERNAL_ERROR` | Unclassified failure |

---

## 7. Retry behavior

| Scenario | Policy |
|---|---|
| Idempotent **read** (memory fetch) | Retry up to 3 with backoff. |
| **Image / video generation** MCP failures | Limited retries (e.g. 2); each retry re-validates balance. |
| `INSUFFICIENT_BALANCE` / `CFO_REJECTED` | **No** retry; return structured failure to Planner. |
| Partial success (script ok, image failed) | Return `success: false` with partial payload **or** explicit `partial: true` + degraded `confidence_score` (implementation choice documented at bind time). |
| Transient 5xx from MCP | Backoff retry ≤ 3. |

---

## 8. Traceability (specs & user stories)

| Artifact | Reference |
|---|---|
| US-004 | Script from Trend Alert + SOUL.md constraints (FR 3.0) |
| US-005 | Ideogram + `character_reference_id` (FR 3.1) |
| US-006 | Video tier vs budget (FR 3.2) |
| US-009 | `confidence_score` on output (NFR 1.0) |
| US-012 | `state_version` (FR 6.1) |
| US-013 | Balance before spend (FR 5.1) |
| US-014 | CFO cap interaction (FR 5.2) |
| `specs/technical.md` | `AgentTask`, `AgentResult`, `post_content` tool schema |

---

## 9. Example request

```json
{
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "agent_id": "agent-001",
  "state_version": 7,
  "goal_description": "60s TikTok on Ethiopian fashion week street style",
  "persona_constraints": ["Gen-Z tone", "No politics", "No health claims"],
  "trend_alert": {
    "alert_id": "tal-20260322-001",
    "summary_topic": "Ethiopian fashion week street style",
    "virality_score": 0.91,
    "relevance_score": 0.81
  },
  "character_reference_id": "chr-ref-ethio-vogue-01",
  "priority": "high",
  "platform_target": "tiktok",
  "estimated_budget_usdc_max": 12.0,
  "required_resources": [
    "mcp://memory/agent-001/recent"
  ]
}
```

---

## 10. Example response

```json
{
  "task_id": "550e8400-e29b-41d4-a716-446655440000",
  "success": true,
  "confidence_score": 0.86,
  "script": {
    "title": "Addis sidewalks are the runway",
    "beats": [
      "Hook: fit check in Merkato alley",
      "Trend: three silhouettes blowing up",
      "CTA: comment your city"
    ],
    "cta": "Drop your city if street fashion hits different",
    "duration_seconds_estimate": 58,
    "disclosure_suggestion": "automated"
  },
  "media": {
    "image_job_ref": "img-job-88421",
    "image_url": "https://cdn.example.invalid/chimera/agent-001/tal-001.png",
    "video_tier": "TIER_2",
    "video_job_ref": "vid-job-99102",
    "video_url": "https://cdn.example.invalid/chimera/agent-001/tal-001.mp4"
  },
  "spend_usdc": 8.4,
  "state_version": 7,
  "skill_version": "1.0.0",
  "reasoning_trace_id": "trace-b2c8d1"
}
```

---

## 11. Non-functional requirements

| NFR | Target | Source |
|---|---|---|
| Concurrency | Workers use virtual-thread executor at host level | Architecture strategy §6 |
| Cost observability | Every billed MCP call accounted in `spend_usdc` | FR 5.x alignment |
| Latency | Generation bounded by orchestrator/job timeouts; align with NFR 3.1 where applicable | NFR 3.1 |
| Scale | Stateless skill contract; horizontal scale via `task_queue` | NFR 3.0 |
| Safety | Output subject to Judge + sensitive-topic detection (US-011) | NFR 1.2 |

---

## 12. MCP tools & resources (indirect)

| Server | Typical tools / resources |
|---|---|
| `mcp-server-weaviate` | `search_memory`, `store_memory` (persona / RAG context) |
| `mcp-server-ideogram` | Image generation tool (with `character_reference_id`) |
| `mcp-server-runway` | Video generation tool (Tier 2); Tier 1 may use a different tool manifest per deployment |
| `mcp-server-coinbase` | `get_balance`, and **only** spend paths approved after CFO Sub-Judge (not invoked directly by this skill for settlement—balance read is pre-flight) |

Exact tool names and schemas are defined by each MCP server; implementations must map to those manifests while preserving the **MCP-only** boundary.

---

## 13. What this skill must never do

- Publish to Twitter, Instagram, or Threads (US-007 belongs to Publisher worker / `post_content` path after Judge).
- Call Ideogram, Runway, or LLM vendor APIs **without** MCP indirection.
- Skip **balance** check before cost-incurring MCP tools (US-013).
- Override **CFO** or daily spend limits (US-014).
- Emit final posts without `disclosure_level` / disclosure strategy alignment for downstream `post_content` (NFR 2.0).
- Hardcode secrets or log raw wallet material (US-015).
- Bump `state_version` or commit GlobalState (Judge, US-012).
