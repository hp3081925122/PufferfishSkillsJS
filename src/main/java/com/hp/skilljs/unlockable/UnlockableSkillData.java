package com.hp.skilljs.unlockable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UnlockableSkillData {
    private static final Map<UUID, Map<ResourceLocation, Set<String>>> PLAYER_SKILLS = new ConcurrentHashMap<>();

    private UnlockableSkillData() {
    }

    private static Map<ResourceLocation, Set<String>> getPlayerMap(ServerPlayer player) {
        return PLAYER_SKILLS.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>());
    }

    private static Set<String> getCategorySet(ServerPlayer player, ResourceLocation categoryId) {
        return getPlayerMap(player).computeIfAbsent(categoryId, id -> ConcurrentHashMap.newKeySet());
    }

    public static boolean allow(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        return getCategorySet(player, categoryId).add(skillId);
    }

    public static boolean disallow(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        Map<ResourceLocation, Set<String>> playerMap = getPlayerMap(player);
        Set<String> skills = playerMap.get(categoryId);
        if (skills == null) {
            return false;
        }

        boolean removed = skills.remove(skillId);
        if (skills.isEmpty()) {
            playerMap.remove(categoryId);
        }
        return removed;
    }

    public static boolean isAllowed(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        return getPlayerMap(player).getOrDefault(categoryId, Set.of()).contains(skillId);
    }

    public static Set<String> getAllowedSkills(ServerPlayer player, ResourceLocation categoryId) {
        return new HashSet<>(getPlayerMap(player).getOrDefault(categoryId, Set.of()));
    }

    public static void clearSkill(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        disallow(player, categoryId, skillId);
    }

    public static void clearCategory(ServerPlayer player, ResourceLocation categoryId) {
        getPlayerMap(player).remove(categoryId);
    }

    public static void clearAll(ServerPlayer player) {
        PLAYER_SKILLS.remove(player.getUUID());
    }
}
