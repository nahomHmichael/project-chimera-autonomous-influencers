package com.chimera.model;

import java.util.List;

/** Task context payload — SRS §6.2 */
public record TaskContext(
    String goalDescription,
    List<String> personaConstraints,
    List<String> requiredResources
) {}
