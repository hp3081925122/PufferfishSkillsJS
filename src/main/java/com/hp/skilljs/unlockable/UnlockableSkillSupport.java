package com.hp.skilljs.unlockable;

import com.hp.skilljs.mixin.SkillsModAccessor;
import com.hp.skilljs.network.PufferfishSkillsJSNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.server.data.PlayerData;

import java.util.Optional;
import java.util.Set;

public final class UnlockableSkillSupport {
    private UnlockableSkillSupport() {
    }

    public static boolean allowSkillUnlock(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        Optional<CategoryConfig> categoryConfig = getCategoryConfig(categoryId);
        if (categoryConfig.isEmpty() || categoryConfig.get().skills().getById(skillId).isEmpty()) {
            return false;
        }

        boolean changed = UnlockableSkillData.allow(player, categoryId, skillId);
        syncCategory(player, categoryId);
        return changed;
    }

    public static boolean disallowSkillUnlock(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        boolean changed = UnlockableSkillData.disallow(player, categoryId, skillId);
        syncCategory(player, categoryId);
        return changed;
    }

    public static boolean canUnlockAllowedSkill(
        ServerPlayer player,
        CategoryConfig categoryConfig,
        CategoryData categoryData,
        SkillConfig skillConfig
    ) {
        if (!UnlockableSkillData.isAllowed(player, categoryConfig.id(), skillConfig.id())) {
            return false;
        }

        SkillDefinitionConfig definition = categoryConfig.definitions().getById(skillConfig.definitionId()).orElse(null);
        if (definition == null) {
            return false;
        }

        if (categoryData.getUnlockedSkillIds().contains(skillConfig.id())) {
            return false;
        }

        return categoryData.getPointsLeft(categoryConfig) >= Math.max(definition.requiredPoints(), definition.cost())
            && categoryData.getSpentPoints(categoryConfig) >= definition.requiredSpentPoints();
    }

    public static void syncCategory(ServerPlayer player, ResourceLocation categoryId) {
        PufferfishSkillsJSNetwork.syncUnlockableCategory(player, categoryId, UnlockableSkillData.getAllowedSkills(player, categoryId));
    }

    public static void clearSkill(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        UnlockableSkillData.clearSkill(player, categoryId, skillId);
        syncCategory(player, categoryId);
    }

    public static void clearCategory(ServerPlayer player, ResourceLocation categoryId) {
        UnlockableSkillData.clearCategory(player, categoryId);
        PufferfishSkillsJSNetwork.syncUnlockableCategory(player, categoryId, Set.of());
    }

    private static Optional<CategoryConfig> getCategoryConfig(ResourceLocation categoryId) {
        SkillsMod skillsMod = SkillsMod.getInstance();
        if (skillsMod == null) {
            return Optional.empty();
        }

        return ((SkillsModAccessor) skillsMod).skilljs$invokeGetCategory(categoryId);
    }

    public static boolean isCategoryUnlocked(ServerPlayer player, CategoryConfig categoryConfig) {
        SkillsMod skillsMod = SkillsMod.getInstance();
        if (skillsMod == null) {
            return false;
        }

        PlayerData playerData = ((SkillsModAccessor) skillsMod).skilljs$invokeGetPlayerData(player);
        return playerData.isCategoryUnlocked(categoryConfig);
    }
}
