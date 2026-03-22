package com.chimera.model;

import java.util.List;
import java.util.UUID;

/**
 * Persona projection derived from versioned {@code SOUL.md}.
 *
 * <p><b>SRS:</b> §4.1 FR 1.0 persona, §6.2 {@code AgentPersona} example.
 * <b>User stories:</b> US-004 (on-brand script), US-005 ({@code characterReferenceId}).</p>
 *
 * @param id                    persona id
 * @param name                  display name
 * @param voiceTraits           tone descriptors
 * @param directives            hard constraints
 * @param backstory             narrative context for LLM
 * @param characterReferenceId  visual consistency id for image MCP tools
 */
public record AgentPersona(
        UUID id,
        String name,
        List<String> voiceTraits,
        List<String> directives,
        String backstory,
        String characterReferenceId
) {
}
