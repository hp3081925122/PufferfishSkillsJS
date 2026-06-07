// ============================================
// PufferfishSkillsJS API 使用示例
// ============================================
//
// 这个示例专门演示“查询类 API”怎么写。
// 不使用命令，改成玩家右键物品触发，更适合整合包里直接体验。
//
// 触发方式：
// - 右键木棍：查看所有分类总览
// - 右键纸张：查看战斗分类的详细信息
// - 右键书本：查看魔法分类的经验信息

function showAllCategories(player) {
    let summaries = PufferfishSkills.getSummaries(player);

    player.tell(Text.gold("=== 技能分类总览 ==="));
    summaries.forEach(summary => {
        player.tell(Text.gray(
            `${summary.get("id")}: ` +
            `${summary.get("unlockedCount")}/${summary.get("skillCount")} ` +
            `(${summary.get("progress").toFixed(1)}%)`
        ));
    });
}

function showCategoryDetails(player, categoryId) {
    if (!PufferfishSkills.hasCategory(categoryId)) {
        player.tell(Text.red(`找不到分类：${categoryId}`));
        return;
    }

    let category = PufferfishSkills.getCategory(categoryId).get();
    let summary = PufferfishSkills.getSummary(player, categoryId);

    player.tell(Text.gold(`=== 分类：${category.getIdString()} ===`));
    player.tell(Text.gray(`是否有经验系统：${category.hasExperience()}`));
    player.tell(Text.gray(`技能总数：${category.getSkillCount()}`));
    player.tell(Text.gray(`已解锁数量：${category.getUnlockedCount(player)}`));
    player.tell(Text.gray(`进度：${category.getProgress(player).toFixed(1)}%`));
    player.tell(Text.gray(`技能 ID：${category.getSkillIds().join(", ")}`));
    player.tell(Text.gray(`技能状态：${category.getSkillStates(player).join(", ")}`));
    player.tell(Text.gray(`已解锁技能：${category.getUnlockedSkillIds(player).join(", ")}`));
    player.tell(Text.gray(`点数来源：${category.getPointsSources(player).map(id => id.toString()).join(", ")}`));
    player.tell(Text.gray(`当前点数：${summary.get("pointsLeft")} / ${summary.get("pointsTotal")}`));
}

ItemEvents.rightClicked(event => {
    let player = event.player;
    let itemId = event.item.id;

    // 右键木棍：看所有分类总览
    if (itemId == "minecraft:stick") {
        showAllCategories(player);
        return;
    }

    // 右键纸张：看战斗分类的详细信息
    if (itemId == "minecraft:paper") {
        showCategoryDetails(player, "puffish_skills:combat");
        return;
    }

    // 右键书本：看魔法分类的经验信息
    if (itemId == "minecraft:book") {
        if (PufferfishSkills.hasCategory("puffish_skills:magic")) {
            let category = PufferfishSkills.getCategory("puffish_skills:magic").get();
            player.tell(Text.aqua("=== 魔法分类经验 ==="));
            player.tell(Text.aqua(`等级：${category.getExperienceLevel(player)}`));
            player.tell(Text.aqua(`当前经验：${category.getExperienceCurrent(player)}`));
            player.tell(Text.aqua(`升级所需：${category.getExperienceRequiredForNextLevel(player)}`));
            player.tell(Text.aqua(`进度：${category.getExperienceProgressToNextLevel(player).toFixed(1)}%`));
        }
    }
});