package com.hp.skilljs.mixin;

import com.hp.skilljs.client.RepeatableSkillClientCache;
import com.hp.skilljs.client.UnlockableSkillClientCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.gui.SkillsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Optional;

@Mixin(value = SkillsScreen.class, remap = false)
public abstract class SkillsScreenMixin extends Screen {
    @Shadow
    private int contentPaddingTop;

    @Shadow
    private int contentPaddingLeft;

    @Shadow
    private int contentPaddingRight;

    @Shadow
    private int contentPaddingBottom;

    protected SkillsScreenMixin() {
        super(Component.empty());
    }

    @Redirect(
        method = "lambda$drawContentWithCategory$22",
        at = @At(
            value = "INVOKE",
            target = "Lnet/puffish/skillsmod/client/data/ClientCategoryData;getSkillState(Lnet/puffish/skillsmod/client/config/skill/ClientSkillConfig;)Lnet/puffish/skillsmod/api/Skill$State;"
        )
    )
    private Skill.State onGetSkillState(ClientCategoryData categoryData, ClientSkillConfig skillConfig) {
        Skill.State state = categoryData.getSkillState(skillConfig);
        ResourceLocation categoryId = categoryData.getConfig().id();
        if (state == Skill.State.LOCKED && UnlockableSkillClientCache.isAllowed(categoryId, skillConfig.id())) {
            return Skill.State.AVAILABLE;
        }

        if (state != Skill.State.UNLOCKED) {
            return state;
        }

        RepeatableSkillClientCache.Entry entry = RepeatableSkillClientCache.get(categoryId, skillConfig.id());
        if (entry == null || !entry.repeatable()) {
            return state;
        }

        if (entry.remainingRepeats() == 0) {
            return state;
        }

        return Skill.State.AVAILABLE;
    }

    @Inject(
        method = "drawContentWithCategory",
        at = @At("TAIL")
    )
    private void onAppendRepeatableSkillTooltip(
        GuiGraphics guiGraphics,
        double mouseX,
        double mouseY,
        ClientCategoryData categoryData,
        CallbackInfo ci
    ) {
        if (this.minecraft == null) {
            return;
        }

        ClientCategoryConfig categoryConfig = categoryData.getConfig();
        if (!this.skilljs$isInsideContent(mouseX, mouseY)) {
            return;
        }

        int transformedMouseX = (int) Math.round((mouseX - categoryData.getX() - this.width / 2.0D) / categoryData.getScale());
        int transformedMouseY = (int) Math.round((mouseY - categoryData.getY() - this.height / 2.0D) / categoryData.getScale());
        Optional<ClientSkillConfig> hoveredSkill = categoryConfig.skills()
            .values()
            .stream()
            .filter(skillConfig -> categoryConfig.getDefinitionById(skillConfig.definitionId())
                .map(definition -> this.skilljs$isInsideSkill(transformedMouseX, transformedMouseY, skillConfig, definition))
                .orElse(false))
            .findFirst();
        if (hoveredSkill.isEmpty()) {
            return;
        }

        ClientSkillConfig skillConfig = hoveredSkill.get();
        ClientSkillDefinitionConfig definition = categoryConfig.definitions().get(skillConfig.definitionId());
        if (definition == null) {
            return;
        }

        RepeatableSkillClientCache.Entry entry = RepeatableSkillClientCache.get(categoryConfig.id(), skillConfig.id());
        if (entry == null || !entry.repeatable()) {
            return;
        }

        ArrayList<FormattedCharSequence> tooltipLines = new ArrayList<>();
        tooltipLines.add(definition.title().getVisualOrderText());
        tooltipLines.addAll(Tooltip.splitTooltip(this.minecraft, ComponentUtils.mergeStyles(definition.description().copy(), Style.EMPTY.applyFormat(ChatFormatting.GRAY))));
        if (Screen.hasShiftDown()) {
            tooltipLines.addAll(Tooltip.splitTooltip(this.minecraft, ComponentUtils.mergeStyles(definition.extraDescription().copy(), Style.EMPTY.applyFormat(ChatFormatting.GRAY))));
        }

        if (this.minecraft.options.advancedItemTooltips) {
            tooltipLines.add(Component.literal(skillConfig.id()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
        }

        tooltipLines.add(Component.empty().getVisualOrderText());
        tooltipLines.add(this.skilljs$tooltipLine(Component.translatable("tooltip.pufferfishskillsjs.repeatable.available"), ChatFormatting.GOLD));
        tooltipLines.add(this.skilljs$tooltipLine(Component.translatable("tooltip.pufferfishskillsjs.repeatable.count", entry.count()), ChatFormatting.AQUA));

        int remainingRepeats = entry.remainingRepeats();
        if (remainingRepeats < 0) {
            tooltipLines.add(this.skilljs$tooltipLine(Component.translatable("tooltip.pufferfishskillsjs.repeatable.remaining.unlimited"), ChatFormatting.AQUA));
        } else {
            tooltipLines.add(this.skilljs$tooltipLine(Component.translatable("tooltip.pufferfishskillsjs.repeatable.remaining.limited", remainingRepeats), ChatFormatting.AQUA));
        }

        this.setTooltipForNextRenderPass(tooltipLines);
    }

    @Unique
    private FormattedCharSequence skilljs$tooltipLine(Component component, ChatFormatting color) {
        MutableComponent styledComponent = component.copy().withStyle(color);
        return Tooltip.splitTooltip(this.minecraft, styledComponent).get(0);
    }

    @Unique
    private boolean skilljs$isInsideContent(double mouseX, double mouseY) {
        return mouseX >= this.contentPaddingLeft
            && mouseY >= this.contentPaddingTop
            && mouseX < this.width - this.contentPaddingRight
            && mouseY < this.height - this.contentPaddingBottom;
    }

    @Unique
    private boolean skilljs$isInsideSkill(
        int transformedMouseX,
        int transformedMouseY,
        ClientSkillConfig skillConfig,
        ClientSkillDefinitionConfig definition
    ) {
        int halfSize = Math.round(13.0F * definition.size());
        return transformedMouseX >= skillConfig.x() - halfSize
            && transformedMouseY >= skillConfig.y() - halfSize
            && transformedMouseX < skillConfig.x() + halfSize
            && transformedMouseY < skillConfig.y() + halfSize;
    }
}
