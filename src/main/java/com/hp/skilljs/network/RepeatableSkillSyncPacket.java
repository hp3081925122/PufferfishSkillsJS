package com.hp.skilljs.network;

import com.hp.skilljs.client.RepeatableSkillClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

import java.util.LinkedHashMap;
import java.util.Map;

public record RepeatableSkillSyncPacket(Identifier categoryId, Map<String, RepeatableSkillClientCache.Entry> entries) implements CustomPacketPayload {
    public static final Type<RepeatableSkillSyncPacket> TYPE = new Type<>(net.minecraft.resources.Identifier.fromNamespaceAndPath("pufferfishskillsjs", "repeatable_sync"));
    public static final StreamCodec<FriendlyByteBuf, RepeatableSkillSyncPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), RepeatableSkillSyncPacket::decode);

    @Override
    public Type<RepeatableSkillSyncPacket> type() {
        return TYPE;
    }

    public static void encode(RepeatableSkillSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeIdentifier(packet.categoryId);
        buffer.writeVarInt(packet.entries.size());
        packet.entries.forEach((skillId, entry) -> {
            buffer.writeUtf(skillId);
            buffer.writeBoolean(entry.repeatable());
            buffer.writeVarInt(entry.count());
            buffer.writeVarInt(entry.limit());
        });
    }

    public static RepeatableSkillSyncPacket decode(FriendlyByteBuf buffer) {
        Identifier categoryId = buffer.readIdentifier();
        int size = buffer.readVarInt();
        Map<String, RepeatableSkillClientCache.Entry> entries = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String skillId = buffer.readUtf();
            boolean repeatable = buffer.readBoolean();
            int count = buffer.readVarInt();
            int limit = buffer.readVarInt();
            entries.put(skillId, new RepeatableSkillClientCache.Entry(repeatable, count, limit));
        }

        return new RepeatableSkillSyncPacket(categoryId, entries);
    }

    public static void handle(RepeatableSkillSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> RepeatableSkillClientCache.syncCategory(packet.categoryId(), packet.entries()));
    }
}
