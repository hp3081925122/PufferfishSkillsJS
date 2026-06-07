package com.hp.skilljs.integration;

import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Skill;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 技能包装类 - 为KubeJS提供友好的技能操作接口
 */
public class SkillWrapper {
    private final Skill skill;
    private final ServerPlayer player;
    
    public SkillWrapper(Skill skill, ServerPlayer player) {
        this.skill = skill;
        this.player = player;
    }
    
    /**
     * 获取技能ID
     */
    public String getId() {
        return skill.getId();
    }

    /**
     * 获取所属分类ID
     */
    public String getCategoryId() {
        return skill.getCategory().getId().toString();
    }
    
    /**
     * 获取所属分类
     */
    public CategoryWrapper getCategory() {
        return new CategoryWrapper(skill.getCategory());
    }
    
    /**
     * 获取当前状态
     * @return "LOCKED", "AVAILABLE", "AFFORDABLE", "UNLOCKED", "EXCLUDED"
     */
    public String getState() {
        return skill.getState(player).name();
    }
    
    /**
     * 是否已锁定
     */
    public boolean isLocked() {
        return skill.getState(player) == Skill.State.LOCKED;
    }
    
    /**
     * 是否可用（满足前置条件但可能不够点数）
     */
    public boolean isAvailable() {
        return skill.getState(player) == Skill.State.AVAILABLE;
    }
    
    /**
     * 是否负担得起（满足所有条件可以解锁）
     */
    public boolean isAffordable() {
        return skill.getState(player) == Skill.State.AFFORDABLE;
    }
    
    /**
     * 是否已解锁
     */
    public boolean isUnlocked() {
        return skill.getState(player) == Skill.State.UNLOCKED;
    }
    
    /**
     * 是否被排除（互斥技能已解锁）
     */
    public boolean isExcluded() {
        return skill.getState(player) == Skill.State.EXCLUDED;
    }
    
    /**
     * 是否可以解锁
     */
    public boolean canUnlock() {
        Skill.State state = skill.getState(player);
        return state == Skill.State.AFFORDABLE;
    }
    
    /**
     * 解锁此技能
     * @return 是否成功
     */
    public boolean unlock() {
        if (canUnlock()) {
            skill.unlock(player);
            return true;
        }
        if (isUnlocked() && isRepeatable()) {
            return RepeatableSkillSupport.tryRepeatUnlock(player, skill.getCategory().getId(), skill.getId());
        }
        return false;
    }
    
    /**
     * 强制解锁（无视消耗）
     */
    public void forceUnlock() {
        skill.unlock(player);
    }
    
    /**
     * 锁定此技能
     */
    public void lock() {
        skill.lock(player);
    }
    
    /**
     * 获取完整ID (category:skill)
     */
    public String getFullId() {
        return skill.getCategory().getId() + ":" + skill.getId();
    }

    public String getSkillType() {
        return SkillTypeRegistry.getSkillType(skill.getCategory().getId(), skill.getId()).getId();
    }

    public boolean isRepeatable() {
        return SkillTypeRegistry.isRepeatable(skill.getCategory().getId(), skill.getId());
    }

    public int getRepeatCount() {
        return RepeatableSkillData.getRepeatCount(player, skill.getCategory().getId(), skill.getId());
    }

    public int getRepeatLimit() {
        return RepeatableSkillSupport.getRepeatLimit(skill.getCategory().getId(), skill.getId());
    }

    public boolean hasRepeatLimit() {
        return RepeatableSkillSupport.hasRepeatLimit(skill.getCategory().getId(), skill.getId());
    }

    public boolean hasReachedRepeatLimit() {
        return RepeatableSkillData.hasReachedRepeatLimit(player, skill.getCategory().getId(), skill.getId());
    }

    public int getRemainingRepeats() {
        return RepeatableSkillData.getRemainingRepeats(player, skill.getCategory().getId(), skill.getId());
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", getId());
        summary.put("categoryId", getCategoryId());
        summary.put("fullId", getFullId());
        summary.put("state", getState());
        summary.put("locked", isLocked());
        summary.put("available", isAvailable());
        summary.put("affordable", isAffordable());
        summary.put("unlocked", isUnlocked());
        summary.put("excluded", isExcluded());
        summary.put("canUnlock", canUnlock());
        summary.put("skillType", getSkillType());
        summary.put("repeatable", isRepeatable());
        summary.put("repeatCount", getRepeatCount());
        summary.put("repeatLimit", getRepeatLimit());
        summary.put("hasRepeatLimit", hasRepeatLimit());
        summary.put("reachedRepeatLimit", hasReachedRepeatLimit());
        summary.put("remainingRepeats", getRemainingRepeats());
        return summary;
    }
    
    @Override
    public String toString() {
        return "SkillWrapper{id='" + skill.getId() + "', state=" + getState() + "}";
    }
}
