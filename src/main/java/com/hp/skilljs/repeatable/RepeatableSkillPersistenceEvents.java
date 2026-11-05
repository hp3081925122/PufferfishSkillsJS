package com.hp.skilljs.repeatable;

import com.hp.skilljs.PufferfishSkillsJSMod;
import com.hp.skilljs.unlockable.UnlockableSkillData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;

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

        CompoundTag tag = new CompoundTag();
        RepeatableSkillData.writeToTag(serverPlayer, tag);
        UnlockableSkillData.writeToTag(serverPlayer, tag);
        File file = event.getPlayerFile(FILE_SUFFIX);
        if (tag.isEmpty()) {
            deleteFile(file);
            return;
        }

        try {
            NbtIo.writeCompressed(tag, file.toPath());
            PufferfishSkillsJSMod.LOGGER.debug("Saved PufferfishSkillsJS player data player={} roots={}", serverPlayer.getGameProfile().getName(), tag.getAllKeys());
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
            UnlockableSkillData.clearAll(serverPlayer);
            PufferfishSkillsJSMod.LOGGER.debug("Cleared PufferfishSkillsJS player data without save file player={}", serverPlayer.getGameProfile().getName());
            return;
        }

        try {
            CompoundTag tag = NbtIo.readCompressed(file.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            RepeatableSkillData.readFromTag(serverPlayer, tag);
            UnlockableSkillData.readFromTag(serverPlayer, tag);
            PufferfishSkillsJSMod.LOGGER.debug("Loaded PufferfishSkillsJS player data player={} roots={}", serverPlayer.getGameProfile().getName(), tag.getAllKeys());
        } catch (IOException exception) {
            RepeatableSkillData.clearAll(serverPlayer);
            UnlockableSkillData.clearAll(serverPlayer);
            PufferfishSkillsJSMod.LOGGER.error("读取可重复技能数据失败", exception);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        // 数据按 UUID 存在静态表中，服务器关闭后释放，避免单人切换存档时残留
        RepeatableSkillData.clearAllPlayers();
        UnlockableSkillData.clearAllPlayers();
    }

    private static void deleteFile(File file) {
        if (file.isFile() && !file.delete()) {
            PufferfishSkillsJSMod.LOGGER.warn("删除空的可重复技能数据文件失败：{}", file);
        }
    }
}
