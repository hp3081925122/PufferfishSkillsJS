// ============================================
// PufferfishSkillsJS 完整示例脚本
// ============================================
//
// 这份示例把“查询、摘要、批量操作、点数、经验、事件监听”串在一起。
// 同样不使用命令，全部用右键物品触发，方便你直接拿去改成自己的玩法。
//
// 触发方式示例：
// - 右键木棍：查看所有分类总览
// - 右键纸张：查看战斗分类详情
// - 右键书本：查看魔法分类经验
// - 右键金粒：批量解锁战斗分类技能
// - 右键煤炭：批量锁定战斗分类技能
// - 右键钻石：强制解锁战斗分类技能
// - 右键绿宝石：给技能点
// - 右键下界之星：重置战斗分类

function 打印分类摘要(player, summary) {
    player.tell(Text.gold(`分类：${summary.get("id")}`));
    player.tell(Text.gray(`  是否解锁：${summary.get("unlocked")}`));
    player.tell(Text.gray(`  进度：${summary.get("progress").toFixed(1)}%`));
    player.tell(Text.gray(`  技能数：${summary.get("unlockedCount")}/${summary.get("skillCount")}`));
    player.tell(Text.gray(`  点数：${summary.get("pointsLeft")} 剩余 / ${summary.get("pointsTotal")} 总计 / ${summary.get("spentPoints")} 已花费`));
    player.tell(Text.gray(`  经验等级：${summary.get("experienceLevel")}`));
    player.tell(Text.gray(`  当前经验：${summary.get("experienceCurrent")}`));
    player.tell(Text.gray(`  升级所需：${summary.get("experienceRequiredForNextLevel")}`));
}

function 打印分类详情(player, category) {
    player.tell(Text.aqua(`=== ${category.getIdString()} ===`));
    player.tell(Text.gray(`是否有经验系统：${category.hasExperience()}`));
    player.tell(Text.gray(`技能 ID：${category.getSkillIds().join(", ")}`));
    player.tell(Text.gray(`技能状态：${category.getSkillStates(player).join(", ")}`));
    player.tell(Text.gray(`已解锁技能：${category.getUnlockedSkillIds(player).join(", ")}`));
    player.tell(Text.gray(`点数来源：${category.getPointsSources(player).map(id => id.toString()).join(", ")}`));
}

ItemEvents.rightClicked(event => {
    let player = event.player;
    let itemId = event.item.id;

    if (itemId == "minecraft:stick") {
        player.tell(Text.gold("=== 所有分类总览 ==="));
        PufferfishSkills.getSummaries(player).forEach(summary => {
            player.tell(Text.gray(
                `${summary.get("id")}: ` +
                `${summary.get("unlockedCount")}/${summary.get("skillCount")} ` +
                `(${summary.get("progress").toFixed(1)}%)`
            ));
        });
        return;
    }

    if (itemId == "minecraft:paper") {
        if (PufferfishSkills.hasCategory("puffish_skills:combat")) {
            let category = PufferfishSkills.getCategory("puffish_skills:combat").get();
            打印分类详情(player, category);
            打印分类摘要(player, PufferfishSkills.getSummary(player, "puffish_skills:combat"));
        }
        return;
    }

    if (itemId == "minecraft:book") {
        if (PufferfishSkills.hasCategory("puffish_skills:magic")) {
            let category = PufferfishSkills.getCategory("puffish_skills:magic").get();
            player.tell(Text.gold("=== 魔法分类经验 ==="));
            player.tell(Text.gold(`等级：${category.getExperienceLevel(player)}`));
            player.tell(Text.gold(`当前经验：${category.getExperienceCurrent(player)}`));
            player.tell(Text.gold(`升级所需：${category.getExperienceRequiredForNextLevel(player)}`));
            player.tell(Text.gold(`进度：${category.getExperienceProgressToNextLevel(player).toFixed(1)}%`));
        }
        return;
    }

    if (itemId == "minecraft:gold_nugget") {
        if (!PufferfishSkills.hasCategory("puffish_skills:combat")) return;
        let category = PufferfishSkills.getCategory("puffish_skills:combat").get();
        let ids = category.getSkillIds();
        let affected = category.unlockSkills(player, ids);
        player.tell(Text.green(`批量解锁了 ${affected} 个战斗技能。`));
        打印分类详情(player, category);
        return;
    }

    if (itemId == "minecraft:coal") {
        if (!PufferfishSkills.hasCategory("puffish_skills:combat")) return;
        let category = PufferfishSkills.getCategory("puffish_skills:combat").get();
        let ids = category.getSkillIds();
        let affected = category.lockSkills(player, ids);
        player.tell(Text.yellow(`批量锁定了 ${affected} 个战斗技能。`));
        打印分类详情(player, category);
        return;
    }

    if (itemId == "minecraft:diamond") {
        if (!PufferfishSkills.hasCategory("puffish_skills:combat")) return;
        let category = PufferfishSkills.getCategory("puffish_skills:combat").get();
        let ids = category.getSkillIds();
        let affected = category.forceUnlockSkills(player, ids);
        player.tell(Text.aqua(`强制解锁了 ${affected} 个战斗技能。`));
        打印分类详情(player, category);
        return;
    }

    if (itemId == "minecraft:emerald") {
        if (PufferfishSkills.hasCategory("puffish_skills:combat")) {
            let category = PufferfishSkills.getCategory("puffish_skills:combat").get();
            category.addPointsSilently(player, "puffish_skills:starting", 1);
            player.tell(Text.green("你获得了 1 个战斗技能点。"));
        }
        return;
    }

    if (itemId == "minecraft:nether_star") {
        PufferfishSkills.resetCategory(player, "puffish_skills:combat");
        player.tell(Text.red("战斗分类已重置并锁定。"));
        return;
    }
});

// 事件监听：真正解锁技能时给提示
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer();
    player.tell(Text.green(`解锁技能：${event.getCategoryId()}:${event.getSkillId()}`));
});

// 事件监听：技能被锁回去时给提示
PufferfishSkillsEvents.skillLock(event => {
    let player = event.getPlayer();
    player.tell(Text.red(`锁定技能：${event.getCategoryId()}:${event.getSkillId()}`));
});

// 事件监听：分类解锁时给提示
PufferfishSkillsEvents.categoryUnlock(event => {
    let player = event.getPlayer();
    player.tell(Text.gold(`解锁分类：${event.getCategoryId()}`));
});

// 事件监听：分类锁定时给提示
PufferfishSkillsEvents.categoryLock(event => {
    let player = event.getPlayer();
    player.tell(Text.gray(`锁定分类：${event.getCategoryId()}`));
});