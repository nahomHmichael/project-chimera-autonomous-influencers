package com.chimera.model;

import java.util.UUID;

/** Worker result with OCC state version — SRS §6.1 */
public record AgentResult(
    UUID taskId,
    UUID workerId,
    boolean success,
    float confidenceScore,
    String payload,
    int stateVersion
) {}
