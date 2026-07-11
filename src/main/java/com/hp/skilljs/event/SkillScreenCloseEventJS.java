package com.hp.skilljs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.server.level.ServerPlayer;

public class SkillScreenCloseEventJS extends EventJS {
    private final ServerPlayer player;

    public SkillScreenCloseEventJS(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
