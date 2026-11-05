package com.hp.skilljs;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PufferfishSkillsJSConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue COMPRESS_ATTRIBUTE_REWARDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMPRESS_ATTRIBUTE_REWARDS = builder
            .define("compressAttributeRewards", false);
        SPEC = builder.build();
    }

    private PufferfishSkillsJSConfig() {
    }
}
