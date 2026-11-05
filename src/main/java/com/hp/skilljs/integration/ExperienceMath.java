package com.hp.skilljs.integration;

import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Experience;

/**
 * Pufferfish 经验曲线：处于等级 L 时，升到 L+1 需要 getRequired(L) 点经验，
 * 等级 L 的起点为 getRequiredTotal(L - 1)（L = 0 时为 0）。
 */
final class ExperienceMath {
    private ExperienceMath() {
    }

    static int requiredForNextLevel(Experience exp, ServerPlayer player) {
        return exp.getRequired(player, exp.getLevel(player));
    }

    static double progressToNextLevel(Experience exp, ServerPlayer player) {
        int required = requiredForNextLevel(exp, player);
        if (required <= 0) {
            return 100.0;
        }

        return Math.max(0.0, Math.min(100.0, (double) exp.getCurrent(player) / required * 100.0));
    }

    static boolean levelUp(Experience exp, ServerPlayer player) {
        int before = exp.getLevel(player);
        exp.addLevel(player, 1);
        return exp.getLevel(player) > before;
    }
}
