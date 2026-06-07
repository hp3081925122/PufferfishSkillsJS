# PufferfishSkillsJS

Pufferfish's Skills 模组的 KubeJS 附属，为整合包作者提供完整的技能树魔改支持。

## 🌟 功能特性

### 核心能力
- **事件系统**：监听技能解锁/锁定/重复点击事件，实现自定义逻辑
- **完整API**：查询和操作玩家技能数据，支持强制解锁/锁定
- **分类管理**：管理技能分类的解锁状态和点数
- **经验系统**：与技能经验系统深度集成
- **奖励系统**：触发自定义奖励更新

### 特色功能
- **可重复技能**：支持把指定技能设为可无限重复点击，或配置最大点击上限，每次消耗技能点并再次触发奖励
- **灵活配置**：通过 KubeJS 脚本灵活定制技能行为
- **服务端同步**：可重复技能数据自动同步到客户端

## 📋 依赖

| 依赖 | 版本要求 |
|------|----------|
| Minecraft | 1.20.1 |
| Forge | 47.0.0+ |
| KubeJS | 2001.6.5+ |
| Pufferfish's Skills | 0.17.0+ |

## 📥 安装

1. 确保已安装所有必需依赖
2. 将模组文件放入 `mods` 文件夹即可

## 📖 使用文档

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
let categories = PufferfishSkills.getCategories();
let category = PufferfishSkills.getCategory("puffish_skills:combat");
```

**技能操作**
```javascript
let unlocked = PufferfishSkills.isSkillUnlocked(player, "puffish_skills:combat", "strength_1");
let success = PufferfishSkills.unlockSkill(player, "puffish_skills:combat", "strength_1");
PufferfishSkills.forceUnlockSkill(player, "puffish_skills:combat", "strength_1");
PufferfishSkills.lockSkill(player, "puffish_skills:combat", "strength_1");
let repeated = PufferfishSkills.repeatUnlockSkill(player, "puffish_skills:combat", "rage_stack");
```

**分类操作**
```javascript
PufferfishSkills.unlockCategory(player, "puffish_skills:magic");
PufferfishSkills.lockCategory(player, "puffish_skills:magic");
let unlocked = PufferfishSkills.isCategoryUnlocked(player, "puffish_skills:magic");
```

**点数管理**
```javascript
let points = PufferfishSkills.getPoints(player, "puffish_skills:combat", "puffish_skills:starting");
PufferfishSkills.addPoints(player, "puffish_skills:combat", "puffish_skills:starting", 5);
PufferfishSkills.setPoints(player, "puffish_skills:combat", "puffish_skills:starting", 10);
```

**重置功能**
```javascript
PufferfishSkills.resetAll(player);
PufferfishSkills.resetCategory(player, "puffish_skills:combat");
```

#### RepeatableSkills - 可重复技能API对象

```javascript
RepeatableSkills.setSkillType("puffish_skills:combat", "rage_stack", "repeatable");
RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true);
RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5);
RepeatableSkills.setRepeatLimit("puffish_skills:combat", "rage_stack", 5);

let type = RepeatableSkills.getSkillType("puffish_skills:combat", "rage_stack");
let repeatable = RepeatableSkills.isRepeatable("puffish_skills:combat", "rage_stack");
let limit = RepeatableSkills.getRepeatLimit("puffish_skills:combat", "rage_stack");

let count = RepeatableSkills.getRepeatCount(player, "puffish_skills:combat", "rage_stack");
let remaining = RepeatableSkills.getRemainingRepeats(player, "puffish_skills:combat", "rage_stack");
RepeatableSkills.repeatUnlock(player, "puffish_skills:combat", "rage_stack");
```

**界面操作**
```javascript
PufferfishSkills.openScreen(player);
PufferfishSkills.openCategoryScreen(player, "puffish_skills:combat");
```

#### CategoryWrapper - 分类包装类

```javascript
let category = PufferfishSkills.getCategory("puffish_skills:combat");

let id = category.getId();
let skills = category.getSkills(player);
let unlockedSkills = category.getUnlockedSkills(player);
category.unlock(player);
category.lock(player);
category.addPoints(player, "puffish_skills:starting", 5);
let progress = category.getProgress(player);
```

#### SkillWrapper - 技能包装类

```javascript
let skill = category.getSkill(player, "strength_1");

let state = skill.getState();
let isUnlocked = skill.isUnlocked();
let repeatable = skill.isRepeatable();
let repeatCount = skill.getRepeatCount();
skill.unlock();
skill.forceUnlock();
skill.lock();
```

#### SkillExperience - 经验系统API

```javascript
let totalExp = SkillExperience.getTotalExperience(player, "puffish_skills:combat");
SkillExperience.addExperience(player, "puffish_skills:combat", 100);
let level = SkillExperience.getLevel(player, "puffish_skills:combat");
let progress = SkillExperience.getProgressToNextLevel(player, "puffish_skills:combat");
```

## 🎯 使用示例

### 可重复技能配置
```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5);
});

PufferfishSkillsEvents.skillRepeatUnlock(event => {
    if (event.getCategoryId().toString() != "puffish_skills:combat") return;
    if (event.getSkillId() != "rage_stack") return;

    let player = event.getPlayer();
    let count = event.getRepeatCount();
    let remaining = RepeatableSkills.getRemainingRepeats(player, "puffish_skills:combat", "rage_stack");
    player.tell(`怒气层数提升到了 ${count}，剩余可点击次数 ${remaining}`);
});
```

### 自定义技能解锁条件
```javascript
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

### 技能与游戏阶段联动
```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer();
    let categoryId = event.getCategoryId().toString();
    
    if (categoryId == "puffish_skills:mining") {
        player.stages.add('expert_miner');
    }
});
```

### 自定义奖励系统
```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer();
    let skillId = event.getSkillId();
    
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

## 🔧 开发说明

### 项目结构
```
src/main/java/com/hp/skilljs/
├── client/           # 客户端相关代码
├── event/            # KubeJS事件定义
├── integration/      # API包装类
├── mixin/            # Mixin注入
├── network/          # 网络同步
├── repeatable/       # 可重复技能核心逻辑
└── PufferfishSkillsJS.java  # 主类
```

### 编译项目
```bash
./gradlew build
```

## 📜 许可证

MIT License

## 🤝 致谢

- [Pufferfish's Skills](https://www.curseforge.com/minecraft/mc-mods/puffish-skills) - 原版技能树模组
- [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs) - JavaScript脚本支持

## 📞 联系方式

如有问题或建议，请在 CurseForge 页面留言反馈。