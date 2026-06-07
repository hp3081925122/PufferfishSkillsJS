package com.hp.skilljs.integration;

import com.hp.skilljs.mixin.SkillsModAccessor;
import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.network.packets.out.PointsUpdateOutPacket;

import java.util.Optional;

public final class RepeatableSkillSupport {
    private RepeatableSkillSupport() {
    }

    public static Optional<CategoryConfig> getCategoryConfig(ResourceLocation categoryId) {
        SkillsMod skillsMod = SkillsMod.getInstance();
        if (skillsMod == null) {
            return Optional.empty();
        }

        return ((SkillsModAccessor) skillsMod).skilljs$invokeGetCategory(categoryId);
    }

    public static int getExtraSpentPoints(ServerPlayer player, ResourceLocation categoryId) {
        return getCategoryConfig(categoryId)
            .map(categoryConfig -> RepeatableSkillData.getExtraSpentPoints(player, categoryConfig))
            .orElse(0);
    }

    public static int getEffectiveSpentPoints(ServerPlayer player, ResourceLocation categoryId, int baseSpentPoints) {
        return baseSpentPoints + getExtraSpentPoints(player, categoryId);
    }

    public static int getEffectivePointsLeft(ServerPlayer player, ResourceLocation categoryId, int basePointsLeft) {
        return basePointsLeft - getExtraSpentPoints(player, categoryId);
    }

    public static int getRepeatLimit(ResourceLocation categoryId, String skillId) {
        return SkillTypeRegistry.getRepeatLimit(categoryId, skillId);
    }

    public static boolean hasRepeatLimit(ResourceLocation categoryId, String skillId) {
        return SkillTypeRegistry.hasRepeatLimit(categoryId, skillId);
    }

    public static boolean tryRepeatUnlock(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        SkillsMod skillsMod = SkillsMod.getInstance();
        if (skillsMod == null) {
            return false;
        }

        int before = RepeatableSkillData.getRepeatCount(player, categoryId, skillId);
        skillsMod.tryUnlockSkill(player, categoryId, skillId, true);
        int after = RepeatableSkillData.getRepeatCount(player, categoryId, skillId);
        return after > before;
    }

    public static void syncPoints(ServerPlayer player, ResourceLocation categoryId) {
        SkillsMod skillsMod = SkillsMod.getInstance();
        if (skillsMod == null) {
            return;
        }

        SkillsModAccessor accessor = (SkillsModAccessor) skillsMod;
        Optional<CategoryConfig> categoryConfig = accessor.skilljs$invokeGetCategory(categoryId);
        if (categoryConfig.isEmpty()) {
            return;
        }

        PlayerData playerData = accessor.skilljs$invokeGetPlayerData(player);
        if (!playerData.isCategoryUnlocked(categoryConfig.get())) {
            return;
        }

        CategoryData categoryData = playerData.getOrCreateCategoryData(categoryConfig.get());
        int spentPoints = RepeatableSkillData.getEffectiveSpentPoints(player, categoryConfig.get(), categoryData);
        accessor.skilljs$getPacketSender().send(
            player,
            new PointsUpdateOutPacket(categoryId, spentPoints, categoryData.getPointsTotal())
        );
    }
}
