package com.hp.skilljs.network;

import com.hp.skilljs.PufferfishSkillsKubeJSPlugin;
import com.hp.skilljs.event.SkillScreenCloseEventJS;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SkillScreenClosePacket() {
    public static void encode(SkillScreenClosePacket packet, FriendlyByteBuf buffer) {
    }

    public static SkillScreenClosePacket decode(FriendlyByteBuf buffer) {
        return new SkillScreenClosePacket();
    }

    public static void handle(SkillScreenClosePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PufferfishSkillsKubeJSPlugin.SKILL_SCREEN_CLOSE.post(new SkillScreenCloseEventJS(player));
            }
        });
        context.setPacketHandled(true);
    }
}
