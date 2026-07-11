package com.hp.skilljs.network;

import com.hp.skilljs.client.UnlockableSkillClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public record UnlockableSkillSyncPacket(ResourceLocation categoryId, Set<String> skillIds) {
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

    public static void handle(UnlockableSkillSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> UnlockableSkillClientCache.syncCategory(packet.categoryId(), packet.skillIds()));
        context.setPacketHandled(true);
    }
}
