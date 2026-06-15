package com.hp.skilljs;

import com.hp.skilljs.repeatable.RepeatableSkillPersistenceEvents;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(PufferfishSkillsJSMod.MOD_ID)
public class PufferfishSkillsJSMod {
    public static final String MOD_ID = "pufferfishskillsjs";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public PufferfishSkillsJSMod() {
        PufferfishSkillsJS.init();
        MinecraftForge.EVENT_BUS.register(RepeatableSkillPersistenceEvents.class);
    }
}
