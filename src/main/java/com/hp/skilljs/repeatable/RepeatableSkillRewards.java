package com.hp.skilljs.repeatable;

import com.hp.skilljs.reward.RewardUpdateContexts;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;

public final class RepeatableSkillRewards {
    private RepeatableSkillRewards() {
    }

    public static void update(ServerPlayer player, CategoryConfig categoryConfig, SkillConfig skillConfig, int count, boolean action) {
        SkillDefinitionConfig definition = RepeatableSkillData.getDefinition(categoryConfig, skillConfig.id());
        if (definition == null) {
            return;
        }

        for (int rewardIndex = 0; rewardIndex < definition.rewards().size(); rewardIndex++) {
            definition.rewards().get(rewardIndex).instance().update(
                RewardUpdateContexts.create(player, categoryConfig.id(), definition.id(), rewardIndex, count, action)
            );
        }
    }
}
