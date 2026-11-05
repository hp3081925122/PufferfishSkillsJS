package com.hp.skilljs;

import com.hp.skilljs.integration.SkillEventListeners;
import com.hp.skilljs.repeatable.RepeatableSkillPersistenceEvents;
import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(PufferfishSkillsJSMod.MOD_ID)
public class PufferfishSkillsJSMod {
    public static final String MOD_ID = "pufferfishskillsjs";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public PufferfishSkillsJSMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, PufferfishSkillsJSConfig.SPEC);
        PufferfishSkillsJS.init(modBus);
        SkillEventListeners.register();
        NeoForge.EVENT_BUS.register(RepeatableSkillPersistenceEvents.class);
    }
}
