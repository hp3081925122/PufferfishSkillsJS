package com.hp.skilljs.network;

import com.hp.skilljs.PufferfishSkillsKubeJSPlugin;
import com.hp.skilljs.event.SkillScreenCloseEventJS;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;


public record SkillScreenClosePacket() implements CustomPacketPayload {
    public static final Type<SkillScreenClosePacket> TYPE = new Type<>(net.minecraft.resources.Identifier.fromNamespaceAndPath("pufferfishskillsjs", "screen_close"));
    public static final StreamCodec<FriendlyByteBuf, SkillScreenClosePacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), SkillScreenClosePacket::decode);

    @Override
    public Type<SkillScreenClosePacket> type() {
        return TYPE;
    }

    public static void encode(SkillScreenClosePacket packet, FriendlyByteBuf buffer) {
    }

    public static SkillScreenClosePacket decode(FriendlyByteBuf buffer) {
        return new SkillScreenClosePacket();
    }

    public static void handle(SkillScreenClosePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player != null) {
                PufferfishSkillsKubeJSPlugin.SKILL_SCREEN_CLOSE.post(new SkillScreenCloseEventJS(player));
            }
        });
    }
}
