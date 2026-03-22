package com.chimera;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chimera.model.AgentResult;
import com.chimera.model.AgentTask;
import com.chimera.model.TaskContext;
import com.chimera.model.TrendData;
import com.chimera.worker.trend.TrendFetcherWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for the Trend Fetcher Worker and related DTOs.
 *
 * <p><b>Specs:</b> {@code specs/technical.md} §1 ({@code TrendData}, {@code AgentResult}, {@code AgentTask}),
 * Redis {@code trend:cache:{platform}} §4.
 * <b>Architecture:</b> {@code research/architecture_strategy.md} §3 (Worker: Trend Fetcher, {@code review_queue}).
 * <b>User stories:</b> US-001 (MCP poll), US-003 (Trend Alerts), US-009 ({@code confidenceScore}).</p>
 *
 * <p>Execution tests against {@link TrendFetcherWorker#execute(AgentTask)} are expected to fail (error) until
 * MCP-backed implementation exists — they define the empty slot.</p>
 */
@DisplayName("Trend Fetcher Worker contract")
class trendFetcherTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static AgentTask sampleTrendFetchTask() {
        return new AgentTask(
                UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
                "fetch_trends",
                "high",
                new TaskContext(
                        "Ingest trends for niche test",
                        List.of("Gen-Z tone", "No politics"),
                        List.of("mcp://news/example/latest")),
                null,
                Instant.parse("2026-03-22T12:00:00Z"),
                "pending");
    }

    @Test
    @DisplayName("workerRole matches architecture Strategy §3 Agent Roles table")
    void workerRoleMatchesArchitectureTable() {
        assertEquals("Trend Fetcher Worker", TrendFetcherWorker.create().workerRole());
    }

    @Nested
    @DisplayName("execute(AgentTask) — must satisfy AgentResult + payload contract (currently unimplemented)")
    class ExecuteContract {

        /**
         * Red test: succeeds only when {@link TrendFetcherWorker#execute(AgentTask)} returns a real result.
         * Until then, {@link UnsupportedOperationException} fails the test run.
         */
        @Test
        @DisplayName("returns AgentResult with confidenceScore in [0,1] and non-blank JSON payload")
        void returnsResultWithValidConfidenceAndPayload() throws Exception {
            TrendFetcherWorker worker = TrendFetcherWorker.create();
            AgentResult result = worker.execute(sampleTrendFetchTask());
            assertNotNull(result);
            assertTrue(isValidConfidence(result.confidenceScore()), "specs/technical.md §1 AgentResult 0.0–1.0");
            assertNotNull(result.payload());
            assertFalse(result.payload().isBlank());
            assertPayloadContainsTrendsArray(result.payload());
        }
    }

    @Nested
    @DisplayName("TrendData DTO (immutable record)")
    class TrendDataContract {

        @Test
        @DisplayName("TrendData is a Java record (immutable DTO per CLAUDE.md / technical.md §1)")
        void trendDataIsImmutableRecord() {
            assertTrue(TrendData.class.isRecord());
        }

        @Test
        @DisplayName("rawMetadataJson holds opaque JSON (not Map<String,Object>) per technical.md §1")
        void rawMetadataIsJsonDocumentString() throws Exception {
            TrendData td =
                    new TrendData("twitter", "#Example", 0.85f, Instant.now(), "{\"source\":\"mcp\"}");
            JsonNode meta = JSON.readTree(td.rawMetadataJson());
            assertTrue(meta.has("source"));
        }
    }

    @Nested
    @DisplayName("AgentResult confidence invariant (technical.md §1)")
    class ConfidenceInvariant {

        @Test
        @DisplayName("values outside [0,1] violate the published DTO contract")
        void confidenceOutsideUnitIntervalIsInvalid() {
            var invalid = new AgentResult(UUID.randomUUID(), UUID.randomUUID(), true, 1.2f, "{}", 1);
            assertFalse(isValidConfidence(invalid.confidenceScore()));
        }
    }

    static boolean isValidConfidence(float c) {
        return c >= 0.0f && c <= 1.0f;
    }

    /**
     * Aligns with {@code skills/skill_fetch_trends/README.md} output shape (trends array).
     *
     * @param payload JSON string from {@link AgentResult#payload()}
     */
    static void assertPayloadContainsTrendsArray(String payload) throws Exception {
        JsonNode root = JSON.readTree(payload);
        assertTrue(root.has("trends"), "payload must expose a 'trends' array per skill_fetch_trends contract");
        assertTrue(root.get("trends").isArray());
    }
}
