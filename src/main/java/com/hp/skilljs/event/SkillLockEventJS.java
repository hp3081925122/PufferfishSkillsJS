package com.hp.skilljs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Optional;

public class SkillLockEventJS implements KubeEvent {
    private final ServerPlayer player;
    private final Identifier categoryId;
    private final String skillId;

    public SkillLockEventJS(ServerPlayer player, Identifier categoryId, String skillId) {
        this.player = player;
        this.categoryId = categoryId;
        this.skillId = skillId;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Identifier getCategoryId() {
        return categoryId;
    }

    public String getSkillId() {
        return skillId;
    }

    public String getFullSkillId() {
        return categoryId + ":" + skillId;
    }

    public Optional<Category> getCategory() {
        return SkillsAPI.getCategory(categoryId);
    }

    public Optional<Skill> getSkill() {
        return SkillsAPI.getCategory(categoryId).flatMap(category -> category.getSkill(skillId));
    }
}