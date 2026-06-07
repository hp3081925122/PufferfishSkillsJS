package com.hp.skilljs.integration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Objects;

public class RewardAPIWrapper {
    private ResourceLocation parseResourceLocation(String value) {
        return Objects.requireNonNull(ResourceLocation.tryParse(value), () -> "Invalid resource location: " + value);
    }

    public void updateRewards(ServerPlayer player, String rewardType) {
        SkillsAPI.updateRewards(player, parseResourceLocation(rewardType));
    }

    public void updateAllRewards(ServerPlayer player) {
        SkillsAPI.updateRewards(player, id -> true);
    }
}