package com.hp.skilljs.repeatable;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillTypeRegistry {
    private static final Map<String, SkillType> SKILL_TYPES = new ConcurrentHashMap<>();
    private static final Map<String, Integer> REPEAT_LIMITS = new ConcurrentHashMap<>();

    private SkillTypeRegistry() {
    }

    private static String key(ResourceLocation categoryId, String skillId) {
        return categoryId + "|" + skillId;
    }

    public static void setSkillType(ResourceLocation categoryId, String skillId, SkillType type) {
        if (type == SkillType.NORMAL) {
            SKILL_TYPES.remove(key(categoryId, skillId));
            REPEAT_LIMITS.remove(key(categoryId, skillId));
            return;
        }

        SKILL_TYPES.put(key(categoryId, skillId), type);
    }

    public static void setSkillType(ResourceLocation categoryId, String skillId, String typeName) {
        setSkillType(categoryId, skillId, SkillType.fromName(typeName));
    }

    public static SkillType getSkillType(ResourceLocation categoryId, String skillId) {
        return SKILL_TYPES.getOrDefault(key(categoryId, skillId), SkillType.NORMAL);
    }

    public static boolean isRepeatable(ResourceLocation categoryId, String skillId) {
        return getSkillType(categoryId, skillId) == SkillType.REPEATABLE;
    }

    public static void setRepeatLimit(ResourceLocation categoryId, String skillId, int limit) {
        String key = key(categoryId, skillId);
        if (limit <= 0) {
            REPEAT_LIMITS.remove(key);
            return;
        }

        REPEAT_LIMITS.put(key, limit);
    }

    public static int getRepeatLimit(ResourceLocation categoryId, String skillId) {
        return REPEAT_LIMITS.getOrDefault(key(categoryId, skillId), 0);
    }

    public static boolean hasRepeatLimit(ResourceLocation categoryId, String skillId) {
        return getRepeatLimit(categoryId, skillId) > 0;
    }

    public static void clearSkillType(ResourceLocation categoryId, String skillId) {
        SKILL_TYPES.remove(key(categoryId, skillId));
        REPEAT_LIMITS.remove(key(categoryId, skillId));
    }

    public static void clearAll() {
        SKILL_TYPES.clear();
        REPEAT_LIMITS.clear();
    }
}
