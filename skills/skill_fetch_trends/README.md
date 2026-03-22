# Skill: skill_fetch_trends
> **Version:** 1.0.0  
> **Category:** Perception  
> **MCP Servers Required:** `mcp-server-news`, `mcp-server-twitter`

## Purpose

Fetches trending topics from configured MCP Resources, applies semantic filtering,
and returns a ranked list of `TrendData` records for the Planner to act on.

## Input Contract

```json
{
  "platforms": ["twitter", "youtube", "google_trends", "news"],
  "niche": "string — agent's content niche (e.g. 'Ethiopian fashion')",
  "relevance_threshold": 0.75,
  "max_results": 10
}
```

## Output Contract

```json
{
  "trends": [
    {
      "platform": "twitter",
      "topic": "#EthiopianFashionWeek",
      "virality_score": 0.91,
      "fetched_at": "2026-03-21T10:00:00Z",
      "raw_metadata": {}
    }
  ],
  "fetch_duration_ms": 320,
  "skill_version": "1.0.0"
}
```

## Exceptions

| Exception | Condition | Behaviour |
|---|---|---|
| `McpConnectionException` | MCP Server unreachable | Retry 3x with exponential backoff, then throw |
| `RelevanceFilterException` | Zero results above threshold | Return empty list, log warning |
| `BudgetExceededException` | Fetch cost exceeds per-task budget | Abort, escalate to CFO Judge |

## Polling Schedule

Runs every **4 hours** as a background Worker (aligned with OpenClaw polling cycle).
