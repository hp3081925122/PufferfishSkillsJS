package com.hp.skilljs.mixin;

import com.hp.skilljs.event.SkillRepeatUnlockEventJS;
import com.hp.skilljs.integration.RepeatableSkillSupport;
import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.RepeatableSkillRewards;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.network.packets.out.PointsUpdateOutPacket;
import com.hp.skilljs.PufferfishSkillsKubeJSPlugin;
import com.hp.skilljs.event.CategoryLockEventJS;
import com.hp.skilljs.event.CategoryUnlockEventJS;
import com.hp.skilljs.event.SkillLockEventJS;
import com.hp.skilljs.event.SkillUnlockEventJS;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = SkillsMod.class, remap = false)
public abstract class SkillsModMixin {
    @Shadow
    protected abstract Optional<CategoryConfig> getCategory(ResourceLocation categoryId);

    @Shadow
    protected abstract PlayerData getPlayerData(ServerPlayer player);

    @Shadow
    @Final
    private ServerPacketSender packetSender;

    @Unique
    private void skilljs$syncRepeatablePoints(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData) {
        this.packetSender.send(
            player,
            new PointsUpdateOutPacket(
                categoryConfig.id(),
                RepeatableSkillData.getEffectiveSpentPoints(player, categoryConfig, categoryData),
                categoryData.getPointsTotal()
            )
        );
    }

    @Inject(method = "tryUnlockSkill", at = @At("HEAD"), cancellable = true, remap = false)
    private void onTryUnlockRepeatableSkill(ServerPlayer player, ResourceLocation categoryId, String skillId, boolean action, CallbackInfo ci) {
        Optional<CategoryConfig> categoryConfig = this.getCategory(categoryId);
        if (categoryConfig.isEmpty()) {
            return;
        }

        Optional<SkillConfig> skillConfig = categoryConfig.get().skills().getById(skillId);
        if (skillConfig.isEmpty()) {
            return;
        }

        PlayerData playerData = this.getPlayerData(player);
        if (!playerData.isCategoryUnlocked(categoryConfig.get())) {
            return;
        }

        CategoryData categoryData = playerData.getOrCreateCategoryData(categoryConfig.get());
        if (!categoryData.getUnlockedSkillIds().contains(skillId)) {
            if (RepeatableSkillData.getExtraSpentPoints(player, categoryConfig.get()) <= 0) {
                return;
            }

            SkillDefinitionConfig definition = RepeatableSkillData.getDefinition(categoryConfig.get(), skillId);
            if (definition == null) {
                return;
            }

            if (!this.skilljs$canAffordWithRepeatablePoints(player, categoryConfig.get(), categoryData, definition)) {
                ci.cancel();
            }

            return;
        }

        if (!SkillTypeRegistry.isRepeatable(categoryId, skillId)) {
            return;
        }

        if (!RepeatableSkillData.canRepeatUnlock(player, categoryConfig.get(), categoryData, skillConfig.get())) {
            ci.cancel();
            return;
        }

        int repeatCount = RepeatableSkillData.incrementRepeatCount(player, categoryId, skillId);
        RepeatableSkillRewards.update(player, categoryConfig.get(), skillConfig.get(), repeatCount, action);
        PufferfishSkillsKubeJSPlugin.SKILL_REPEAT_UNLOCK.post(new SkillRepeatUnlockEventJS(player, categoryId, skillId, repeatCount));
        this.skilljs$syncRepeatablePoints(player, categoryConfig.get(), categoryData);
        RepeatableSkillSupport.syncRepeatableState(player, categoryConfig.get());
        ci.cancel();
    }

    @Unique
    private boolean skilljs$canAffordWithRepeatablePoints(
        ServerPlayer player,
        CategoryConfig categoryConfig,
        CategoryData categoryData,
        SkillDefinitionConfig definition
    ) {
        int requiredPoints = Math.max(definition.requiredPoints(), definition.cost());
        if (RepeatableSkillData.getEffectivePointsLeft(player, categoryConfig, categoryData) < requiredPoints) {
            return false;
        }

        return RepeatableSkillData.getEffectiveSpentPoints(player, categoryConfig, categoryData) >= definition.requiredSpentPoints();
    }

    @Inject(method = "lambda$tryUnlockSkill$20", at = @At(value = "INVOKE", target = "Lnet/puffish/skillsmod/server/data/CategoryData;unlockSkill(Ljava/lang/String;)V", shift = At.Shift.AFTER), remap = false)
    private void onSkillUnlock(CategoryData categoryData, String skillId, ServerPlayer player, ResourceLocation categoryId, CategoryConfig categoryConfig, CallbackInfo ci) {
        if (SkillTypeRegistry.isRepeatable(categoryId, skillId)) {
            RepeatableSkillData.ensureInitialUnlock(player, categoryId, skillId);
            RepeatableSkillSupport.syncRepeatableState(player, categoryConfig);
        }
        PufferfishSkillsKubeJSPlugin.SKILL_UNLOCK.post(new SkillUnlockEventJS(player, categoryId, skillId));
    }

    @Inject(method = "lockSkill", at = @At("TAIL"), remap = false)
    private void onSkillLock(ServerPlayer player, ResourceLocation categoryId, String skillId, CallbackInfo ci) {
        RepeatableSkillData.clearSkill(player, categoryId, skillId);
        RepeatableSkillSupport.syncRepeatableState(player, categoryId);
        PufferfishSkillsKubeJSPlugin.SKILL_LOCK.post(new SkillLockEventJS(player, categoryId, skillId));
    }

    @Inject(method = "unlockCategory", at = @At("TAIL"), remap = false)
    private void onCategoryUnlock(ServerPlayer player, ResourceLocation categoryId, CallbackInfo ci) {
        PufferfishSkillsKubeJSPlugin.CATEGORY_UNLOCK.post(new CategoryUnlockEventJS(player, categoryId));
    }

    @Inject(method = "lockCategory", at = @At("TAIL"), remap = false)
    private void onCategoryLock(ServerPlayer player, ResourceLocation categoryId, CallbackInfo ci) {
        RepeatableSkillData.clearCategory(player, categoryId);
        RepeatableSkillSupport.clearRepeatableState(player, categoryId);
        PufferfishSkillsKubeJSPlugin.CATEGORY_LOCK.post(new CategoryLockEventJS(player, categoryId));
    }

    @Inject(method = "resetSkills", at = @At("TAIL"), remap = false)
    private void onResetSkills(ServerPlayer player, ResourceLocation categoryId, CallbackInfo ci) {
        RepeatableSkillData.clearCategory(player, categoryId);
        RepeatableSkillSupport.syncRepeatableState(player, categoryId);
    }

    @Inject(method = "eraseCategory", at = @At("TAIL"), remap = false)
    private void onEraseCategory(ServerPlayer player, ResourceLocation categoryId, CallbackInfo ci) {
        RepeatableSkillData.clearCategory(player, categoryId);
        RepeatableSkillSupport.clearRepeatableState(player, categoryId);
    }

    @Inject(method = "exportPlayerData", at = @At("TAIL"), remap = false)
    private void onExportPlayerData(ServerPlayer player, CompoundTag tag, CallbackInfo ci) {
        RepeatableSkillData.writeToTag(player, tag);
    }

    @Inject(method = "importPlayerData", at = @At("TAIL"), remap = false)
    private void onImportPlayerData(ServerPlayer player, CompoundTag tag, CallbackInfo ci) {
        RepeatableSkillData.readFromTag(player, tag);
    }

    @Inject(method = "syncPoints", at = @At("TAIL"), remap = false)
    private void onSyncPoints(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData, CallbackInfo ci) {
        if (RepeatableSkillData.getExtraSpentPoints(player, categoryConfig) > 0) {
            this.skilljs$syncRepeatablePoints(player, categoryConfig, categoryData);
        }
    }

    @Inject(method = "showCategory", at = @At("TAIL"), remap = false)
    private void onShowCategory(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData, CallbackInfo ci) {
        RepeatableSkillSupport.syncRepeatableState(player, categoryConfig);
    }
}
