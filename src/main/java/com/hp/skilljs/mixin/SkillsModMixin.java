package com.hp.skilljs.mixin;

import com.hp.skilljs.event.SkillRepeatUnlockEventJS;
import com.hp.skilljs.integration.RepeatableSkillSupport;
import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.RepeatableSkillRewards;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import com.hp.skilljs.unlockable.UnlockableSkillData;
import com.hp.skilljs.unlockable.UnlockableSkillSupport;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;
import net.puffish.skillsmod.impl.rewards.RewardUpdateContextImpl;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.network.packets.out.PointsUpdateOutPacket;
import net.puffish.skillsmod.server.network.packets.out.SkillUpdateOutPacket;
import net.puffish.skillsmod.server.setup.ServerPlatform;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(value = SkillsMod.class, remap = false)
public abstract class SkillsModMixin {
    @Shadow
    protected abstract Optional<CategoryConfig> getCategory(ResourceLocation categoryId);

    @Shadow
    protected abstract PlayerData getPlayerData(ServerPlayer player);

    @Shadow
    @Final
    private ServerPacketSender packetSender;

    @Shadow
    @Final
    private ServerPlatform platform;

    @Shadow
    protected abstract Collection<CategoryConfig> getAllCategories();

    @Shadow
    protected abstract Optional<CategoryData> getCategoryDataIfUnlocked(ServerPlayer player, CategoryConfig categoryConfig);

    @Unique
    private void skilljs$syncRepeatablePoints(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData) {
        int baseSpent = categoryData.getSpentPoints(categoryConfig);
        int extraSpent = RepeatableSkillData.getExtraSpentPoints(player, categoryConfig);
        int effectiveSpent = baseSpent + extraSpent;
        this.packetSender.send(
            player,
            new PointsUpdateOutPacket(
                categoryConfig.id(),
                effectiveSpent,
                categoryData.getPointsTotal()
            )
        );
    }

    @Unique
    private int skilljs$getEffectiveUnlockedCount(
        ServerPlayer player,
        CategoryConfig categoryConfig,
        CategoryData categoryData,
        String definitionId
    ) {
        int count = 0;
        for (SkillConfig skillConfig : categoryConfig.skills().getAll()) {
            if (!definitionId.equals(skillConfig.definitionId()) || !categoryData.getUnlockedSkillIds().contains(skillConfig.id())) {
                continue;
            }

            if (SkillTypeRegistry.isRepeatable(categoryConfig.id(), skillConfig.id())) {
                count += Math.max(1, RepeatableSkillData.getRepeatCount(player, categoryConfig.id(), skillConfig.id()));
            } else {
                count++;
            }
        }

        return count;
    }

    @Unique
    private void skilljs$updateDefinitionRewards(
        ServerPlayer player,
        CategoryConfig categoryConfig,
        CategoryData categoryData,
        SkillDefinitionConfig definition,
        boolean action,
        Predicate<SkillRewardConfig> rewardFilter
    ) {
        int count = this.skilljs$getEffectiveUnlockedCount(player, categoryConfig, categoryData, definition.id());
        definition.rewards().stream()
            .filter(rewardFilter)
            .forEach(reward -> reward.instance().update(new RewardUpdateContextImpl(player, count, action)));
    }

    @Inject(method = "updateRewards(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Predicate;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUpdateRewards(ServerPlayer player, Predicate<SkillRewardConfig> rewardFilter, CallbackInfo ci) {
        if (this.platform.isFakePlayer(player)) {
            ci.cancel();
            return;
        }

        for (CategoryConfig categoryConfig : this.getAllCategories()) {
            this.getCategoryDataIfUnlocked(player, categoryConfig).ifPresent(categoryData -> {
                for (SkillDefinitionConfig definition : categoryConfig.definitions().getAll()) {
                    this.skilljs$updateDefinitionRewards(player, categoryConfig, categoryData, definition, false, rewardFilter);
                }
            });
        }

        ci.cancel();
    }

    @Inject(
        method = "updateRewards(Lnet/minecraft/server/level/ServerPlayer;Lnet/puffish/skillsmod/config/CategoryConfig;Lnet/puffish/skillsmod/server/data/CategoryData;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void onUpdateCategoryRewards(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData, CallbackInfo ci) {
        for (SkillDefinitionConfig definition : categoryConfig.definitions().getAll()) {
            this.skilljs$updateDefinitionRewards(player, categoryConfig, categoryData, definition, false, reward -> true);
        }

        ci.cancel();
    }

    @Inject(
        method = "updateSkillRewards",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void onUpdateSkillRewards(
        ServerPlayer player,
        CategoryConfig categoryConfig,
        CategoryData categoryData,
        SkillConfig skillConfig,
        boolean action,
        CallbackInfo ci
    ) {
        categoryConfig.definitions()
            .getById(skillConfig.definitionId())
            .ifPresent(definition -> this.skilljs$updateDefinitionRewards(player, categoryConfig, categoryData, definition, action, reward -> true));
        ci.cancel();
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
            if (UnlockableSkillSupport.canUnlockAllowedSkill(player, categoryConfig.get(), categoryData, skillConfig.get())) {
                categoryData.unlockSkill(skillId);
                this.packetSender.send(player, new SkillUpdateOutPacket(categoryId, skillId, true));
                this.skilljs$syncRepeatablePoints(player, categoryConfig.get(), categoryData);
                SkillsMod.SKILL_UNLOCK.invoker().onSkillUnlock(categoryId, skillId);
                this.onSkillUnlock(categoryData, skillId, player, categoryId, categoryConfig.get(), ci);
                this.onUpdateSkillRewards(player, categoryConfig.get(), categoryData, skillConfig.get(), true, ci);
                ci.cancel();
                return;
            }

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
        RepeatableSkillRewards.update(player, categoryConfig.get(), skillConfig.get(), repeatCount, true);
        PufferfishSkillsKubeJSPlugin.SKILL_REPEAT_UNLOCK.post(new SkillRepeatUnlockEventJS(player, categoryId, skillId, repeatCount));
        this.skilljs$syncRepeatablePoints(player, categoryConfig.get(), categoryData);
        RepeatableSkillSupport.syncRepeatableState(player, categoryConfig.get());
        ci.cancel();
    }

    @Inject(method = "lambda$getSkillState$52", at = @At("RETURN"), cancellable = true, remap = false)
    private void onGetAllowedSkillState(
        ServerPlayer player,
        CategoryConfig categoryConfig,
        SkillConfig skillConfig,
        SkillDefinitionConfig definition,
        CallbackInfoReturnable<Skill.State> cir
    ) {
        Skill.State original = cir.getReturnValue();
        if (original != Skill.State.LOCKED || !UnlockableSkillData.isAllowed(player, categoryConfig.id(), skillConfig.id())) {
            return;
        }

        if (RepeatableSkillData.getEffectivePointsLeft(player, categoryConfig, this.getPlayerData(player).getOrCreateCategoryData(categoryConfig)) < Math.max(definition.requiredPoints(), definition.cost())) {
            cir.setReturnValue(Skill.State.AVAILABLE);
            return;
        }

        if (RepeatableSkillData.getEffectiveSpentPoints(player, categoryConfig, this.getPlayerData(player).getOrCreateCategoryData(categoryConfig)) < definition.requiredSpentPoints()) {
            cir.setReturnValue(Skill.State.AVAILABLE);
            return;
        }

        cir.setReturnValue(Skill.State.AFFORDABLE);
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
        UnlockableSkillSupport.clearSkill(player, categoryId, skillId);
        if (SkillTypeRegistry.isRepeatable(categoryId, skillId)) {
            RepeatableSkillData.ensureInitialUnlock(player, categoryId, skillId);
            RepeatableSkillSupport.syncRepeatableState(player, categoryConfig);
        }
        PufferfishSkillsKubeJSPlugin.SKILL_UNLOCK.post(new SkillUnlockEventJS(player, categoryId, skillId));
    }

    @Inject(method = "lockSkill", at = @At("TAIL"), remap = false)
    private void onSkillLock(ServerPlayer player, ResourceLocation categoryId, String skillId, CallbackInfo ci) {
        UnlockableSkillSupport.clearSkill(player, categoryId, skillId);
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
        UnlockableSkillSupport.clearCategory(player, categoryId);
        RepeatableSkillSupport.clearRepeatableState(player, categoryId);
        PufferfishSkillsKubeJSPlugin.CATEGORY_LOCK.post(new CategoryLockEventJS(player, categoryId));
    }

    @Inject(method = "resetSkills", at = @At("TAIL"), remap = false)
    private void onResetSkills(ServerPlayer player, ResourceLocation categoryId, CallbackInfo ci) {
        UnlockableSkillSupport.clearCategory(player, categoryId);
        RepeatableSkillData.clearCategory(player, categoryId);
        RepeatableSkillSupport.syncRepeatableState(player, categoryId);
    }

    @Inject(method = "eraseCategory", at = @At("TAIL"), remap = false)
    private void onEraseCategory(ServerPlayer player, ResourceLocation categoryId, CallbackInfo ci) {
        UnlockableSkillSupport.clearCategory(player, categoryId);
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
        if (RepeatableSkillData.getExtraSpentPoints(player, categoryConfig) > 0) {
            this.skilljs$syncRepeatablePoints(player, categoryConfig, categoryData);
        }
        UnlockableSkillSupport.syncCategory(player, categoryConfig.id());
        RepeatableSkillSupport.syncRepeatableState(player, categoryConfig);
    }
}
