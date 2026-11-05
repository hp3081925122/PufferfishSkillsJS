package com.hp.skilljs.integration;

import com.hp.skilljs.PufferfishSkillsKubeJSPlugin;
import com.hp.skilljs.event.SkillLockEventJS;
import com.hp.skilljs.event.SkillUnlockEventJS;
import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import com.hp.skilljs.unlockable.UnlockableSkillSupport;
import net.puffish.skillsmod.api.SkillsAPI;

/**
 * 通过 Pufferfish 官方事件挂接技能解锁/锁定，替代注入上游 lambda 方法。
 * 官方事件只在技能状态真正改变时触发。
 */
public final class SkillEventListeners {
    private static boolean registered;

    private SkillEventListeners() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        SkillsAPI.registerSkillUnlockEvent((player, categoryId, skillId) -> {
            UnlockableSkillSupport.clearSkill(player, categoryId, skillId);
            if (SkillTypeRegistry.isRepeatable(categoryId, skillId)) {
                RepeatableSkillData.ensureInitialUnlock(player, categoryId, skillId);
                RepeatableSkillSupport.syncRepeatableState(player, categoryId);
            }
            PufferfishSkillsKubeJSPlugin.SKILL_UNLOCK.post(new SkillUnlockEventJS(player, categoryId, skillId));
        });

        SkillsAPI.registerSkillLockEvent((player, categoryId, skillId) -> {
            UnlockableSkillSupport.clearSkill(player, categoryId, skillId);
            RepeatableSkillData.clearSkill(player, categoryId, skillId);
            RepeatableSkillSupport.syncPoints(player, categoryId);
            RepeatableSkillSupport.syncRepeatableState(player, categoryId);
            PufferfishSkillsKubeJSPlugin.SKILL_LOCK.post(new SkillLockEventJS(player, categoryId, skillId));
        });
    }
}
