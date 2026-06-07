package com.hp.skilljs.integration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Experience;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Objects;
import java.util.Optional;

public class ExperienceAPIWrapper {
    private ResourceLocation parseResourceLocation(String value) {
        return Objects.requireNonNull(ResourceLocation.tryParse(value), () -> "Invalid resource location: " + value);
    }

    public Optional<Experience> getExperience(String categoryId) {
        return SkillsAPI.getCategory(parseResourceLocation(categoryId)).flatMap(Category::getExperience);
    }

    public int getTotalExperience(ServerPlayer player, String categoryId) {
        return getExperience(categoryId).map(exp -> exp.getTotal(player)).orElse(0);
    }

    public void setTotalExperience(ServerPlayer player, String categoryId, int amount) {
        getExperience(categoryId).ifPresent(exp -> exp.setTotal(player, amount));
    }

    public void addExperience(ServerPlayer player, String categoryId, int amount) {
        getExperience(categoryId).ifPresent(exp -> exp.addTotal(player, amount));
    }

    public void removeExperience(ServerPlayer player, String categoryId, int amount) {
        addExperience(player, categoryId, -amount);
    }

    public int getLevel(ServerPlayer player, String categoryId) {
        return getExperience(categoryId).map(exp -> exp.getLevel(player)).orElse(0);
    }

    public int getCurrentExperience(ServerPlayer player, String categoryId) {
        return getExperience(categoryId).map(exp -> exp.getCurrent(player)).orElse(0);
    }

    public int getRequiredForNextLevel(ServerPlayer player, String categoryId) {
        int currentLevel = getLevel(player, categoryId);
        return getRequiredForLevel(categoryId, currentLevel + 1);
    }

    public int getRequiredForLevel(String categoryId, int level) {
        return getExperience(categoryId).map(exp -> exp.getRequired(level)).orElse(0);
    }

    public int getRequiredTotalForLevel(String categoryId, int level) {
        return getExperience(categoryId).map(exp -> exp.getRequiredTotal(level)).orElse(0);
    }

    public void setLevel(ServerPlayer player, String categoryId, int level) {
        int requiredTotal = getRequiredTotalForLevel(categoryId, level);
        setTotalExperience(player, categoryId, requiredTotal);
    }

    public boolean levelUp(ServerPlayer player, String categoryId) {
        return getExperience(categoryId).map(exp -> {
            int currentLevel = exp.getLevel(player);
            int nextLevelRequired = exp.getRequiredTotal(currentLevel + 1);
            if (exp.getTotal(player) >= nextLevelRequired) {
                return false;
            }

            exp.setTotal(player, nextLevelRequired);
            return true;
        }).orElse(false);
    }

    public double getProgressToNextLevel(ServerPlayer player, String categoryId) {
        return getExperience(categoryId).map(exp -> {
            int currentLevel = exp.getLevel(player);
            int currentLevelRequired = exp.getRequiredTotal(currentLevel);
            int nextLevelRequired = exp.getRequiredTotal(currentLevel + 1);
            int currentExp = exp.getTotal(player);

            if (nextLevelRequired <= currentLevelRequired) {
                return 100.0;
            }

            int needed = nextLevelRequired - currentLevelRequired;
            int have = currentExp - currentLevelRequired;
            return Math.min(100.0, (double) have / needed * 100.0);
        }).orElse(0.0);
    }
}