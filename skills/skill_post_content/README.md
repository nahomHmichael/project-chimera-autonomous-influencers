# Skill: skill_post_content
> **Version:** 1.0.0  
> **Category:** Action / Publishing  
> **MCP Servers Required:** `mcp-server-twitter`, `mcp-server-instagram`

## Purpose

Publishes a generated content asset to one or more social platforms via MCP Tools.
Enforces platform-native AI disclosure labels on every post (SRS NFR 2.0).
All calls are routed through the MCP layer — zero direct API access.

## Input Contract

```json
{
  "platform": "twitter | instagram | threads",
  "text_content": "string",
  "media_urls": ["string"],
  "disclosure_level": "automated | assisted | none",
  "character_reference_id": "string — for visual consistency (FR 3.1)",
  "agent_id": "string",
  "task_id": "uuid"
}
```

## Output Contract

```json
{
  "published": true,
  "platform_post_id": "string",
  "published_at": "2026-03-21T10:30:00Z",
  "disclosure_applied": true,
  "skill_version": "1.0.0"
}
```

## Pre-conditions (enforced by skill)

1. `confidence_score` of content MUST be ≥ 0.90, OR human HITL approval received.
2. Sensitive topic check MUST have passed (no Politics/Health/Finance/Legal flags).
3. `character_reference_id` MUST be present for image/video posts.

## Exceptions

| Exception | Condition | Behaviour |
|---|---|---|
| `HitlRequiredException` | Content not yet approved | Block publish, add to HITL queue |
| `SensitiveTopicException` | Topic flagged by classifier | Force HITL, never auto-publish |
| `McpPublishException` | Platform MCP Tool fails | Retry 2x, then mark job as FAILED |
| `BudgetExceededException` | Publishing API cost exceeds budget | Abort, escalate to CFO Judge |
