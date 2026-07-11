package com.hp.skilljs.client;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class UnlockableSkillClientCache {
    private static final Map<ResourceLocation, Set<String>> CATEGORY_SKILLS = new ConcurrentHashMap<>();

    private UnlockableSkillClientCache() {
    }

    public static void syncCategory(ResourceLocation categoryId, Set<String> skills) {
        CATEGORY_SKILLS.remove(categoryId);
        if (skills.isEmpty()) {
            return;
        }

        CATEGORY_SKILLS.put(categoryId, ConcurrentHashMap.newKeySet(skills.size()));
        CATEGORY_SKILLS.get(categoryId).addAll(new HashSet<>(skills));
    }

    public static boolean isAllowed(ResourceLocation categoryId, String skillId) {
        return CATEGORY_SKILLS.getOrDefault(categoryId, Set.of()).contains(skillId);
    }
}
