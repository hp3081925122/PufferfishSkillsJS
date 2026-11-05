package com.hp.skilljs.mixin;

import com.hp.skilljs.client.RepeatableSkillClientCache;
import com.hp.skilljs.client.UnlockableSkillClientCache;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 技能界面用于绘制边框的显示状态：被允许解锁的锁定技能、仍可继续加点的可重复技能显示为可用。
 */
@Mixin(value = ClientCategoryData.class, remap = false)
public abstract class ClientCategoryDataMixin {
    @Shadow
    public abstract ClientCategoryConfig getConfig();

    @Inject(
        method = "getSkillState(Lnet/puffish/skillsmod/client/config/skill/ClientSkillConfig;)Lnet/puffish/skillsmod/api/Skill$State;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void skilljs$onGetSkillState(ClientSkillConfig skillConfig, CallbackInfoReturnable<Skill.State> cir) {
        Skill.State state = cir.getReturnValue();
        Identifier categoryId = this.getConfig().id();
        if (state == Skill.State.LOCKED && UnlockableSkillClientCache.isAllowed(categoryId, skillConfig.id())) {
            cir.setReturnValue(Skill.State.AVAILABLE);
            return;
        }

        if (state != Skill.State.UNLOCKED) {
            return;
        }

        RepeatableSkillClientCache.Entry entry = RepeatableSkillClientCache.get(categoryId, skillConfig.id());
        if (entry != null && entry.repeatable() && entry.remainingRepeats() != 0) {
            cir.setReturnValue(Skill.State.AVAILABLE);
        }
    }
}
