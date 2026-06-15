package com.hp.skilljs.repeatable;

import com.hp.skilljs.PufferfishSkillsJSMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.io.IOException;

public final class RepeatableSkillPersistenceEvents {
    private static final String FILE_SUFFIX = "pufferfishskillsjs";

    private RepeatableSkillPersistenceEvents() {
    }

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        CompoundTag tag = RepeatableSkillData.writeToTag(serverPlayer, new CompoundTag());
        File file = event.getPlayerFile(FILE_SUFFIX);
        if (tag.isEmpty()) {
            deleteFile(file);
            return;
        }

        try {
            NbtIo.writeCompressed(tag, file);
        } catch (IOException exception) {
            PufferfishSkillsJSMod.LOGGER.error("保存可重复技能数据失败", exception);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        File file = event.getPlayerFile(FILE_SUFFIX);
        if (!file.isFile()) {
            RepeatableSkillData.clearAll(serverPlayer);
            return;
        }

        try {
            RepeatableSkillData.readFromTag(serverPlayer, NbtIo.readCompressed(file));
        } catch (IOException exception) {
            RepeatableSkillData.clearAll(serverPlayer);
            PufferfishSkillsJSMod.LOGGER.error("读取可重复技能数据失败", exception);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();
        if (original instanceof ServerPlayer originalPlayer && player instanceof ServerPlayer serverPlayer) {
            CompoundTag tag = RepeatableSkillData.writeToTag(originalPlayer, new CompoundTag());
            RepeatableSkillData.readFromTag(serverPlayer, tag);
        }
    }

    private static void deleteFile(File file) {
        if (file.isFile() && !file.delete()) {
            PufferfishSkillsJSMod.LOGGER.warn("删除空的可重复技能数据文件失败：{}", file);
        }
    }
}
