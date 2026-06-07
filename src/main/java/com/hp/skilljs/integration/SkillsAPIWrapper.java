package com.hp.skilljs.integration;

import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Experience;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillsAPIWrapper {
    private ResourceLocation parseResourceLocation(String value) {
        return Objects.requireNonNull(ResourceLocation.tryParse(value), () -> "Invalid resource location: " + value);
    }

    private Optional<Category> resolveCategory(String categoryId) {
        return SkillsAPI.getCategory(parseResourceLocation(categoryId));
    }

    public List<CategoryWrapper> getCategories() {
        return SkillsAPI.streamCategories().map(CategoryWrapper::new).collect(Collectors.toList());
    }

    public Optional<CategoryWrapper> getCategory(String categoryId) {
        return resolveCategory(categoryId).map(CategoryWrapper::new);
    }

    public Optional<CategoryWrapper> getCategory(ResourceLocation categoryId) {
        return SkillsAPI.getCategory(categoryId).map(CategoryWrapper::new);
    }

    public boolean hasCategory(String categoryId) {
        return resolveCategory(categoryId).isPresent();
    }

    public boolean hasSkill(String categoryId, String skillId) {
        return resolveCategory(categoryId)
            .flatMap(category -> category.getSkill(skillId))
            .isPresent();
    }

    public List<CategoryWrapper> getUnlockedCategories(ServerPlayer player) {
        return SkillsAPI.streamUnlockedCategories(player).map(CategoryWrapper::new).collect(Collectors.toList());
    }

    public Map<String, Object> getSummary(ServerPlayer player, String categoryId) {
        return resolveCategory(categoryId)
            .<Map<String, Object>>map(category -> new CategoryWrapper(category).getSummary(player))
            .orElseGet(() -> new LinkedHashMap<>());
    }

    public List<Map<String, Object>> getSummaries(ServerPlayer player) {
        return SkillsAPI.streamCategories()
            .map(category -> new CategoryWrapper(category).getSummary(player))
            .collect(Collectors.toList());
    }

    public List<SkillWrapper> getSkills(ServerPlayer player, String categoryId) {
        return resolveCategory(categoryId)
            .map(category -> category.streamSkills().map(skill -> new SkillWrapper(skill, player)).collect(Collectors.toList()))
            .orElseGet(List::of);
    }

    public List<SkillWrapper> getUnlockedSkills(ServerPlayer player, String categoryId) {
        return getSkills(player, categoryId).stream().filter(SkillWrapper::isUnlocked).collect(Collectors.toList());
    }

    public List<SkillWrapper> getAvailableSkills(ServerPlayer player, String categoryId) {
        return getSkills(player, categoryId).stream().filter(SkillWrapper::isAvailable).collect(Collectors.toList());
    }

    public List<SkillWrapper> getAffordableSkills(ServerPlayer player, String categoryId) {
        return getSkills(player, categoryId).stream().filter(SkillWrapper::isAffordable).collect(Collectors.toList());
    }

    public List<String> getSkillStates(ServerPlayer player, String categoryId) {
        return getSkills(player, categoryId).stream().map(SkillWrapper::getState).collect(Collectors.toList());
    }

    public List<String> getSkillIds(String categoryId) {
        return resolveCategory(categoryId)
            .map(category -> category.streamSkills().map(Skill::getId).collect(Collectors.toList()))
            .orElseGet(List::of);
    }

    public List<String> getUnlockedSkillIds(ServerPlayer player, String categoryId) {
        return getUnlockedSkills(player, categoryId).stream().map(SkillWrapper::getId).collect(Collectors.toList());
    }

    public void openScreen(ServerPlayer player) {
        SkillsAPI.openScreen(player);
    }

    public void openCategoryScreen(ServerPlayer player, String categoryId) {
        resolveCategory(categoryId).ifPresent(category -> category.openScreen(player));
    }

    public boolean isSkillUnlocked(ServerPlayer player, String categoryId, String skillId) {
        return resolveCategory(categoryId)
            .flatMap(category -> category.getSkill(skillId))
            .map(skill -> skill.getState(player) == Skill.State.UNLOCKED)
            .orElse(false);
    }

    public boolean unlockSkill(ServerPlayer player, String categoryId, String skillId) {
        ResourceLocation categoryKey = parseResourceLocation(categoryId);
        return resolveCategory(categoryId)
            .flatMap(category -> category.getSkill(skillId))
            .map(skill -> {
                if (skill.getState(player) == Skill.State.AFFORDABLE) {
                    skill.unlock(player);
                    return true;
                }
                if (skill.getState(player) == Skill.State.UNLOCKED && SkillTypeRegistry.isRepeatable(categoryKey, skillId)) {
                    return RepeatableSkillSupport.tryRepeatUnlock(player, categoryKey, skillId);
                }
                return false;
            })
            .orElse(false);
    }

    public int unlockSkills(ServerPlayer player, String categoryId, List<String> skillIds) {
        return applySkillAction(player, categoryId, skillIds, SkillAction.UNLOCK);
    }

    public void forceUnlockSkill(ServerPlayer player, String categoryId, String skillId) {
        resolveCategory(categoryId).flatMap(category -> category.getSkill(skillId)).ifPresent(skill -> skill.unlock(player));
    }

    public int forceUnlockSkills(ServerPlayer player, String categoryId, List<String> skillIds) {
        return applySkillAction(player, categoryId, skillIds, SkillAction.FORCE_UNLOCK);
    }

    public void lockSkill(ServerPlayer player, String categoryId, String skillId) {
        resolveCategory(categoryId).flatMap(category -> category.getSkill(skillId)).ifPresent(skill -> skill.lock(player));
    }

    public int lockSkills(ServerPlayer player, String categoryId, List<String> skillIds) {
        return applySkillAction(player, categoryId, skillIds, SkillAction.LOCK);
    }

    public void unlockCategory(ServerPlayer player, String categoryId) {
        resolveCategory(categoryId).ifPresent(category -> category.unlock(player));
    }

    public void lockCategory(ServerPlayer player, String categoryId) {
        resolveCategory(categoryId).ifPresent(category -> category.lock(player));
    }

    public boolean isCategoryUnlocked(ServerPlayer player, String categoryId) {
        return resolveCategory(categoryId).map(category -> category.isUnlocked(player)).orElse(false);
    }

    public void resetAll(ServerPlayer player) {
        SkillsAPI.streamCategories().forEach(category -> {
            category.resetSkills(player);
            category.lock(player);
        });
    }

    public void resetCategory(ServerPlayer player, String categoryId) {
        resolveCategory(categoryId).ifPresent(category -> {
            category.resetSkills(player);
            category.lock(player);
        });
    }

    public void resetSkills(ServerPlayer player, String categoryId) {
        resolveCategory(categoryId).ifPresent(category -> category.resetSkills(player));
    }

    public void eraseCategory(ServerPlayer player, String categoryId) {
        resolveCategory(categoryId).ifPresent(category -> category.erase(player));
    }

    public int getPoints(ServerPlayer player, String categoryId, String source) {
        return resolveCategory(categoryId).map(category -> category.getPoints(player, parseResourceLocation(source))).orElse(0);
    }

    public void setPoints(ServerPlayer player, String categoryId, String source, int count) {
        resolveCategory(categoryId).ifPresent(category -> category.setPoints(player, parseResourceLocation(source), count));
    }

    public void addPoints(ServerPlayer player, String categoryId, String source, int count) {
        resolveCategory(categoryId).ifPresent(category -> category.addPoints(player, parseResourceLocation(source), count));
    }

    public void setPointsSilently(ServerPlayer player, String categoryId, String source, int count) {
        resolveCategory(categoryId).ifPresent(category -> category.setPointsSilently(player, parseResourceLocation(source), count));
    }

    public void addPointsSilently(ServerPlayer player, String categoryId, String source, int count) {
        resolveCategory(categoryId).ifPresent(category -> category.addPointsSilently(player, parseResourceLocation(source), count));
    }

    public List<ResourceLocation> getPointsSources(ServerPlayer player, String categoryId) {
        return resolveCategory(categoryId)
            .map(category -> category.streamPointsSources(player).collect(Collectors.toList()))
            .orElseGet(List::of);
    }

    public int getSpentPoints(ServerPlayer player, String categoryId) {
        return resolveCategory(categoryId)
            .map(category -> RepeatableSkillSupport.getEffectiveSpentPoints(player, category.getId(), category.getSpentPoints(player)))
            .orElse(0);
    }

    public int getPointsTotal(ServerPlayer player, String categoryId) {
        return resolveCategory(categoryId).map(category -> category.getPointsTotal(player)).orElse(0);
    }

    public int getPointsLeft(ServerPlayer player, String categoryId) {
        return resolveCategory(categoryId)
            .map(category -> RepeatableSkillSupport.getEffectivePointsLeft(player, category.getId(), category.getPointsLeft(player)))
            .orElse(0);
    }

    public String getSkillType(String categoryId, String skillId) {
        return SkillTypeRegistry.getSkillType(parseResourceLocation(categoryId), skillId).getId();
    }

    public boolean isRepeatableSkill(String categoryId, String skillId) {
        return SkillTypeRegistry.isRepeatable(parseResourceLocation(categoryId), skillId);
    }

    public int getRepeatCount(ServerPlayer player, String categoryId, String skillId) {
        return RepeatableSkillData.getRepeatCount(player, parseResourceLocation(categoryId), skillId);
    }

    public int getRepeatLimit(String categoryId, String skillId) {
        return RepeatableSkillSupport.getRepeatLimit(parseResourceLocation(categoryId), skillId);
    }

    public boolean hasRepeatLimit(String categoryId, String skillId) {
        return RepeatableSkillSupport.hasRepeatLimit(parseResourceLocation(categoryId), skillId);
    }

    public boolean hasReachedRepeatLimit(ServerPlayer player, String categoryId, String skillId) {
        return RepeatableSkillData.hasReachedRepeatLimit(player, parseResourceLocation(categoryId), skillId);
    }

    public int getRemainingRepeats(ServerPlayer player, String categoryId, String skillId) {
        return RepeatableSkillData.getRemainingRepeats(player, parseResourceLocation(categoryId), skillId);
    }

    public boolean repeatUnlockSkill(ServerPlayer player, String categoryId, String skillId) {
        return RepeatableSkillSupport.tryRepeatUnlock(player, parseResourceLocation(categoryId), skillId);
    }

    public int getExperienceLevel(ServerPlayer player, String categoryId) {
        return getLevel(player, categoryId);
    }

    public int getExperienceCurrent(ServerPlayer player, String categoryId) {
        return getCurrentExperience(player, categoryId);
    }

    public int getExperienceRequiredForNextLevel(ServerPlayer player, String categoryId) {
        return getRequiredForNextLevel(player, categoryId);
    }

    public double getExperienceProgressToNextLevel(ServerPlayer player, String categoryId) {
        return getProgressToNextLevel(player, categoryId);
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

    public Optional<Experience> getExperience(String categoryId) {
        return resolveCategory(categoryId).flatMap(Category::getExperience);
    }

    public int getRequiredForLevel(String categoryId, int level) {
        return getExperience(categoryId).map(exp -> exp.getRequired(level)).orElse(0);
    }

    public int getRequiredTotalForLevel(String categoryId, int level) {
        return getExperience(categoryId).map(exp -> exp.getRequiredTotal(level)).orElse(0);
    }

    public void setLevel(ServerPlayer player, String categoryId, int level) {
        getExperience(categoryId).ifPresent(exp -> exp.setTotal(player, exp.getRequiredTotal(level)));
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

    private int applySkillAction(ServerPlayer player, String categoryId, List<String> skillIds, SkillAction action) {
        return resolveCategory(categoryId)
            .map(category -> {
                int affected = 0;
                for (String skillId : skillIds) {
                    var skillOpt = category.getSkill(skillId);
                    if (skillOpt.isEmpty()) {
                        continue;
                    }

                    var skill = skillOpt.get();
                    switch (action) {
                        case UNLOCK -> {
                            if (skill.getState(player) == Skill.State.AFFORDABLE) {
                                skill.unlock(player);
                                affected++;
                            }
                        }
                        case FORCE_UNLOCK -> {
                            if (skill.getState(player) != Skill.State.UNLOCKED) {
                                skill.unlock(player);
                                affected++;
                            }
                        }
                        case LOCK -> {
                            if (skill.getState(player) != Skill.State.LOCKED) {
                                skill.lock(player);
                                affected++;
                            }
                        }
                    }
                }
                return affected;
            })
            .orElse(0);
    }

    private enum SkillAction {
        UNLOCK,
        FORCE_UNLOCK,
        LOCK
    }
}
