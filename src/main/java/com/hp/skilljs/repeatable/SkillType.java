package com.hp.skilljs.repeatable;

import java.util.Locale;

public enum SkillType {
    NORMAL("normal"),
    REPEATABLE("repeatable");

    private final String id;

    SkillType(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public static SkillType fromName(String name) {
        if (name == null || name.isBlank()) {
            return NORMAL;
        }

        String normalized = name.toLowerCase(Locale.ROOT);
        for (SkillType type : values()) {
            if (type.id.equals(normalized)) {
                return type;
            }
        }

        return NORMAL;
    }
}
