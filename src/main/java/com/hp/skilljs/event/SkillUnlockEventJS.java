package com.hp.skilljs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Optional;

public class SkillUnlockEventJS extends EventJS {
    private final ServerPlayer player;
    private final ResourceLocation categoryId;
    private final String skillId;

    public SkillUnlockEventJS(ServerPlayer player, ResourceLocation categoryId, String skillId) {
        this.player = player;
        this.categoryId = categoryId;
        this.skillId = skillId;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getCategoryId() {
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