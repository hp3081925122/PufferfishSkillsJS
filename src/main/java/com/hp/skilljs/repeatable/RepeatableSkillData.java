package com.hp.skilljs.repeatable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.server.data.CategoryData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RepeatableSkillData {
    private static final String ROOT_KEY = "pufferfishskillsjs_repeatable";
    private static final String COUNTS_KEY = "counts";
    private static final Map<UUID, Map<ResourceLocation, Map<String, Integer>>> PLAYER_COUNTS = new ConcurrentHashMap<>();

    private RepeatableSkillData() {
    }

    private static Map<ResourceLocation, Map<String, Integer>> getPlayerMap(ServerPlayer player) {
        return PLAYER_COUNTS.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>());
    }

    private static Map<String, Integer> getCategoryMap(ServerPlayer player, ResourceLocation categoryId) {
        return getPlayerMap(player).computeIfAbsent(categoryId, id -> new ConcurrentHashMap<>());
    }

    public static int getRepeatCount(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        return getPlayerMap(player).getOrDefault(categoryId, Map.of()).getOrDefault(skillId, 0);
    }

    public static void setRepeatCount(ServerPlayer player, ResourceLocation categoryId, String skillId, int count) {
        if (count <= 0) {
            clearSkill(player, categoryId, skillId);
            return;
        }

        getCategoryMap(player, categoryId).put(skillId, count);
    }

    public static int incrementRepeatCount(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        Map<String, Integer> categoryMap = getCategoryMap(player, categoryId);
        int count = categoryMap.getOrDefault(skillId, 0) + 1;
        categoryMap.put(skillId, count);
        return count;
    }

    public static void ensureInitialUnlock(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        if (getRepeatCount(player, categoryId, skillId) <= 0) {
            setRepeatCount(player, categoryId, skillId, 1);
        }
    }

    public static void clearSkill(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        Map<String, Integer> categoryMap = getPlayerMap(player).get(categoryId);
        if (categoryMap == null) {
            return;
        }

        categoryMap.remove(skillId);
        if (categoryMap.isEmpty()) {
            getPlayerMap(player).remove(categoryId);
        }
    }

    public static void clearCategory(ServerPlayer player, ResourceLocation categoryId) {
        getPlayerMap(player).remove(categoryId);
    }

    public static void clearAll(ServerPlayer player) {
        PLAYER_COUNTS.remove(player.getUUID());
    }

    public static int getExtraSpentPoints(ServerPlayer player, CategoryConfig categoryConfig) {
        Map<String, Integer> categoryMap = getPlayerMap(player).get(categoryConfig.id());
        if (categoryMap == null || categoryMap.isEmpty()) {
            return 0;
        }

        int extraSpent = 0;
        for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
            int count = entry.getValue();
            if (count <= 1 || !SkillTypeRegistry.isRepeatable(categoryConfig.id(), entry.getKey())) {
                continue;
            }

            SkillDefinitionConfig definition = getDefinition(categoryConfig, entry.getKey());
            if (definition == null) {
                continue;
            }

            extraSpent += Math.max(0, definition.cost()) * (count - 1);
        }

        return extraSpent;
    }

    public static int getEffectiveSpentPoints(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData) {
        return categoryData.getSpentPoints(categoryConfig) + getExtraSpentPoints(player, categoryConfig);
    }

    public static int getEffectivePointsLeft(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData) {
        int availableTotal = Math.min(categoryData.getPointsTotal(), categoryConfig.general().spentPointsLimit());
        return availableTotal - getEffectiveSpentPoints(player, categoryConfig, categoryData);
    }

    public static boolean canRepeatUnlock(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData, SkillConfig skillConfig) {
        if (!SkillTypeRegistry.isRepeatable(categoryConfig.id(), skillConfig.id())) {
            return false;
        }

        if (!categoryData.getUnlockedSkillIds().contains(skillConfig.id())) {
            return false;
        }

        if (hasReachedRepeatLimit(player, categoryConfig.id(), skillConfig.id())) {
            return false;
        }

        SkillDefinitionConfig definition = getDefinition(categoryConfig, skillConfig.id());
        if (definition == null) {
            return false;
        }

        return getEffectivePointsLeft(player, categoryConfig, categoryData) >= Math.max(0, definition.cost());
    }

    public static SkillDefinitionConfig getDefinition(CategoryConfig categoryConfig, String skillId) {
        return categoryConfig.skills().getById(skillId)
            .flatMap(skill -> categoryConfig.definitions().getById(skill.definitionId()))
            .orElse(null);
    }

    public static int getRepeatLimit(ResourceLocation categoryId, String skillId) {
        return SkillTypeRegistry.getRepeatLimit(categoryId, skillId);
    }

    public static boolean hasRepeatLimit(ResourceLocation categoryId, String skillId) {
        return SkillTypeRegistry.hasRepeatLimit(categoryId, skillId);
    }

    public static boolean hasReachedRepeatLimit(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        int limit = getRepeatLimit(categoryId, skillId);
        return limit > 0 && getRepeatCount(player, categoryId, skillId) >= limit;
    }

    public static int getRemainingRepeats(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        int limit = getRepeatLimit(categoryId, skillId);
        if (limit <= 0) {
            return -1;
        }

        return Math.max(0, limit - getRepeatCount(player, categoryId, skillId));
    }

    public static CompoundTag writeToTag(ServerPlayer player, CompoundTag targetTag) {
        CompoundTag rootTag = new CompoundTag();
        Map<ResourceLocation, Map<String, Integer>> playerMap = getPlayerMap(player);
        for (Map.Entry<ResourceLocation, Map<String, Integer>> categoryEntry : playerMap.entrySet()) {
            CompoundTag categoryTag = new CompoundTag();
            CompoundTag countsTag = new CompoundTag();

            for (Map.Entry<String, Integer> skillEntry : categoryEntry.getValue().entrySet()) {
                if (skillEntry.getValue() > 0) {
                    countsTag.putInt(skillEntry.getKey(), skillEntry.getValue());
                }
            }

            if (!countsTag.isEmpty()) {
                categoryTag.put(COUNTS_KEY, countsTag);
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

        CompoundTag rootTag = sourceTag.getCompound(ROOT_KEY);
        Map<ResourceLocation, Map<String, Integer>> playerMap = new HashMap<>();

        for (String categoryKey : rootTag.getAllKeys()) {
            ResourceLocation categoryId = ResourceLocation.tryParse(categoryKey);
            if (categoryId == null) {
                continue;
            }

            CompoundTag categoryTag = rootTag.getCompound(categoryKey);
            if (!categoryTag.contains(COUNTS_KEY, 10)) {
                continue;
            }

            CompoundTag countsTag = categoryTag.getCompound(COUNTS_KEY);
            Map<String, Integer> counts = new HashMap<>();
            for (String skillId : countsTag.getAllKeys()) {
                int count = countsTag.getInt(skillId);
                if (count > 0) {
                    counts.put(skillId, count);
                }
            }

            if (!counts.isEmpty()) {
                playerMap.put(categoryId, new ConcurrentHashMap<>(counts));
            }
        }

        if (!playerMap.isEmpty()) {
            PLAYER_COUNTS.put(player.getUUID(), new ConcurrentHashMap<>(playerMap));
        }
    }
}
