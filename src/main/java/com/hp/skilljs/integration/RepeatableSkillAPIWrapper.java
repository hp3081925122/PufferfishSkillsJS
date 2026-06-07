package com.hp.skilljs.integration;

import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.SkillType;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class RepeatableSkillAPIWrapper {
    private ResourceLocation parseResourceLocation(String value) {
        return Objects.requireNonNull(ResourceLocation.tryParse(value), () -> "Invalid resource location: " + value);
    }

    public void setSkillType(String categoryId, String skillId, String typeName) {
        SkillTypeRegistry.setSkillType(parseResourceLocation(categoryId), skillId, typeName);
    }

    public void setRepeatable(String categoryId, String skillId, boolean repeatable) {
        SkillTypeRegistry.setSkillType(parseResourceLocation(categoryId), skillId, repeatable ? SkillType.REPEATABLE : SkillType.NORMAL);
    }

    public void setRepeatable(String categoryId, String skillId, boolean repeatable, int limit) {
        ResourceLocation categoryKey = parseResourceLocation(categoryId);
        SkillTypeRegistry.setSkillType(categoryKey, skillId, repeatable ? SkillType.REPEATABLE : SkillType.NORMAL);
        SkillTypeRegistry.setRepeatLimit(categoryKey, skillId, repeatable ? limit : 0);
    }

    public String getSkillType(String categoryId, String skillId) {
        return SkillTypeRegistry.getSkillType(parseResourceLocation(categoryId), skillId).getId();
    }

    public boolean isRepeatable(String categoryId, String skillId) {
        return SkillTypeRegistry.isRepeatable(parseResourceLocation(categoryId), skillId);
    }

    public void setRepeatLimit(String categoryId, String skillId, int limit) {
        SkillTypeRegistry.setRepeatLimit(parseResourceLocation(categoryId), skillId, limit);
    }

    public int getRepeatLimit(String categoryId, String skillId) {
        return SkillTypeRegistry.getRepeatLimit(parseResourceLocation(categoryId), skillId);
    }

    public boolean hasRepeatLimit(String categoryId, String skillId) {
        return SkillTypeRegistry.hasRepeatLimit(parseResourceLocation(categoryId), skillId);
    }

    public int getRepeatCount(ServerPlayer player, String categoryId, String skillId) {
        return RepeatableSkillData.getRepeatCount(player, parseResourceLocation(categoryId), skillId);
    }

    public boolean hasReachedRepeatLimit(ServerPlayer player, String categoryId, String skillId) {
        return RepeatableSkillData.hasReachedRepeatLimit(player, parseResourceLocation(categoryId), skillId);
    }

    public int getRemainingRepeats(ServerPlayer player, String categoryId, String skillId) {
        return RepeatableSkillData.getRemainingRepeats(player, parseResourceLocation(categoryId), skillId);
    }

    public void setRepeatCount(ServerPlayer player, String categoryId, String skillId, int count) {
        ResourceLocation categoryKey = parseResourceLocation(categoryId);
        RepeatableSkillData.setRepeatCount(player, categoryKey, skillId, count);
        RepeatableSkillSupport.syncPoints(player, categoryKey);
    }

    public void clearRepeatData(ServerPlayer player, String categoryId, String skillId) {
        ResourceLocation categoryKey = parseResourceLocation(categoryId);
        RepeatableSkillData.clearSkill(player, categoryKey, skillId);
        RepeatableSkillSupport.syncPoints(player, categoryKey);
    }

    public void clearCategoryData(ServerPlayer player, String categoryId) {
        ResourceLocation categoryKey = parseResourceLocation(categoryId);
        RepeatableSkillData.clearCategory(player, categoryKey);
        RepeatableSkillSupport.syncPoints(player, categoryKey);
    }

    public void clearAllData(ServerPlayer player) {
        RepeatableSkillData.clearAll(player);
    }

    public boolean repeatUnlock(ServerPlayer player, String categoryId, String skillId) {
        return RepeatableSkillSupport.tryRepeatUnlock(player, parseResourceLocation(categoryId), skillId);
    }
}
