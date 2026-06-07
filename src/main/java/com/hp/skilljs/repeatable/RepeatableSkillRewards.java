package com.hp.skilljs.repeatable;

import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.impl.rewards.RewardUpdateContextImpl;

public final class RepeatableSkillRewards {
    private RepeatableSkillRewards() {
    }

    public static void update(ServerPlayer player, CategoryConfig categoryConfig, SkillConfig skillConfig, int count, boolean action) {
        SkillDefinitionConfig definition = RepeatableSkillData.getDefinition(categoryConfig, skillConfig.id());
        if (definition == null) {
            return;
        }

        definition.rewards().forEach(reward -> reward.instance().update(new RewardUpdateContextImpl(player, count, action)));
    }
}
