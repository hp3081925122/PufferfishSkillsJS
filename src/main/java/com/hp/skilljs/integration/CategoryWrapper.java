package com.hp.skilljs.integration;

import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;

import java.util.List;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CategoryWrapper {
    private final Category category;

    public CategoryWrapper(Category category) {
        this.category = category;
    }

    private ResourceLocation parseResourceLocation(String value) {
        return Objects.requireNonNull(ResourceLocation.tryParse(value), () -> "Invalid resource location: " + value);
    }

    public ResourceLocation getId() {
        return category.getId();
    }

    public String getIdString() {
        return category.getId().toString();
    }

    public List<SkillWrapper> getSkills(ServerPlayer player) {
        return category.streamSkills().map(skill -> new SkillWrapper(skill, player)).collect(Collectors.toList());
    }

    public SkillWrapper getSkill(ServerPlayer player, String skillId) {
        return category.getSkill(skillId).map(skill -> new SkillWrapper(skill, player)).orElse(null);
    }

    public List<SkillWrapper> getUnlockedSkills(ServerPlayer player) {
        return category.streamUnlockedSkills(player).map(skill -> new SkillWrapper(skill, player)).collect(Collectors.toList());
    }

    public List<SkillWrapper> getAvailableSkills(ServerPlayer player) {
        return category.streamSkills()
            .filter(skill -> {
                var state = skill.getState(player);
                return state == Skill.State.AVAILABLE || state == Skill.State.AFFORDABLE;
            })
            .map(skill -> new SkillWrapper(skill, player))
            .collect(Collectors.toList());
    }

    public List<SkillWrapper> getAffordableSkills(ServerPlayer player) {
        return category.streamSkills()
            .filter(skill -> skill.getState(player) == Skill.State.AFFORDABLE)
            .map(skill -> new SkillWrapper(skill, player))
            .collect(Collectors.toList());
    }

    public void openScreen(ServerPlayer player) {
        category.openScreen(player);
    }

    public void unlock(ServerPlayer player) {
        category.unlock(player);
    }

    public int unlockSkills(ServerPlayer player, List<String> skillIds) {
        int affected = 0;
        for (String skillId : skillIds) {
            var skill = category.getSkill(skillId);
            if (skill.isEmpty()) {
                continue;
            }

            if (skill.get().getState(player) == Skill.State.AFFORDABLE) {
                skill.get().unlock(player);
                affected++;
                continue;
            }

            if (skill.get().getState(player) == Skill.State.UNLOCKED && RepeatableSkillSupport.tryRepeatUnlock(player, category.getId(), skillId)) {
                affected++;
            }
        }
        return affected;
    }

    public void lock(ServerPlayer player) {
        category.lock(player);
    }

    public int lockSkills(ServerPlayer player, List<String> skillIds) {
        int affected = 0;
        for (String skillId : skillIds) {
            var skill = category.getSkill(skillId);
            if (skill.isPresent() && skill.get().getState(player) != Skill.State.LOCKED) {
                skill.get().lock(player);
                affected++;
            }
        }
        return affected;
    }

    public int forceUnlockSkills(ServerPlayer player, List<String> skillIds) {
        int affected = 0;
        for (String skillId : skillIds) {
            var skill = category.getSkill(skillId);
            if (skill.isPresent() && skill.get().getState(player) != Skill.State.UNLOCKED) {
                skill.get().unlock(player);
                affected++;
            }
        }
        return affected;
    }

    public boolean isUnlocked(ServerPlayer player) {
        return category.isUnlocked(player);
    }

    public void resetSkills(ServerPlayer player) {
        category.resetSkills(player);
    }

    public void erase(ServerPlayer player) {
        category.erase(player);
    }

    public boolean hasExperience() {
        return category.getExperience().isPresent();
    }

    public int getPoints(ServerPlayer player, String source) {
        return category.getPoints(player, parseResourceLocation(source));
    }

    public void setPoints(ServerPlayer player, String source, int count) {
        category.setPoints(player, parseResourceLocation(source), count);
    }

    public void addPoints(ServerPlayer player, String source, int count) {
        category.addPoints(player, parseResourceLocation(source), count);
    }

    public void setPointsSilently(ServerPlayer player, String source, int count) {
        category.setPointsSilently(player, parseResourceLocation(source), count);
    }

    public void addPointsSilently(ServerPlayer player, String source, int count) {
        category.addPointsSilently(player, parseResourceLocation(source), count);
    }

    public List<ResourceLocation> getPointsSources(ServerPlayer player) {
        return category.streamPointsSources(player).collect(Collectors.toList());
    }

    public List<String> getSkillIds() {
        return category.streamSkills().map(Skill::getId).collect(Collectors.toList());
    }

    public List<String> getSkillStates(ServerPlayer player) {
        return category.streamSkills().map(skill -> skill.getState(player).name()).collect(Collectors.toList());
    }

    public List<String> getUnlockedSkillIds(ServerPlayer player) {
        return category.streamUnlockedSkills(player).map(Skill::getId).collect(Collectors.toList());
    }

    public int getSpentPoints(ServerPlayer player) {
        return RepeatableSkillSupport.getEffectiveSpentPoints(player, category.getId(), category.getSpentPoints(player));
    }

    public int getPointsTotal(ServerPlayer player) {
        return category.getPointsTotal(player);
    }

    public int getPointsLeft(ServerPlayer player) {
        return RepeatableSkillSupport.getEffectivePointsLeft(player, category.getId(), category.getPointsLeft(player));
    }

    public int getExperienceLevel(ServerPlayer player) {
        return category.getExperience()
            .map(experience -> experience.getLevel(player))
            .orElse(0);
    }

    public int getExperienceCurrent(ServerPlayer player) {
        return category.getExperience()
            .map(experience -> experience.getCurrent(player))
            .orElse(0);
    }

    public int getExperienceRequiredForNextLevel(ServerPlayer player) {
        return category.getExperience()
            .map(experience -> {
                int currentLevel = experience.getLevel(player);
                return experience.getRequiredTotal(currentLevel + 1) - experience.getRequiredTotal(currentLevel);
            })
            .orElse(0);
    }

    public double getExperienceProgressToNextLevel(ServerPlayer player) {
        return category.getExperience()
            .map(experience -> {
                int currentLevel = experience.getLevel(player);
                int currentLevelRequired = experience.getRequiredTotal(currentLevel);
                int nextLevelRequired = experience.getRequiredTotal(currentLevel + 1);
                int currentExp = experience.getTotal(player);

                if (nextLevelRequired <= currentLevelRequired) {
                    return 100.0;
                }

                int needed = nextLevelRequired - currentLevelRequired;
                int have = currentExp - currentLevelRequired;
                return Math.min(100.0, (double) have / needed * 100.0);
            })
            .orElse(0.0);
    }

    public Map<String, Object> getSummary(ServerPlayer player) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", getIdString());
        summary.put("hasExperience", hasExperience());
        summary.put("unlocked", isUnlocked(player));
        summary.put("skillCount", getSkillCount());
        summary.put("unlockedCount", getUnlockedCount(player));
        summary.put("progress", getProgress(player));
        summary.put("pointsTotal", getPointsTotal(player));
        summary.put("pointsLeft", getPointsLeft(player));
        summary.put("spentPoints", getSpentPoints(player));
        summary.put("experienceLevel", getExperienceLevel(player));
        summary.put("experienceCurrent", getExperienceCurrent(player));
        summary.put("experienceRequiredForNextLevel", getExperienceRequiredForNextLevel(player));
        summary.put("experienceProgressToNextLevel", getExperienceProgressToNextLevel(player));
        summary.put("skillIds", getSkillIds());
        summary.put("unlockedSkillIds", getUnlockedSkillIds(player));
        summary.put("skillStates", getSkillStates(player));
        summary.put("pointsSources", getPointsSources(player).stream().map(ResourceLocation::toString).collect(Collectors.toList()));
        return summary;
    }

    public int getExtraPoints(ServerPlayer player) {
        return category.getExtraPoints(player);
    }

    public int getExtraSpentPoints(ServerPlayer player) {
        return RepeatableSkillSupport.getExtraSpentPoints(player, category.getId());
    }

    public String getSkillType(String skillId) {
        return SkillTypeRegistry.getSkillType(category.getId(), skillId).getId();
    }

    public boolean isRepeatableSkill(String skillId) {
        return SkillTypeRegistry.isRepeatable(category.getId(), skillId);
    }

    public int getRepeatCount(ServerPlayer player, String skillId) {
        return RepeatableSkillData.getRepeatCount(player, category.getId(), skillId);
    }

    public int getRepeatLimit(String skillId) {
        return RepeatableSkillSupport.getRepeatLimit(category.getId(), skillId);
    }

    public boolean hasRepeatLimit(String skillId) {
        return RepeatableSkillSupport.hasRepeatLimit(category.getId(), skillId);
    }

    public boolean hasReachedRepeatLimit(ServerPlayer player, String skillId) {
        return RepeatableSkillData.hasReachedRepeatLimit(player, category.getId(), skillId);
    }

    public int getRemainingRepeats(ServerPlayer player, String skillId) {
        return RepeatableSkillData.getRemainingRepeats(player, category.getId(), skillId);
    }

    public boolean repeatUnlockSkill(ServerPlayer player, String skillId) {
        return RepeatableSkillSupport.tryRepeatUnlock(player, category.getId(), skillId);
    }

    public void reset(ServerPlayer player) {
        category.erase(player);
    }

    public void unlockAllSkills(ServerPlayer player) {
        category.streamSkills().forEach(skill -> skill.unlock(player));
    }

    public void lockAllSkills(ServerPlayer player) {
        category.streamSkills().forEach(skill -> skill.lock(player));
    }

    public int getSkillCount() {
        return (int) category.streamSkills().count();
    }

    public int getUnlockedCount(ServerPlayer player) {
        return (int) category.streamUnlockedSkills(player).count();
    }

    public double getProgress(ServerPlayer player) {
        long total = category.streamSkills().count();
        if (total == 0) {
            return 0.0;
        }

        long unlocked = category.streamUnlockedSkills(player).count();
        return (double) unlocked / total * 100.0;
    }

    @Override
    public String toString() {
        return "CategoryWrapper{id='" + getIdString() + "'}";
    }
}
