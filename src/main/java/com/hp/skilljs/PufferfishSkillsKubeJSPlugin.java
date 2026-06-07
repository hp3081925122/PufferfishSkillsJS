package com.hp.skilljs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import com.hp.skilljs.event.CategoryLockEventJS;
import com.hp.skilljs.event.CategoryUnlockEventJS;
import com.hp.skilljs.event.SkillLockEventJS;
import com.hp.skilljs.event.SkillRepeatUnlockEventJS;
import com.hp.skilljs.event.SkillUnlockEventJS;
import com.hp.skilljs.integration.CategoryWrapper;
import com.hp.skilljs.integration.ExperienceAPIWrapper;
import com.hp.skilljs.integration.RepeatableSkillAPIWrapper;
import com.hp.skilljs.integration.RewardAPIWrapper;
import com.hp.skilljs.integration.SkillWrapper;
import com.hp.skilljs.integration.SkillsAPIWrapper;

public class PufferfishSkillsKubeJSPlugin extends KubeJSPlugin {
    public static final EventGroup EVENTS = EventGroup.of("PufferfishSkillsEvents");

    public static final EventHandler SKILL_UNLOCK = EVENTS.server("skillUnlock", () -> SkillUnlockEventJS.class);
    public static final EventHandler SKILL_REPEAT_UNLOCK = EVENTS.server("skillRepeatUnlock", () -> SkillRepeatUnlockEventJS.class);
    public static final EventHandler SKILL_LOCK = EVENTS.server("skillLock", () -> SkillLockEventJS.class);
    public static final EventHandler CATEGORY_UNLOCK = EVENTS.server("categoryUnlock", () -> CategoryUnlockEventJS.class);
    public static final EventHandler CATEGORY_LOCK = EVENTS.server("categoryLock", () -> CategoryLockEventJS.class);

    @Override
    public void registerEvents() {
        EVENTS.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("PufferfishSkills", new SkillsAPIWrapper());
        event.add("RepeatableSkills", new RepeatableSkillAPIWrapper());
        event.add("SkillExperience", new ExperienceAPIWrapper());
        event.add("SkillRewards", new RewardAPIWrapper());
        event.add("SkillWrapper", SkillWrapper.class);
        event.add("CategoryWrapper", CategoryWrapper.class);
    }
}
