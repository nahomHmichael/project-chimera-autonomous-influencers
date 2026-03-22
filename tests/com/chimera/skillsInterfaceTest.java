package com.chimera;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chimera.skill.Skill;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for {@link Skill} and runtime registration of skill implementations.
 *
 * <p><b>Specs:</b> {@code specs/technical.md} §1 (DTO boundaries for skill I/O), MCP tool orchestration via skills
 * (architecture strategy §7).
 * <b>Architecture:</b> {@code research/architecture_strategy.md} §7 (Agent Skills vs MCP servers).
 * <b>User stories:</b> US-004–US-007 (skill-sized workflows: script, media, publish paths).</p>
 *
 * <p>These tests fail until concrete {@link Skill} implementations are registered (e.g. via
 * {@code META-INF/services/com.chimera.skill.Skill}) matching the {@code skills/} directory contracts.</p>
 */
@DisplayName("Skill interface and registration contract")
class skillsInterfaceTest {

    private static final Pattern SEMVER = Pattern.compile("\\d+\\.\\d+\\.\\d+.*");

    /**
     * Minimum skill ids expected from repository {@code skills/} READMEs (contract names).
     */
    private static final List<String> REQUIRED_SKILL_IDS =
            List.of("skill_fetch_trends", "skill_generate_content", "skill_post_content");

    @Test
    @DisplayName("Skill interface exposes execute, skillId, and version (binary contract)")
    void skillInterfaceDeclaresCoreMethods() throws NoSuchMethodException {
        assertNotNull(Skill.class.getMethod("execute", Object.class));
        assertNotNull(Skill.class.getMethod("skillId"));
        assertNotNull(Skill.class.getMethod("version"));
    }

    @Test
    @DisplayName("ServiceLoader exposes all skills documented under skills/ (SPI empty slot)")
    void serviceLoaderRegistersAllDocumentedSkills() {
        List<Skill<?, ?>> found = new ArrayList<>();
        ServiceLoader.load(Skill.class).stream().map(ServiceLoader.Provider::get).forEach(found::add);
        assertFalse(
                found.isEmpty(),
                "Register Skill implementations with META-INF/services/com.chimera.skill.Skill — "
                        + "see skills/skill_fetch_trends, skill_generate_content, skill_post_content READMEs");
        List<String> missing = new ArrayList<>(REQUIRED_SKILL_IDS);
        for (Skill<?, ?> s : found) {
            missing.removeIf(id -> id.equals(s.skillId()));
        }
        assertTrue(
                missing.isEmpty(),
                "Missing Skill implementations for ids: " + missing + " (SPI must list each concrete class)");
    }

    @Test
    @DisplayName("Registered skills return non-blank ids and semver-shaped versions")
    void skillMetadataIsWellFormed() {
        List<Skill<?, ?>> found = new ArrayList<>();
        ServiceLoader.load(Skill.class).stream().map(ServiceLoader.Provider::get).forEach(found::add);
        assertFalse(found.isEmpty(), "no Skill implementations to validate — register SPI entries first");
        for (Skill<?, ?> s : found) {
            assertFalse(s.skillId().isBlank(), "skillId must be non-blank");
            assertTrue(SEMVER.matcher(s.version()).find(), "version should follow semver: " + s.version());
        }
    }
}
