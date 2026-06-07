# PufferfishSkillsJS

Pufferfish's Skills 模组的 KubeJS 附属，为整合包作者提供完整的技能树魔改支持。

## 功能特性

- **事件系统**：监听技能解锁/锁定事件
- **完整API**：查询和操作玩家技能数据
- **分类管理**：管理技能分类的解锁和点数
- **经验系统**：与技能经验系统集成
- **奖励系统**：触发自定义奖励更新
- **可重复技能**：支持把指定技能设为可无限重复点击，或配置最大点击上限，每次消耗技能点并再次触发奖励

## 依赖

- Minecraft 1.20.1
- Forge 47.0.0+
- KubeJS 2001.6.5+
- Pufferfish's Skills 0.17.0+

## 安装

将模组文件放入 `mods` 文件夹即可。

## 使用文档

### 事件监听

#### 技能解锁事件
```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer();
    let categoryId = event.getCategoryId();
    let skillId = event.getSkillId();
    
    console.log(`玩家 ${player.getName().getString()} 解锁了技能: ${categoryId}:${skillId}`);
});
```

#### 技能锁定事件
```javascript
PufferfishSkillsEvents.skillLock(event => {
    let player = event.getPlayer();
    let categoryId = event.getCategoryId();
    let skillId = event.getSkillId();
    
    console.log(`玩家 ${player.getName().getString()} 的技能被锁定: ${categoryId}:${skillId}`);
});
```

#### 技能重复解锁事件
```javascript
PufferfishSkillsEvents.skillRepeatUnlock(event => {
    let player = event.getPlayer();
    let categoryId = event.getCategoryId();
    let skillId = event.getSkillId();
    let repeatCount = event.getRepeatCount();

    console.log(`玩家 ${player.getName().getString()} 重复点击了技能: ${categoryId}:${skillId}，当前次数: ${repeatCount}`);
});
```

### API 参考

#### PufferfishSkills - 主API对象

**获取分类信息**
```javascript
// 获取所有分类
let categories = PufferfishSkills.getCategories();

// 获取特定分类
let category = PufferfishSkills.getCategory("puffish_skills:combat");
```

**技能操作**
```javascript
// 检查技能是否已解锁
let unlocked = PufferfishSkills.isSkillUnlocked(player, "puffish_skills:combat", "strength_1");

// 解锁技能（需要满足条件）
let success = PufferfishSkills.unlockSkill(player, "puffish_skills:combat", "strength_1");

// 强制解锁技能
PufferfishSkills.forceUnlockSkill(player, "puffish_skills:combat", "strength_1");

// 锁定技能
PufferfishSkills.lockSkill(player, "puffish_skills:combat", "strength_1");

// 对可重复技能再次点击解锁
let repeated = PufferfishSkills.repeatUnlockSkill(player, "puffish_skills:combat", "rage_stack");
```

**分类操作**
```javascript
// 解锁分类
PufferfishSkills.unlockCategory(player, "puffish_skills:magic");

// 锁定分类
PufferfishSkills.lockCategory(player, "puffish_skills:magic");

// 检查分类是否解锁
let unlocked = PufferfishSkills.isCategoryUnlocked(player, "puffish_skills:magic");
```

**点数管理**
```javascript
// 获取点数
let points = PufferfishSkills.getPoints(player, "puffish_skills:combat", "puffish_skills:starting");

// 添加点数
PufferfishSkills.addPoints(player, "puffish_skills:combat", "puffish_skills:starting", 5);

// 设置点数
PufferfishSkills.setPoints(player, "puffish_skills:combat", "puffish_skills:starting", 10);
```

**重置功能**
```javascript
// 重置所有技能
PufferfishSkills.resetAll(player);

// 重置特定分类
PufferfishSkills.resetCategory(player, "puffish_skills:combat");
```

#### RepeatableSkills - 可重复技能API对象

```javascript
// 把技能设为可重复类型
RepeatableSkills.setSkillType("puffish_skills:combat", "rage_stack", "repeatable");

// 简写写法
RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true);

// 带点击上限的写法，总点击次数最多 5 次
RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5);

// 单独设置点击上限，0 或更小表示不限次
RepeatableSkills.setRepeatLimit("puffish_skills:combat", "rage_stack", 5);

// 查询技能类型
let type = RepeatableSkills.getSkillType("puffish_skills:combat", "rage_stack");
let repeatable = RepeatableSkills.isRepeatable("puffish_skills:combat", "rage_stack");
let limit = RepeatableSkills.getRepeatLimit("puffish_skills:combat", "rage_stack");
let hasLimit = RepeatableSkills.hasRepeatLimit("puffish_skills:combat", "rage_stack");

// 查询和修改玩家重复次数
let count = RepeatableSkills.getRepeatCount(player, "puffish_skills:combat", "rage_stack");
let reachedLimit = RepeatableSkills.hasReachedRepeatLimit(player, "puffish_skills:combat", "rage_stack");
let remaining = RepeatableSkills.getRemainingRepeats(player, "puffish_skills:combat", "rage_stack");
RepeatableSkills.setRepeatCount(player, "puffish_skills:combat", "rage_stack", 3);
RepeatableSkills.clearRepeatData(player, "puffish_skills:combat", "rage_stack");

// 主动执行一次重复点击
RepeatableSkills.repeatUnlock(player, "puffish_skills:combat", "rage_stack");
```

**界面操作**
```javascript
// 打开技能树界面
PufferfishSkills.openScreen(player);

// 打开特定分类界面
PufferfishSkills.openCategoryScreen(player, "puffish_skills:combat");
```

#### CategoryWrapper - 分类包装类

```javascript
let category = PufferfishSkills.getCategory("puffish_skills:combat");

// 基本信息
let id = category.getId();           // ResourceLocation
let idString = category.getIdString(); // "puffish_skills:combat"
let count = category.getSkillCount();

// 技能操作
let skills = category.getSkills(player);
let skill = category.getSkill(player, "strength_1");
let unlockedSkills = category.getUnlockedSkills(player);
let availableSkills = category.getAvailableSkills(player);
let affordableSkills = category.getAffordableSkills(player);

// 分类操作
category.unlock(player);
category.lock(player);
let isUnlocked = category.isUnlocked(player);

// 点数操作
category.addPoints(player, "puffish_skills:starting", 5);
let points = category.getPoints(player, "puffish_skills:starting");

// 进度
category.reset(player);
category.unlockAllSkills(player);
category.lockAllSkills(player);
let unlockedCount = category.getUnlockedCount(player);
let progress = category.getProgress(player); // 百分比
```

#### SkillWrapper - 技能包装类

```javascript
let skill = category.getSkill(player, "strength_1");

// 基本信息
let id = skill.getId();
let fullId = skill.getFullId(); // "puffish_skills:combat:strength_1"
let category = skill.getCategory();

// 状态检查
let state = skill.getState(); // "LOCKED", "AVAILABLE", "AFFORDABLE", "UNLOCKED", "EXCLUDED"
let isLocked = skill.isLocked();
let isAvailable = skill.isAvailable();
let isAffordable = skill.isAffordable();
let isUnlocked = skill.isUnlocked();
let isExcluded = skill.isExcluded();
let canUnlock = skill.canUnlock();
let type = skill.getSkillType();
let repeatable = skill.isRepeatable();
let repeatCount = skill.getRepeatCount();
let repeatLimit = skill.getRepeatLimit();
let hasRepeatLimit = skill.hasRepeatLimit();
let reachedRepeatLimit = skill.hasReachedRepeatLimit();
let remainingRepeats = skill.getRemainingRepeats();

// 操作
skill.unlock();      // 正常解锁
skill.forceUnlock(); // 强制解锁
skill.lock();        // 锁定
```

### 可重复技能示例

```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5);
});

PufferfishSkillsEvents.skillRepeatUnlock(event => {
    if (event.getCategoryId().toString() != "puffish_skills:combat") {
        return;
    }

    if (event.getSkillId() != "rage_stack") {
        return;
    }

    let player = event.getPlayer();
    let count = event.getRepeatCount();
    let remaining = RepeatableSkills.getRemainingRepeats(player, "puffish_skills:combat", "rage_stack");
    player.tell(`怒气层数提升到了 ${count}，剩余可点击次数 ${remaining}`);
});
```

#### SkillExperience - 经验系统API

```javascript
// 经验值操作
let totalExp = SkillExperience.getTotalExperience(player, "puffish_skills:combat");
SkillExperience.setTotalExperience(player, "puffish_skills:combat", 1000);
SkillExperience.addExperience(player, "puffish_skills:combat", 100);
SkillExperience.removeExperience(player, "puffish_skills:combat", 50);

// 等级操作
let level = SkillExperience.getLevel(player, "puffish_skills:combat");
let currentExp = SkillExperience.getCurrentExperience(player, "puffish_skills:combat");
let required = SkillExperience.getRequiredForNextLevel(player, "puffish_skills:combat");
let progress = SkillExperience.getProgressToNextLevel(player, "puffish_skills:combat");

SkillExperience.setLevel(player, "puffish_skills:combat", 5);
SkillExperience.levelUp(player, "puffish_skills:combat");
```

## 示例脚本

查看 `examples/` 目录获取更多使用示例：

- `skills_events.js` - 事件监听示例
- `skills_api.js` - API使用示例
- `skills_integration.js` - 整合包魔改示例

## 整合包开发建议

### 1. 自定义技能解锁条件
```javascript
// 根据游戏进度解锁技能
ServerEvents.tick(event => {
    event.server.getPlayers().forEach(player => {
        if (player.getStats().getValue("minecraft:killed", "minecraft:wither") > 0) {
            if (!PufferfishSkills.isSkillUnlocked(player, "puffish_skills:combat", "wither_slayer")) {
                PufferfishSkills.forceUnlockSkill(player, "puffish_skills:combat", "wither_slayer");
            }
        }
    });
});
```

### 2. 技能与游戏阶段联动
```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer();
    let categoryId = event.getCategoryId().toString();
    
    if (categoryId == "puffish_skills:mining") {
        player.stages.add('expert_miner');
    }
});
```

### 3. 自定义奖励系统
```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer();
    let skillId = event.getSkillId();
    
    // 根据技能ID给予不同奖励
    switch(skillId) {
        case "health_boost_1":
            player.getAttribute("minecraft:generic.max_health").addPermanentModifier({
                name: "skill_bonus",
                amount: 2,
                operation: "addition"
            });
            break;
        case "speed_boost_1":
            player.getAttribute("minecraft:generic.movement_speed").addPermanentModifier({
                name: "skill_bonus",
                amount: 0.05,
                operation: "multiply_base"
            });
            break;
    }
});
```

## 许可证

MIT License

## 致谢

- [Pufferfish's Skills](https://www.curseforge.com/minecraft/mc-mods/puffish-skills) - 原版技能树模组
- [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs) - JavaScript脚本支持
