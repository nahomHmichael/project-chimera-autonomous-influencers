# Project Chimera — Tooling Strategy
> **Author:** Forward Deployed Engineer  
> **Date:** 2026-03-21

## Developer MCP Servers (Build-Time Tools)

These tools help developers BUILD Chimera. They are NOT used by agents at runtime.

| MCP Server | Purpose | Transport |
|---|---|---|
| `git-mcp` | Version control operations from IDE | stdio |
| `filesystem-mcp` | File editing, directory traversal | stdio |
| `sqlite-mcp` | Local database queries during dev | stdio |
| `Tenx MCP Sense` | Traceability / "Black Box" flight recorder | SSE |

## Runtime MCP Servers (Agent Tools)

These are used by Chimera agents at runtime to interact with the external world.

| MCP Server | Purpose | Transport |
|---|---|---|
| `mcp-server-twitter` | `post_tweet`, `reply_tweet`, `read mentions` | SSE |
| `mcp-server-instagram` | `publish_media`, `read_comments` | SSE |
| `mcp-server-weaviate` | `search_memory`, `store_memory` | SSE |
| `mcp-server-coinbase` | `get_balance`, `transfer_asset`, `deploy_token` | SSE |
| `mcp-server-ideogram` | `generate_image` (with `character_reference_id`) | stdio |
| `mcp-server-runway` | `generate_video` (Tier 2 hero content) | SSE |
| `mcp-server-news` | RSS feed aggregation, trend detection | stdio |

## Agent Skills (Runtime Capability Packages)

Skills are higher-level than raw MCP Tool calls. They encapsulate retry logic,
budget checks, persona constraints, and OCC version tracking.

| Skill | Description | Spec |
|---|---|---|
| `skill_fetch_trends` | Polls MCP Resources, filters by relevance | `skills/skill_fetch_trends/README.md` |
| `skill_post_content` | Publishes via MCP Tools with disclosure enforcement | `skills/skill_post_content/README.md` |

## Key Separation Rule

> **Dev MCP Servers** are for humans building the system.  
> **Runtime MCP Servers** are bridges the agents use to act in the world.  
> **Skills** are the agent's reusable capability packages that orchestrate Runtime MCP calls.
