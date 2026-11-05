package com.hp.skilljs.integration;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Objects;

public class RewardAPIWrapper {
    private Identifier parseResourceLocation(String value) {
        return Objects.requireNonNull(Identifier.tryParse(value), () -> "Invalid resource location: " + value);
    }

    public void updateRewards(ServerPlayer player, String rewardType) {
        SkillsAPI.updateRewards(player, parseResourceLocation(rewardType));
    }

    public void updateAllRewards(ServerPlayer player) {
        SkillsAPI.updateRewards(player, id -> true);
    }
}