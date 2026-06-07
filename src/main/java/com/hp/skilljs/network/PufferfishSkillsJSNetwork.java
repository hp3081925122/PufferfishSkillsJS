package com.hp.skilljs.network;

import com.hp.skilljs.PufferfishSkillsJSMod;
import com.hp.skilljs.client.RepeatableSkillClientCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Map;

public final class PufferfishSkillsJSNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(PufferfishSkillsJSMod.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static boolean initialized;

    private PufferfishSkillsJSNetwork() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        CHANNEL.registerMessage(
            0,
            RepeatableSkillSyncPacket.class,
            RepeatableSkillSyncPacket::encode,
            RepeatableSkillSyncPacket::decode,
            RepeatableSkillSyncPacket::handle
        );
    }

    public static void syncCategory(ServerPlayer player, ResourceLocation categoryId, Map<String, RepeatableSkillClientCache.Entry> entries) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new RepeatableSkillSyncPacket(categoryId, entries));
    }
}
