package com.chimera.worker;

import java.util.Map;

/**
 * Base interface for all Agent Skills — SRS §3.2 / skills/ directory.
 * Skills are reusable capability packages that orchestrate MCP Tool calls.
 */
public interface Skill<I, O> {
    /** Execute the skill with the given typed input, return typed output. */
    O execute(I input);

    /** Unique skill identifier used for OpenClaw Content Manifests. */
    String skillId();

    /** Semantic version of this skill. */
    String version();
}
