package com.hp.skilljs;

import com.hp.skilljs.network.PufferfishSkillsJSNetwork;

public class PufferfishSkillsJS {
    public static final String MOD_ID = "pufferfishskillsjs";
    
    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        PufferfishSkillsJSNetwork.init(modBus);
    }
}
