package com.hp.skilljs.unlockable;

import com.hp.skilljs.PufferfishSkillsJSMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UnlockableSkillData {
    private static final String ROOT_KEY = "pufferfishskillsjs_unlockable";
    private static final String SKILLS_KEY = "skills";
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
        boolean changed = getCategorySet(player, categoryId).add(skillId);
        if (changed) {
            PufferfishSkillsJSMod.LOGGER.debug("Allowed skill unlock player={} category={} skill={}", player.getGameProfile().getName(), categoryId, skillId);
        }
        return changed;
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

    public static CompoundTag writeToTag(ServerPlayer player, CompoundTag targetTag) {
        CompoundTag rootTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Set<String>> categoryEntry : getPlayerMap(player).entrySet()) {
            CompoundTag skillsTag = new CompoundTag();
            for (String skillId : categoryEntry.getValue()) {
                skillsTag.putBoolean(skillId, true);
            }

            if (!skillsTag.isEmpty()) {
                CompoundTag categoryTag = new CompoundTag();
                categoryTag.put(SKILLS_KEY, skillsTag);
                rootTag.put(categoryEntry.getKey().toString(), categoryTag);
            }
        }

        if (rootTag.isEmpty()) {
            targetTag.remove(ROOT_KEY);
        } else {
            targetTag.put(ROOT_KEY, rootTag);
        }

        return targetTag;
    }

    public static void readFromTag(ServerPlayer player, CompoundTag sourceTag) {
        clearAll(player);
        if (!sourceTag.contains(ROOT_KEY, 10)) {
            return;
        }

        Map<ResourceLocation, Set<String>> playerMap = new HashMap<>();
        CompoundTag rootTag = sourceTag.getCompound(ROOT_KEY);
        for (String categoryKey : rootTag.getAllKeys()) {
            ResourceLocation categoryId = ResourceLocation.tryParse(categoryKey);
            if (categoryId == null) {
                continue;
            }

            CompoundTag categoryTag = rootTag.getCompound(categoryKey);
            if (!categoryTag.contains(SKILLS_KEY, 10)) {
                continue;
            }

            Set<String> skills = ConcurrentHashMap.newKeySet();
            CompoundTag skillsTag = categoryTag.getCompound(SKILLS_KEY);
            for (String skillId : skillsTag.getAllKeys()) {
                if (skillsTag.getBoolean(skillId)) {
                    skills.add(skillId);
                }
            }

            if (!skills.isEmpty()) {
                playerMap.put(categoryId, skills);
            }
        }

        if (!playerMap.isEmpty()) {
            PLAYER_SKILLS.put(player.getUUID(), new ConcurrentHashMap<>(playerMap));
        }
    }
}
