package com.hp.skilljs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Optional;

public class CategoryUnlockEventJS extends EventJS {
    private final ServerPlayer player;
    private final ResourceLocation categoryId;

    public CategoryUnlockEventJS(ServerPlayer player, ResourceLocation categoryId) {
        this.player = player;
        this.categoryId = categoryId;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getCategoryId() {
        return categoryId;
    }

    public String getCategoryIdString() {
        return categoryId.toString();
    }

    public Optional<Category> getCategory() {
        return SkillsAPI.getCategory(categoryId);
    }
}