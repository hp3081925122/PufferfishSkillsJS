// ============================================
// PufferfishSkillsJS 事件监听示例
// ============================================
//

// 技能解锁事件：玩家真正解锁某个技能后触发
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer();
    let categoryId = event.getCategoryId().toString();
    let skillId = event.getSkillId();

    console.log(`[PufferfishSkillsJS] ${player.getName().getString()} 解锁了 ${categoryId}:${skillId}`);

    // 示例：解锁战斗系的力量技能时，奖励一把钻石剑
    if (categoryId == "puffish_skills:combat" && skillId == "strength_1") {
        player.tell(Text.gold("你解锁了力量技能，获得奖励：钻石剑"));
        player.give(Item.of("minecraft:diamond_sword"));
    }

    // 示例：如果是魔法分类，就提示一下当前已解锁数量
    if (categoryId == "puffish_skills:magic") {
        let count = PufferfishSkills.getUnlockedSkills(player, "puffish_skills:magic").length;
        player.tell(Text.aqua(`你在魔法分类中已经解锁了 ${count} 个技能。`));
    }
});

// 技能锁定事件：技能被锁回去时触发
PufferfishSkillsEvents.skillLock(event => {
    let player = event.getPlayer();
    let skillId = event.getSkillId();
    player.tell(Text.red(`你的技能 ${skillId} 被锁定了。`));
});

// 分类解锁事件：整个分类被解锁时触发
PufferfishSkillsEvents.categoryUnlock(event => {
    let player = event.getPlayer();
    player.tell(Text.gold(`你解锁了新的技能分类：${event.getCategoryId()}`));
});

// 分类锁定事件：整个分类被锁回去时触发
PufferfishSkillsEvents.categoryLock(event => {
    let player = event.getPlayer();
    player.tell(Text.gray(`你的分类已锁定：${event.getCategoryId()}`));
});