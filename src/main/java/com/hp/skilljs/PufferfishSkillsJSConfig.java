package com.hp.skilljs;

import net.minecraftforge.common.ForgeConfigSpec;

public final class PufferfishSkillsJSConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue COMPRESS_ATTRIBUTE_REWARDS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMPRESS_ATTRIBUTE_REWARDS = builder
            .define("compressAttributeRewards", false);
        SPEC = builder.build();
    }

    private PufferfishSkillsJSConfig() {
    }
}
