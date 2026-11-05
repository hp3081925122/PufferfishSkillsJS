package com.hp.skilljs.integration;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Experience;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Objects;
import java.util.Optional;

public class ExperienceAPIWrapper {
    private Identifier parseResourceLocation(String value) {
        return Objects.requireNonNull(Identifier.tryParse(value), () -> "Invalid resource location: " + value);
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

    public void setExperience(ServerPlayer player, String categoryId, int amount) {
        setTotalExperience(player, categoryId, amount);
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
        return getExperience(categoryId).map(exp -> ExperienceMath.requiredForNextLevel(exp, player)).orElse(0);
    }

    public int getRequiredForLevel(String categoryId, int level) {
        return getExperience(categoryId).map(exp -> exp.getRequired(level)).orElse(0);
    }

    public int getRequiredTotalForLevel(String categoryId, int level) {
        return getExperience(categoryId).map(exp -> exp.getRequiredTotal(level)).orElse(0);
    }

    public void setLevel(ServerPlayer player, String categoryId, int level) {
        getExperience(categoryId).ifPresent(exp -> exp.setLevel(player, Math.max(0, level)));
    }

    public boolean levelUp(ServerPlayer player, String categoryId) {
        return getExperience(categoryId).map(exp -> ExperienceMath.levelUp(exp, player)).orElse(false);
    }

    public double getProgressToNextLevel(ServerPlayer player, String categoryId) {
        return getExperience(categoryId).map(exp -> ExperienceMath.progressToNextLevel(exp, player)).orElse(0.0);
    }
}
