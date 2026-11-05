package com.hp.skilljs.network;

import com.hp.skilljs.client.UnlockableSkillClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

import java.util.LinkedHashSet;
import java.util.Set;

public record UnlockableSkillSyncPacket(ResourceLocation categoryId, Set<String> skillIds) implements CustomPacketPayload {
    public static final Type<UnlockableSkillSyncPacket> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("pufferfishskillsjs", "unlockable_sync"));
    public static final StreamCodec<FriendlyByteBuf, UnlockableSkillSyncPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), UnlockableSkillSyncPacket::decode);

    @Override
    public Type<UnlockableSkillSyncPacket> type() {
        return TYPE;
    }

    public static void encode(UnlockableSkillSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.categoryId);
        buffer.writeVarInt(packet.skillIds.size());
        packet.skillIds.forEach(buffer::writeUtf);
    }

    public static UnlockableSkillSyncPacket decode(FriendlyByteBuf buffer) {
        ResourceLocation categoryId = buffer.readResourceLocation();
        int size = buffer.readVarInt();
        Set<String> skillIds = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            skillIds.add(buffer.readUtf());
        }

        return new UnlockableSkillSyncPacket(categoryId, skillIds);
    }

    public static void handle(UnlockableSkillSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> UnlockableSkillClientCache.syncCategory(packet.categoryId(), packet.skillIds()));
    }
}
