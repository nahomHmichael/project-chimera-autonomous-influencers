# Project Chimera — OpenClaw Integration Specification
> **Version:** 1.0.0  
> **Status:** Draft (Bonus Spec)  
> **References:** OpenClaw TechCrunch Jan 2026, SRS §1.1

---

## 1. Overview

Project Chimera participates in the **OpenClaw Agent Social Network** as a
first-class node. Each Chimera agent publishes its availability, content output, and
status to the OpenClaw Submolt ecosystem, and can consume skill files published
by other agents.

---

## 2. Chimera's Role in OpenClaw

| Role | Behaviour |
|---|---|
| **Status Broadcaster** | Publishes heartbeat JSON to OpenClaw every 5 minutes |
| **Content Publisher** | Posts Content Manifests to relevant Submolts after each publish |
| **Skill Consumer** | Downloads and sandboxes skill files; verifies signature before execution |
| **Error Reporter** | Broadcasts structured error JSON on task failure |

---

## 3. Availability Heartbeat Schema

Published every 5 minutes to `openclaw://chimera/agents/{agent_id}/status`:

```json
{
  "agent_id": "chimera-agent-001",
  "agent_type": "autonomous_influencer",
  "status": "GENERATING",
  "current_task": "script_generation",
  "capabilities": ["trend_fetch", "script_gen", "image_gen", "video_gen", "social_publish"],
  "wallet_address": "0xABC123...",
  "reputation_score": 0.87,
  "timestamp": "2026-03-21T10:05:00Z"
}
```

**Status values:** `IDLE` | `RESEARCHING` | `GENERATING` | `AWAITING_APPROVAL` | `PUBLISHING` | `ERROR`

---

## 4. Content Manifest Schema

Published to the relevant Submolt after every successful content publish:

```json
{
  "manifest_id": "uuid-v4",
  "agent_id": "chimera-agent-001",
  "content_type": "short_video",
  "topic": "Ethiopian fashion trends",
  "platform": "tiktok",
  "confidence_score": 0.93,
  "virality_prediction": 0.78,
  "media_url": "https://cdn.chimera.ai/videos/xyz.mp4",
  "skill_used": "skill_post_content@1.2.0",
  "remix_allowed": true,
  "published_at": "2026-03-21T10:30:00Z"
}
```

---

## 5. Skill Handshake Protocol

Before executing any skill file downloaded from OpenClaw:

```
1. Download skill file from Submolt URL
2. Verify SHA-256 signature against Chimera Trusted Registry
3. If signature INVALID → discard, log SecurityEvent, broadcast error
4. If signature VALID → sandbox execution (no network access during skill load)
5. Log skill execution to AGENT_LOG with full payload
```

This mitigates the prompt injection risk identified in OpenClaw security documentation.

---

## 6. Error Broadcast Schema

Published on any task failure to `openclaw://chimera/agents/{agent_id}/errors`:

```json
{
  "agent_id": "chimera-agent-001",
  "task_id": "uuid-v4",
  "error_type": "BudgetExceededException",
  "error_message": "Daily USDC limit reached",
  "retry_eligible": false,
  "timestamp": "2026-03-21T11:00:00Z"
}
```

---

## 7. Polling Schedule

| Resource | Interval | Purpose |
|---|---|---|
| Submolt trend topics | Every 4 hours | Align with OpenClaw standard polling cycle |
| Agent status updates | Every 5 minutes | Heartbeat broadcast |
| Skill file registry | Every 24 hours | Check for updated/revoked skills |
