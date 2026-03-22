# Skill: `skill_fetch_trends`

> **Version:** 1.0.0  
> **Category:** Perception & trend detection  
> **Status:** Contract only (no runtime implementation in this repo yet)

---

## 1. Purpose

Ingest normalized trend signals from external sources (Twitter, YouTube, Google Trends, news/RSS) **exclusively via MCP Resources**, apply a **semantic relevance filter** (threshold ≥ 0.75), cluster related items, and emit **Trend Alerts** the Planner can turn into `generate_content` tasks—without the agent core calling any third-party HTTP APIs directly.

---

## 2. Role in the Planner / Worker / Judge system

| Phase | Role |
|---|---|
| **Planner** | Consumes skill output as input to campaign DAG expansion; may apply US-002 filtering policy when selecting which alerts become tasks. |
| **Worker** | Executed by the **Trend Fetcher** / **Trend Spotter** workers: scheduled polling (4h cadence per US-001), aggregation, clustering (US-003). |
| **Judge** | Does **not** adjudicate raw trend fetches unless bundled into a downstream content task; trend payloads may be logged for audit. |

Results are pushed toward **`task_queue`** as new tasks only **after** Planner validation—not by this skill alone.

---

## 3. Inputs

| Field | Type | Required | Description |
|---|---|---|---|
| `task_id` | UUID | Yes | Correlates to `AgentTask.taskId` from [`specs/technical.md`](../../specs/technical.md) Schema 1. |
| `agent_id` | string | Yes | Agent / persona instance performing the fetch. |
| `state_version` | int | Yes | OCC snapshot at task start (US-012). |
| `platforms` | string[] | Yes | Subset of `twitter`, `youtube`, `google_trends`, `news` (US-001). |
| `niche` | string | Yes | Semantic filter anchor (e.g. campaign niche). |
| `relevance_threshold` | float | No | Default `0.75` (US-002, FR 2.1). |
| `max_results` | int | No | Cap per platform; default `10`. |
| `required_resource_uris` | string[] | No | Explicit MCP resource URIs to poll (e.g. `mcp://news/...` per technical examples). |

**Preconditions:** Worker must not start **cost-incurring** downstream paths without balance check (US-013); this skill treats MCP read/poll as non-financial unless a server bills per call—then `get_balance` precedes invocation.

---

## 4. Outputs

| Field | Type | Description |
|---|---|---|
| `task_id` | UUID | Echo of input. |
| `success` | boolean | False if unrecoverable after retries. |
| `confidence_score` | float | 0.0–1.0 for **quality of the trend set** (coverage + coherence), per US-009. |
| `trend_alerts` | array | Clustered alerts: `alert_id`, `summary_topic`, `platforms`, `virality_score`, `relevance_score`, `fetched_at`, `source_resource_uris[]`. |
| `filtered_out_count` | int | Items below relevance threshold. |
| `skill_version` | string | e.g. `1.0.0`. |
| `reasoning_trace_id` | string | Opaque ID for Judge/HITL correlation (US-017). |

Payload must be serializable to the Worker → Judge envelope (`AgentResult.payload` JSON string in technical spec).

---

## 5. Validation rules

- `relevance_threshold` ∈ [0.0, 1.0]; if omitted, use `0.75`.
- `platforms` non-empty; unknown values rejected with validation error (no silent drop).
- Every `trend_alerts[].relevance_score` ≥ configured threshold, or the alert must not appear in `trend_alerts` (only counted in `filtered_out_count`).
- `confidence_score` must be set on every successful completion (US-009).
- `state_version` echoed unchanged in side-channel metadata for Judge OCC (US-012).

---

## 6. Error cases

| Code / type | Meaning | Typical cause |
|---|---|---|
| `VALIDATION_ERROR` | Bad input shape or unsupported platform | Caller bug or stale task schema |
| `MCP_UNAVAILABLE` | MCP server or resource unreachable | Network, server down, misconfiguration |
| `MCP_AUTH_ERROR` | Auth failure on MCP transport | Expired credentials, wrong env |
| `RATE_LIMITED` | Upstream or MCP rate limit | Platform throttling |
| `EMPTY_AFTER_FILTER` | No trends ≥ threshold | Niche too narrow or stale sources |
| `BUDGET_BLOCKED` | Balance / budget pre-check failed | US-013 / CFO policy |
| `INTERNAL_ERROR` | Unexpected failure | Implementation defect |

---

## 7. Retry behavior

| Scenario | Policy |
|---|---|
| Transient MCP / network errors | Exponential backoff: e.g. 3 attempts, base delay 1s, max delay 30s, jitter. |
| Rate limited | Respect `Retry-After` or MCP-provided hint; if absent, backoff then retry ≤ 3 times. |
| Validation errors | **No** retry; fail fast. |
| `EMPTY_AFTER_FILTER` | **No** retry for the same inputs within the same poll window; Planner may reschedule next 4h cycle (US-001). |
| Exhausted retries | Return `success: false`, `confidence_score` reflecting uncertainty (e.g. ≤ 0.5), populate error code for Judge routing. |

---

## 8. Traceability (specs & user stories)

| Artifact | Reference |
|---|---|
| US-001 | Poll MCP Resources every 4 hours (Twitter, YouTube, Google Trends). |
| US-002 | Semantic filter relevance ≥ 0.75 (FR 2.1). |
| US-003 | Cluster topics → Trend Alerts (FR 2.2). |
| US-009 | Attach `confidence_score` to output (NFR 1.0). |
| US-012 | Carry `state_version` for OCC (FR 6.1). |
| US-013 | Balance check before billed MCP usage (FR 5.1). |
| `specs/technical.md` | `AgentTask`, `TaskContext`, `AgentResult`, Redis `trend:cache:{platform}` TTL 4h |

---

## 9. Example request

```json
{
  "task_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "agent_id": "agent-001",
  "state_version": 7,
  "platforms": ["twitter", "youtube", "google_trends", "news"],
  "niche": "Ethiopian fashion, Gen-Z tone, no politics",
  "relevance_threshold": 0.75,
  "max_results": 10,
  "required_resource_uris": [
    "mcp://news/ethiopia/fashion/latest"
  ]
}
```

---

## 10. Example response

```json
{
  "task_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "success": true,
  "confidence_score": 0.88,
  "trend_alerts": [
    {
      "alert_id": "tal-20260322-001",
      "summary_topic": "Ethiopian fashion week street style",
      "platforms": ["twitter", "news"],
      "virality_score": 0.91,
      "relevance_score": 0.81,
      "fetched_at": "2026-03-22T12:00:00Z",
      "source_resource_uris": [
        "mcp://twitter/trends/place/ET",
        "mcp://news/ethiopia/fashion/latest"
      ]
    }
  ],
  "filtered_out_count": 14,
  "skill_version": "1.0.0",
  "reasoning_trace_id": "trace-7f3a9c"
}
```

---

## 11. Non-functional requirements

| NFR | Target | Source |
|---|---|---|
| Polling cadence | Align with 4h trend cycle (OpenClaw / SRS alignment) | Architecture strategy §1.2 |
| Scale | Design for 1,000+ agents without degrading orchestrator | NFR 3.0 |
| Caching | Respect Redis `trend:cache:{platform}` (4h TTL) where applicable | `specs/technical.md` §4 |
| Latency | Fetches should complete within orchestrator SLA for Worker slots | NFR 3.1 (where applicable) |
| Security | No secrets in skill text; env-based credentials only | US-015, FR 5.0 |

---

## 12. MCP tools & resources (indirect)

This skill **only** interacts with the outside world through MCP. Typical **logical** servers (see [`research/architecture_strategy.md`](../../research/architecture_strategy.md) §6.2):

| Server | Usage |
|---|---|
| `mcp-server-news` | RSS / news resources for trends |
| `mcp-server-twitter` | Resources for trends, hashtags, or mentions (read paths) |
| YouTube / Google Trends | Via MCP resources exposed by the configured bridge (naming is deployment-specific; contract is **MCP resource URI**, not raw YouTube API) |

Exact tool/resource names are defined by each MCP server’s manifest; this skill binds to **URIs** declared in `required_resource_uris` or Planner-supplied `TaskContext.requiredResources`.

---

## 13. What this skill must never do

- Call external social or news **HTTP APIs** directly from agent core code (violates FR 4.0 / architecture MCP-only boundary).
- **Publish** content to social platforms (that is `skill_generate_content` follow-on + Publisher + Judge, US-007).
- **Mutate** `state_version` or commit GlobalState (Judge responsibility, US-012).
- Log or persist wallet keys, tokens, or PII beyond operational necessity (US-015).
- Bypass relevance threshold to “force” alerts into the Planner.
- Run unbounded retries or `Thread.sleep`-style blocking without backoff policy (production NFR alignment).
