package com.hp.skilljs;

import net.minecraftforge.fml.common.Mod;

@Mod(PufferfishSkillsJSMod.MOD_ID)
public class PufferfishSkillsJSMod {
    public static final String MOD_ID = "pufferfishskillsjs";
    
    public PufferfishSkillsJSMod() {
        // Initialize KubeJS integration
        PufferfishSkillsJS.init();
    }
}
