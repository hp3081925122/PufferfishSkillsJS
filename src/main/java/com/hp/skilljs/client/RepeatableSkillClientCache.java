package com.hp.skilljs.client;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RepeatableSkillClientCache {
    private static final Map<ResourceLocation, Map<String, Entry>> CATEGORY_ENTRIES = new ConcurrentHashMap<>();

    private RepeatableSkillClientCache() {
    }

    public static void syncCategory(ResourceLocation categoryId, Map<String, Entry> entries) {
        CATEGORY_ENTRIES.remove(categoryId);
        if (entries.isEmpty()) {
            return;
        }

        CATEGORY_ENTRIES.put(categoryId, new ConcurrentHashMap<>(new LinkedHashMap<>(entries)));
    }

    public static Entry get(ResourceLocation categoryId, String skillId) {
        return CATEGORY_ENTRIES.getOrDefault(categoryId, Map.of()).get(skillId);
    }

    public record Entry(boolean repeatable, int count, int limit) {
        public int remainingRepeats() {
            if (this.limit <= 0) {
                return -1;
            }

            return Math.max(0, this.limit - this.count);
        }
    }
}
