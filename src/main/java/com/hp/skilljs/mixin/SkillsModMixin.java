package com.hp.skilljs.mixin;

import com.hp.skilljs.PufferfishSkillsKubeJSPlugin;
import com.hp.skilljs.event.CategoryLockEventJS;
import com.hp.skilljs.event.CategoryUnlockEventJS;
import com.hp.skilljs.event.SkillRepeatUnlockEventJS;
import com.hp.skilljs.integration.RepeatableSkillSupport;
import com.hp.skilljs.repeatable.RepeatableSkillData;
import com.hp.skilljs.repeatable.SkillTypeRegistry;
import com.hp.skilljs.reward.RewardUpdateContexts;
import com.hp.skilljs.unlockable.UnlockableSkillData;
import com.hp.skilljs.unlockable.UnlockableSkillSupport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.server.data.PlayerData;
import net.puffish.skillsmod.server.network.ServerPacketSender;
import net.puffish.skillsmod.server.network.packets.out.PointsUpdateOutPacket;
import net.puffish.skillsmod.server.network.packets.out.SkillUpdateOutPacket;
import net.puffish.skillsmod.server.setup.ServerPlatform;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 只注入具名方法，不依赖编译器生成的 lambda 编号（上游小版本更新会改变编号导致崩溃）。
 * 技能解锁/锁定的附加逻辑改由 Pufferfish 官方事件处理，见 PufferfishSkillsJSMod。
 */
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

    @Shadow
    protected abstract void watchNewPoints(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData, boolean force, Runnable action);

    @Shadow
    protected abstract void syncPoints(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData);

    @Shadow
    protected abstract void updateSkillRewards(ServerPlayer player, CategoryConfig categoryConfig, CategoryData categoryData, SkillConfig skillConfig, boolean action);

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
        for (int rewardIndex = 0; rewardIndex < definition.rewards().size(); rewardIndex++) {
            SkillRewardConfig reward = definition.rewards().get(rewardIndex);
            if (!rewardFilter.test(reward)) {
                continue;
            }

            reward.instance().update(RewardUpdateContexts.create(player, categoryConfig.id(), definition.id(), rewardIndex, count, action));
        }
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

    @Inject(method = "updateRewards(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Predicate;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUpdateRewards(ServerPlayer player, Predicate<SkillRewardConfig> rewardFilter, CallbackInfo ci) {
        ci.cancel();
        if (this.platform.isFakePlayer(player)) {
            return;
        }

        for (CategoryConfig categoryConfig : this.getAllCategories()) {
            this.getCategoryDataIfUnlocked(player, categoryConfig).ifPresent(categoryData -> {
                for (SkillDefinitionConfig definition : categoryConfig.definitions().getAll()) {
                    this.skilljs$updateDefinitionRewards(player, categoryConfig, categoryData, definition, false, rewardFilter);
                }
            });
        }
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

    @Inject(method = "updateSkillRewards", at = @At("HEAD"), cancellable = true, remap = false)
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
    private void onTryUnlockSkill(ServerPlayer player, ResourceLocation categoryId, String skillId, boolean force, CallbackInfo ci) {
        Optional<CategoryConfig> optCategoryConfig = this.getCategory(categoryId);
        if (optCategoryConfig.isEmpty()) {
            return;
        }

        CategoryConfig categoryConfig = optCategoryConfig.get();
        Optional<SkillConfig> optSkillConfig = categoryConfig.skills().getById(skillId);
        if (optSkillConfig.isEmpty()) {
            return;
        }

        SkillConfig skillConfig = optSkillConfig.get();
        PlayerData playerData = this.getPlayerData(player);
        if (!playerData.isCategoryUnlocked(categoryConfig)) {
            return;
        }

        CategoryData categoryData = playerData.getOrCreateCategoryData(categoryConfig);
        if (!categoryData.getUnlockedSkillIds().contains(skillId)) {
            if (force) {
                return;
            }

            if (UnlockableSkillSupport.canUnlockAllowedSkill(player, categoryConfig, categoryData, skillConfig)) {
                // 与上游 tryUnlockSkill 的流程保持一致，只是跳过前置连接检查
                this.watchNewPoints(player, categoryConfig, categoryData, false, () -> {
                    categoryData.unlockSkill(skillId);
                    this.packetSender.send(player, new SkillUpdateOutPacket(categoryId, skillId, true));
                    this.syncPoints(player, categoryConfig, categoryData);
                });
                SkillsMod.SKILL_UNLOCK.invoker().onSkillUnlock(player, categoryId, skillId);
                this.updateSkillRewards(player, categoryConfig, categoryData, skillConfig, true);
                ci.cancel();
                return;
            }

            if (RepeatableSkillData.getExtraSpentPoints(player, categoryConfig) <= 0) {
                return;
            }

            SkillDefinitionConfig definition = RepeatableSkillData.getDefinition(categoryConfig, skillId);
            if (definition != null && !this.skilljs$canAffordWithRepeatablePoints(player, categoryConfig, categoryData, definition)) {
                ci.cancel();
            }

            return;
        }

        if (!SkillTypeRegistry.isRepeatable(categoryId, skillId)) {
            return;
        }

        ci.cancel();
        if (!RepeatableSkillData.canRepeatUnlock(player, categoryConfig, categoryData, skillConfig)) {
            return;
        }

        int repeatCount = RepeatableSkillData.incrementRepeatCount(player, categoryId, skillId);
        // 走统一的奖励刷新，按同一 definition 下所有节点的合计次数计算
        this.updateSkillRewards(player, categoryConfig, categoryData, skillConfig, true);
        PufferfishSkillsKubeJSPlugin.SKILL_REPEAT_UNLOCK.post(new SkillRepeatUnlockEventJS(player, categoryId, skillId, repeatCount));
        this.skilljs$syncRepeatablePoints(player, categoryConfig, categoryData);
        RepeatableSkillSupport.syncRepeatableState(player, categoryConfig);
    }

    @Inject(
        method = "getSkillState(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)Ljava/util/Optional;",
        at = @At("RETURN"),
        cancellable = true,
        remap = false
    )
    private void onGetSkillState(ServerPlayer player, ResourceLocation categoryId, String skillId, CallbackInfoReturnable<Optional<Skill.State>> cir) {
        Optional<Skill.State> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) {
            return;
        }

        Skill.State state = original.get();
        boolean allowed = state == Skill.State.LOCKED && UnlockableSkillData.isAllowed(player, categoryId, skillId);
        if (!allowed && state != Skill.State.AFFORDABLE) {
            return;
        }

        Optional<CategoryConfig> categoryConfig = this.getCategory(categoryId);
        if (categoryConfig.isEmpty()) {
            return;
        }

        SkillDefinitionConfig definition = RepeatableSkillData.getDefinition(categoryConfig.get(), skillId);
        if (definition == null) {
            return;
        }

        CategoryData categoryData = this.getPlayerData(player).getOrCreateCategoryData(categoryConfig.get());
        if (this.skilljs$canAffordWithRepeatablePoints(player, categoryConfig.get(), categoryData, definition)) {
            if (allowed) {
                cir.setReturnValue(Optional.of(Skill.State.AFFORDABLE));
            }
            return;
        }

        cir.setReturnValue(Optional.of(Skill.State.AVAILABLE));
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

    @Inject(method = "resetSkills(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("TAIL"), remap = false)
    private void onResetSkills(ServerPlayer player, ResourceLocation categoryId, CallbackInfo ci) {
        UnlockableSkillSupport.clearCategory(player, categoryId);
        RepeatableSkillData.clearCategory(player, categoryId);
        RepeatableSkillSupport.syncPoints(player, categoryId);
        RepeatableSkillSupport.syncRepeatableState(player, categoryId);
    }

    @Inject(method = "eraseCategory(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("TAIL"), remap = false)
    private void onEraseCategory(ServerPlayer player, ResourceLocation categoryId, CallbackInfo ci) {
        UnlockableSkillSupport.clearCategory(player, categoryId);
        RepeatableSkillData.clearCategory(player, categoryId);
        RepeatableSkillSupport.clearRepeatableState(player, categoryId);
    }

    @Inject(method = "exportPlayerData", at = @At("TAIL"), remap = false)
    private void onExportPlayerData(ServerPlayer player, CompoundTag tag, CallbackInfo ci) {
        RepeatableSkillData.writeToTag(player, tag);
        UnlockableSkillData.writeToTag(player, tag);
    }

    @Inject(method = "importPlayerData", at = @At("TAIL"), remap = false)
    private void onImportPlayerData(ServerPlayer player, CompoundTag tag, CallbackInfo ci) {
        RepeatableSkillData.readFromTag(player, tag);
        UnlockableSkillData.readFromTag(player, tag);
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
