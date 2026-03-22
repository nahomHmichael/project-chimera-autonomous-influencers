package com.chimera.skill;

/**
 * Reusable capability package that orchestrates MCP tool invocations (higher level than raw MCP calls).
 *
 * <p><b>SRS:</b> §3.2 MCP integration architecture; skills directory contracts.
 * <b>User stories:</b> US-004–US-007 (skill-sized workflows such as fetch trends, generate content, post).</p>
 *
 * @param <I> skill input DTO (record)
 * @param <O> skill output DTO (record)
 */
public interface Skill<I, O> {

    /**
     * Executes the skill for one logical request.
     *
     * @param input validated skill input
     * @return skill output; implementations must surface errors as exceptions or structured failure DTOs
     */
    O execute(I input);

    /**
     * Unique skill id (e.g. {@code skill_fetch_trends}).
     *
     * @return stable identifier
     */
    String skillId();

    /**
     * Semantic version of the skill contract.
     *
     * @return semver string
     */
    String version();
}
