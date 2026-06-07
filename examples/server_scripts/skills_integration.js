// ============================================
// PufferfishSkillsJS 联动示例
// ============================================
//
// 这份示例演示“技能系统”怎么和别的玩法联动。
// 这里不写命令，改成玩家右键物品触发，比较直观。
//
// 示例逻辑：
// - 右键绿宝石：发放点数，模拟任务奖励
// - 当玩家解锁战斗系技能时，自动给游戏阶段
// - 当玩家解锁魔法系技能时，自动给另一个阶段

PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer();
    let categoryId = event.getCategoryId().toString();

    // 这个思路很适合和 Game Stages 联动
    if (categoryId == "puffish_skills:combat" && player.stages) {
        player.stages.add("warrior");
    }

    if (categoryId == "puffish_skills:magic" && player.stages) {
        player.stages.add("mage");
    }
});

ItemEvents.rightClicked(event => {
    let player = event.player;

    // 右键绿宝石：模拟任务奖励，给战斗分类加点
    if (event.item.id == "minecraft:emerald") {
        if (PufferfishSkills.hasCategory("puffish_skills:combat")) {
            let category = PufferfishSkills.getCategory("puffish_skills:combat").get();
            category.addPointsSilently(player, "puffish_skills:starting", 1);
            player.tell(Text.green("你获得了 1 个战斗技能点。"));
        }

        if (PufferfishSkills.hasCategory("puffish_skills:magic")) {
            let category = PufferfishSkills.getCategory("puffish_skills:magic").get();
            category.addPointsSilently(player, "puffish_skills:starting", 1);
            player.tell(Text.green("你获得了 1 个魔法技能点。"));
        }
    }
});