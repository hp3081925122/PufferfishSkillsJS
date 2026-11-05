package com.hp.skilljs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Optional;

public class CategoryLockEventJS implements KubeEvent {
    private final ServerPlayer player;
    private final Identifier categoryId;

    public CategoryLockEventJS(ServerPlayer player, Identifier categoryId) {
        this.player = player;
        this.categoryId = categoryId;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Identifier getCategoryId() {
        return categoryId;
    }

    public String getCategoryIdString() {
        return categoryId.toString();
    }

    public Optional<Category> getCategory() {
        return SkillsAPI.getCategory(categoryId);
    }
}