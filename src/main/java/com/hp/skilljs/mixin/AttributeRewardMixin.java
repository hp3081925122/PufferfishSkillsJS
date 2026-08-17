package com.hp.skilljs.mixin;

import com.hp.skilljs.PufferfishSkillsJSConfig;
import com.hp.skilljs.reward.RewardUpdateContextMetadata;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.reward.builtin.AttributeReward;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mixin(value = AttributeReward.class, remap = false)
public abstract class AttributeRewardMixin {
    @Unique
    private static final String skilljs$modifierNamePrefix = "pufferfishskillsjs:attribute:";

    @Shadow
    @Final
    private List<UUID> uuids;

    @Shadow
    @Final
    private Attribute attribute;

    @Shadow
    @Final
    private float value;

    @Shadow
    @Final
    private AttributeModifier.Operation operation;

    @Unique
    private boolean skilljs$compacted;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true, remap = false)
    private void skilljs$compactLargeRepeatableAttributeReward(RewardUpdateContext context, CallbackInfo ci) {
        int count = context.getCount();
        ServerPlayer player = context.getPlayer();
        AttributeInstance attributeInstance = Objects.requireNonNull(player.getAttribute(this.attribute));
        String modifierName = null;
        UUID uuid = null;
        if (context instanceof RewardUpdateContextMetadata metadata && metadata.skilljs$getRewardKey() != null) {
            modifierName = skilljs$modifierNamePrefix + metadata.skilljs$getRewardKey();
            uuid = UUID.nameUUIDFromBytes(modifierName.getBytes(StandardCharsets.UTF_8));
        }

        if (!PufferfishSkillsJSConfig.COMPRESS_ATTRIBUTE_REWARDS.get()) {
            if (uuid != null) {
                attributeInstance.removeModifier(uuid);
            }

            if (this.skilljs$compacted) {
                this.skilljs$removeTrackedModifiers(attributeInstance);
                this.uuids.clear();
                this.skilljs$compacted = false;
            }

            return;
        }

        if (uuid == null) {
            return;
        }

        this.skilljs$removeTrackedModifiers(attributeInstance);
        attributeInstance.removeModifier(uuid);
        if (count <= 0) {
            this.uuids.clear();
            this.skilljs$compacted = false;
            ci.cancel();
            return;
        }

        this.uuids.clear();
        this.uuids.add(uuid);
        this.skilljs$compacted = true;
        double compactValue = this.skilljs$getCompactValue(count);
        attributeInstance.addTransientModifier(new AttributeModifier(uuid, modifierName, compactValue, this.operation));
        ci.cancel();
    }

    @Unique
    private void skilljs$removeTrackedModifiers(AttributeInstance attributeInstance) {
        for (UUID existingUuid : this.uuids) {
            if (attributeInstance.getModifier(existingUuid) != null) {
                attributeInstance.removeModifier(existingUuid);
            }
        }
    }

    @Unique
    private double skilljs$getCompactValue(int count) {
        if (this.operation == AttributeModifier.Operation.MULTIPLY_TOTAL) {
            double compactValue = Math.pow(1.0D + this.value, count) - 1.0D;
            if (Double.isFinite(compactValue)) {
                return compactValue;
            }

            return compactValue > 0.0D ? Double.MAX_VALUE : -Double.MAX_VALUE;
        }

        return (double) this.value * count;
    }
}
