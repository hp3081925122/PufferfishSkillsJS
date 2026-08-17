package com.hp.skilljs.mixin;

import com.hp.skilljs.reward.RewardUpdateContextMetadata;
import net.puffish.skillsmod.impl.rewards.RewardUpdateContextImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = RewardUpdateContextImpl.class, remap = false)
public abstract class RewardUpdateContextMixin implements RewardUpdateContextMetadata {
    @Unique
    private String skilljs$rewardKey;

    @Override
    public String skilljs$getRewardKey() {
        return this.skilljs$rewardKey;
    }

    @Override
    public void skilljs$setRewardKey(String rewardKey) {
        this.skilljs$rewardKey = rewardKey;
    }
}
