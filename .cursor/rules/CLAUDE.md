# PROJECT CONTEXT

Project Chimera is an autonomous influencer network: AI agents research trends, produce multimodal content, and manage social engagement under safety gates. Internal coordination uses the FastRender Swarm pattern (Planner / Worker / Judge). Model Context Protocol (MCP) is the **only** integration layer for external systems—no alternate integration path is permitted. The platform targets **1,000+ concurrent agents** and is implemented on **Java 21+**.

---

# PRIME DIRECTIVE (HIGH PRIORITY — MUST STAND OUT)

> NEVER generate implementation code without first reading the relevant
> spec in specs/. The specs/ directory is the source of truth. Ambiguity
> is the enemy of AI. If the spec is vague, ask for clarification before
> writing a single line of code.

---

# MANDATORY WORKFLOW

Step 1 — Read specs/_meta.md

Step 2 — Read specs/functional.md (US-XXX)

Step 3 — Read specs/technical.md

Step 4 — Describe implementation plan (plain English)

Step 5 — THEN write code

---

# JAVA 21+ DIRECTIVES

- **DTOs:** Use Java `record` types **only** for DTOs. Do **not** use mutable POJOs for DTOs. Do **not** use `Map<String, Object>` (or similar untyped maps) for agent payloads or DTO fields.
- **Concurrency:** Use **virtual threads only** via `Executors.newVirtualThreadPerTaskExecutor()`. **Forbidden:** `Thread`, fixed-size thread pools, or other non–virtual-thread executor models for agent/worker concurrency.
- **Testing:** JUnit 5 only; use `@ExtendWith(MockitoExtension.class)` where Mockito is needed.
- **Language style:** Prefer modern Java—pattern matching, `switch` expressions, and `sealed` classes where they clarify design.
- **Documentation:** Every **public** class must have Javadoc that references the applicable **SRS** section and **User Story** (e.g. US-XXX).

---

# ARCHITECTURE RULES

- **Redis Streams:** Planner publishes work to `task_queue`; Worker results flow to `review_queue` (`task_queue` → `review_queue`).
- **Control flow:** **Planner → Worker → Judge** end-to-end for coordinated tasks.
- **Confidence routing (Judge):**
  - **> 0.90** → auto execute
  - **0.70–0.90** → HITL (human-in-the-loop review)
  - **< 0.70** → reject and retry (signal Planner to re-plan / re-queue)
- **Financial actions:** **CFO Sub-Judge** validation is required **before any** financial execution (e.g. on-chain or budgeted spend).
- **External I/O:** **MCP is the only external interaction layer** for third-party systems and runtime bridges—no direct SDK/API calls from agent core code.

---

# WHAT NEVER TO DO

- No direct external API calls (everything goes through MCP).
- No hardcoded secrets—use `System.getenv()`, never commit or log secrets.
- No test-after-code: write or update tests **first** (TDD), then implementation.
- No `Thread.sleep` in production code.
- No mutable DTOs (records only for DTOs; see JAVA 21+ DIRECTIVES).

---

# TRACEABILITY RULE

- **Every class** must remain traceable to the **SRS** and a **User Story** (US-XXX) in Javadoc (and in design discussion when relevant).
- **Every test** must state or clearly imply which **spec** requirement or acceptance criterion it validates (spec validation reference).
