package com.hp.skilljs.reward;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.impl.rewards.RewardUpdateContextImpl;

public final class RewardUpdateContexts {
    private RewardUpdateContexts() {
    }

    public static RewardUpdateContext create(
        ServerPlayer player,
        ResourceLocation categoryId,
        String definitionId,
        int rewardIndex,
        int count,
        boolean action
    ) {
        RewardUpdateContextImpl context = new RewardUpdateContextImpl(player, count, action);
        ((RewardUpdateContextMetadata) (Object) context).skilljs$setRewardKey(categoryId + "/" + definitionId + "/" + rewardIndex);
        return context;
    }
}
