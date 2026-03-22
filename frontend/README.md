# Project Chimera — HITL Dashboard (concept only)

This folder holds **no runnable UI**. It documents a **lightweight operator concept** for the Human-in-the-Loop (HITL) control plane described in `specs/functional.md` (Human-in-the-Loop Dashboard, **US-019**) and `specs/technical.md` §6.

---

## Purpose

The HITL dashboard is a **governed operator review surface**. It exists so **human reviewers** can act on work the **Judge** has already routed out of the automated path (medium-confidence band, sensitive-topic policy, or operational re-review). It **records** `APPROVE` / `REJECT` / `ESCALATE` as **auditable decisions** with OCC (`state_version`) — it does **not** post to social platforms and does **not** call third-party APIs directly.

**Flow (conceptual):** Worker → `review_queue` → **Judge** → items eligible for HITL appear here → reviewer acts → **Chimera backend** commits state / enqueues downstream work (e.g. MCP publish **only** via runtime services, never from this UI tier).

---

## Key views

### 1. Review queue

List of open items sorted by SLA risk (oldest `created_at` first). Each row is a **summary**; the reviewer opens **review detail** to decide.

### 2. Review detail

Full context for **one** `review_id`: safe text preview, scores, flags, and read-only correlation ids. **No** embedded executable rich media from untrusted sources — previews are **sanitized excerpts** per spec.

### 3. Review actions

Primary actions (terminal verbs):

| Action     | Meaning (operator)                                      |
|-----------|----------------------------------------------------------|
| APPROVE   | Allow runtime to proceed (post-commit / enqueue publish path). |
| REJECT    | Deny; Planner/Judge retry semantics apply.               |
| ESCALATE  | Hand to tier-2 (compliance / senior mod); no publish.   |

Submit sends `expected_state_version` with the decision; **409** if state drifted.

---

## Fields the reviewer sees

Aligned with `specs/technical.md` §6.3 / `specs/functional.md` (review item minimum):

| Field                         | Queue | Detail |
|------------------------------|:-----:|:------:|
| `review_id`                  |  ·   |   ✓    |
| `task_id`                    |  ✓   |   ✓    |
| `agent_id`                   |  ✓   |   ✓    |
| `content_summary_or_preview` |  ✓ (truncated) | ✓ (expanded excerpt) |
| `confidence_score`           |  ✓   |   ✓    |
| `policy_flags`               |  · / badge | ✓ |
| `created_at`                 |  ✓   |   ✓    |
| `state_version`              |  ·   |   ✓ (echo on submit) |

Optional: SLA timer (2h / 4h escalation narrative from NFR 1.1) as **display-only**, driven by backend policy.

---

## Text mock layout

**Queue**

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  Chimera HITL — Review queue (operator)                     [refresh] [filter]│
├──────────┬─────────────┬──────────┬────────────┬───────────────────────────┤
│ Task     │ Agent       │ Score    │ Age        │ Summary                     │
├──────────┼─────────────┼──────────┼────────────┼───────────────────────────┤
│ 550e…00  │ 770e…02     │ 0.82     │ 47m        │ TikTok script: fashion …    │
│ 551a…01  │ 770e…02     │ 0.71     │ 1h 12m     │ Reply draft: mention …    │
│ …        │ …           │ …        │ …          │ …                           │
└──────────┴─────────────┴──────────┴────────────┴───────────────────────────┘
        [Open selected]
```

**Detail + actions**

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  Review: 880e8400-e29b-41d4-a716-446655440003                                 │
├──────────────────────────────────────────────────────────────────────────────┤
│  task_id: 550e8400-e29b-41d4-a716-446655440000                                │
│  agent_id: 770e8400-e29b-41d4-a716-446655440002                               │
│  confidence_score: 0.82    state_version (for submit): 7                        │
│  created_at: 2026-03-22T13:00:00Z                                           │
│  policy_flags: { "sensitive_topic": false, "disclosure": "assisted" }         │
├──────────────────────────────────────────────────────────────────────────────┤
│  Content preview (non-executable)                                             │
│  ───────────────────────────────────────────────────────────────────────────  │
│  | Hook: street style in Addis…                                               │
│  | Body: … (truncated)                                                        │
│  ───────────────────────────────────────────────────────────────────────────  │
├──────────────────────────────────────────────────────────────────────────────┤
│  Notes to audit log (optional): [________________________________]           │
│                                                                               │
│     [ APPROVE ]     [ REJECT ]     [ ESCALATE ]     expected_state_version: 7 │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Non-goals

- **No** React, Vue, Svelte, or other app code in this repo path.
- **No** real auth, routing, or API client — backend contracts live in `specs/technical.md` §6.
- **No** direct publish buttons that hit Twitter/Instagram from the browser.
- **Not** a marketing site, influencer-facing app, or content editor product.
- **Not** a replacement for Judge routing logic — the dashboard **consumes** Judge output only.

---

## Relation to implementation

When a real operator UI exists, it should call **Chimera-approved operator APIs** only (`POST /api/judge/review`, queue listing as defined in §6), stay MCP-clean on the client, and treat this README as the **IA / field checklist** — not a design system.
