package com.hp.skilljs.integration;

import com.hp.skilljs.client.RepeatableSkillClientCache;
import com.hp.skilljs.mixin.SkillsModAccessor;
import com.hp.skilljs.network.PufferfishSkillsJSNetwork;
import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.network.packets.out.PointsUpdateOutPacket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class RepeatableSkillSupport {
    private RepeatableSkillSupport() {
    }

    public static Optional<CategoryConfig> getCategoryConfig(Identifier categoryId) {
        SkillsMod skillsMod = SkillsMod.getInstance();
        if (skillsMod == null) {
            return Optional.empty();
        }

        return ((SkillsModAccessor) skillsMod).skilljs$invokeGetCategory(categoryId);
    }

    public static int getExtraSpentPoints(ServerPlayer player, Identifier categoryId) {
        return getCategoryConfig(categoryId)
            .map(categoryConfig -> RepeatableSkillData.getExtraSpentPoints(player, categoryConfig))
            .orElse(0);
    }

    public static int getEffectiveSpentPoints(ServerPlayer player, Identifier categoryId, int baseSpentPoints) {
        return baseSpentPoints + getExtraSpentPoints(player, categoryId);
    }

    public static int getEffectivePointsLeft(ServerPlayer player, Identifier categoryId, int basePointsLeft) {
        return basePointsLeft - getExtraSpentPoints(player, categoryId);
    }

    public static int getRepeatLimit(Identifier categoryId, String skillId) {
        return SkillTypeRegistry.getRepeatLimit(categoryId, skillId);
    }

    public static boolean hasRepeatLimit(Identifier categoryId, String skillId) {
        return SkillTypeRegistry.hasRepeatLimit(categoryId, skillId);
    }

    public static boolean tryRepeatUnlock(ServerPlayer player, Identifier categoryId, String skillId) {
        SkillsMod skillsMod = SkillsMod.getInstance();
        if (skillsMod == null) {
            return false;
        }

        if (!SkillTypeRegistry.isRepeatable(categoryId, skillId) || !isSkillUnlocked(player, categoryId, skillId)) {
            return false;
        }

        int before = RepeatableSkillData.getRepeatCount(player, categoryId, skillId);
        // force=false：未解锁的技能不会被顺带强制解锁
        skillsMod.tryUnlockSkill(player, categoryId, skillId, false);
        int after = RepeatableSkillData.getRepeatCount(player, categoryId, skillId);
        return after > before;
    }

    public static void syncPoints(ServerPlayer player, Identifier categoryId) {
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

    public static void syncRepeatableState(ServerPlayer player, CategoryConfig categoryConfig) {
        Map<String, RepeatableSkillClientCache.Entry> entries = new LinkedHashMap<>();
        for (SkillConfig skillConfig : categoryConfig.skills().getAll()) {
            if (!SkillTypeRegistry.isRepeatable(categoryConfig.id(), skillConfig.id())) {
                continue;
            }

            entries.put(
                skillConfig.id(),
                new RepeatableSkillClientCache.Entry(
                    true,
                    RepeatableSkillData.getRepeatCount(player, categoryConfig.id(), skillConfig.id()),
                    RepeatableSkillData.getRepeatLimit(categoryConfig.id(), skillConfig.id())
                )
            );
        }

        PufferfishSkillsJSNetwork.syncCategory(player, categoryConfig.id(), entries);
    }

    public static boolean isSkillUnlocked(ServerPlayer player, Identifier categoryId, String skillId) {
        SkillsMod skillsMod = SkillsMod.getInstance();
        if (skillsMod == null) {
            return false;
        }

        SkillsModAccessor accessor = (SkillsModAccessor) skillsMod;
        return accessor.skilljs$invokeGetCategory(categoryId)
            .map(categoryConfig -> {
                PlayerData playerData = accessor.skilljs$invokeGetPlayerData(player);
                return playerData.isCategoryUnlocked(categoryConfig)
                    && playerData.getOrCreateCategoryData(categoryConfig).getUnlockedSkillIds().contains(skillId);
            })
            .orElse(false);
    }

    /**
     * 重复次数被脚本直接修改后，同步点数、客户端显示，并重新计算奖励。
     */
    public static void refresh(ServerPlayer player, Identifier categoryId) {
        syncPoints(player, categoryId);
        syncRepeatableState(player, categoryId);
        SkillsMod skillsMod = SkillsMod.getInstance();
        if (skillsMod != null) {
            skillsMod.updateRewards(player, reward -> true);
        }
    }

    public static void syncRepeatableState(ServerPlayer player, Identifier categoryId) {
        getCategoryConfig(categoryId).ifPresent(categoryConfig -> syncRepeatableState(player, categoryConfig));
    }

    public static void clearRepeatableState(ServerPlayer player, Identifier categoryId) {
        PufferfishSkillsJSNetwork.syncCategory(player, categoryId, Map.of());
    }
}
