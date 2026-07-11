package com.hp.skilljs.mixin;

import com.hp.skilljs.network.PufferfishSkillsJSNetwork;
import net.minecraft.client.gui.screens.Screen;
import net.puffish.skillsmod.client.gui.SkillsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "removed", at = @At("HEAD"))
    private void onSkillScreenClose(CallbackInfo ci) {
        if ((Object) this instanceof SkillsScreen) {
            PufferfishSkillsJSNetwork.sendSkillScreenClose();
        }
    }
}
