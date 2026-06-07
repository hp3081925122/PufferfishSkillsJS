package com.hp.skilljs.network;

import com.hp.skilljs.client.RepeatableSkillClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public record RepeatableSkillSyncPacket(ResourceLocation categoryId, Map<String, RepeatableSkillClientCache.Entry> entries) {
    public static void encode(RepeatableSkillSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.categoryId);
        buffer.writeVarInt(packet.entries.size());
        packet.entries.forEach((skillId, entry) -> {
            buffer.writeUtf(skillId);
            buffer.writeBoolean(entry.repeatable());
            buffer.writeVarInt(entry.count());
            buffer.writeVarInt(entry.limit());
        });
    }

    public static RepeatableSkillSyncPacket decode(FriendlyByteBuf buffer) {
        ResourceLocation categoryId = buffer.readResourceLocation();
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

    public static void handle(RepeatableSkillSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> RepeatableSkillClientCache.syncCategory(packet.categoryId(), packet.entries()));
        context.setPacketHandled(true);
    }
}
