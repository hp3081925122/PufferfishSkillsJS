package com.hp.skilljs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerPlayer;

public class SkillScreenCloseEventJS implements KubeEvent {
    private final ServerPlayer player;

    public SkillScreenCloseEventJS(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
