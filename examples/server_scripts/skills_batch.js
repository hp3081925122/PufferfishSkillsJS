// ============================================
// PufferfishSkillsJS 批量操作示例
// ============================================
//
// 这个示例演示“按分类批量解锁 / 锁定 / 强制解锁”怎么写。
// 触发方式使用右键物品：
// - 右键金粒：按“可负担状态”批量解锁
// - 右键煤炭：批量锁定
// - 右键钻石：强制解锁全部技能
// - 右键骨头：只打印当前技能状态

function getCombatCategory() {
    if (!PufferfishSkills.hasCategory("puffish_skills:combat")) {
        return null;
    }

    return PufferfishSkills.getCategory("puffish_skills:combat").get();
}

function showStates(player, category) {
    player.tell(Text.gold(`=== ${category.getIdString()} ===`));
    player.tell(Text.gray(`技能 ID：${category.getSkillIds().join(", ")}`));
    player.tell(Text.gray(`状态：${category.getSkillStates(player).join(", ")}`));
    player.tell(Text.gray(`已解锁：${category.getUnlockedSkillIds(player).join(", ")}`));
}

ItemEvents.rightClicked(event => {
    let player = event.player;
    let itemId = event.item.id;
    let category = getCombatCategory();

    if (!category) {
        player.tell(Text.red("找不到 puffish_skills:combat 分类。"));
        return;
    }

    let skillIds = category.getSkillIds();

    if (itemId == "minecraft:bone") {
        showStates(player, category);
        return;
    }

    if (itemId == "minecraft:gold_nugget") {
        let affected = category.unlockSkills(player, skillIds);
        player.tell(Text.green(`按“可负担状态”批量解锁了 ${affected} 个技能。`));
        showStates(player, category);
        return;
    }

    if (itemId == "minecraft:coal") {
        let affected = category.lockSkills(player, skillIds);
        player.tell(Text.yellow(`批量锁定了 ${affected} 个技能。`));
        showStates(player, category);
        return;
    }

    if (itemId == "minecraft:diamond") {
        let affected = category.forceUnlockSkills(player, skillIds);
        player.tell(Text.aqua(`强制解锁了 ${affected} 个技能。`));
        showStates(player, category);
        return;
    }
});