package com.hp.skilljs.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(value = SkillsMod.class, remap = false)
public interface SkillsModAccessor {
    @Invoker("getCategory")
    Optional<CategoryConfig> skilljs$invokeGetCategory(ResourceLocation categoryId);

    @Invoker("getPlayerData")
    PlayerData skilljs$invokeGetPlayerData(ServerPlayer player);

    @Accessor("packetSender")
    ServerPacketSender skilljs$getPacketSender();
}
