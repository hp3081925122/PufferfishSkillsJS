package com.hp.skilljs.network;

import com.hp.skilljs.client.RepeatableSkillClientCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;

public final class PufferfishSkillsJSNetwork {
    private static boolean initialized;

    private PufferfishSkillsJSNetwork() {
    }

    public static void init(IEventBus modBus) {
        if (initialized) {
            return;
        }
        initialized = true;
        modBus.addListener(PufferfishSkillsJSNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(RepeatableSkillSyncPacket.TYPE, RepeatableSkillSyncPacket.STREAM_CODEC, RepeatableSkillSyncPacket::handle);
        registrar.playToClient(UnlockableSkillSyncPacket.TYPE, UnlockableSkillSyncPacket.STREAM_CODEC, UnlockableSkillSyncPacket::handle);
        registrar.playToServer(SkillScreenClosePacket.TYPE, SkillScreenClosePacket.STREAM_CODEC, SkillScreenClosePacket::handle);
    }

    public static void syncCategory(ServerPlayer player, ResourceLocation categoryId, Map<String, RepeatableSkillClientCache.Entry> entries) {
        PacketDistributor.sendToPlayer(player, new RepeatableSkillSyncPacket(categoryId, entries));
    }

    public static void syncUnlockableCategory(ServerPlayer player, ResourceLocation categoryId, java.util.Set<String> skillIds) {
        PacketDistributor.sendToPlayer(player, new UnlockableSkillSyncPacket(categoryId, skillIds));
    }

    public static void sendSkillScreenClose() {
        PacketDistributor.sendToServer(new SkillScreenClosePacket());
    }
}
