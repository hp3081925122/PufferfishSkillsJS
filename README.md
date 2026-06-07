# PufferfishSkillsJS

中文 | [English](#english)

PufferfishSkillsJS 是 Pufferfish's Skills 的 KubeJS 附属模组，为整合包作者提供技能树事件、数据查询、技能操作和可重复加点能力。

## 功能特性

- 事件系统：监听分类解锁、分类锁定、技能解锁、技能锁定、重复加点等事件。
- KubeJS API：查询和操作玩家技能、分类、点数、经验和进度。
- 可重复技能：允许指定技能在已解锁后继续消耗技能点重复点击，并再次触发奖励。
- 点击上限：可为可重复技能配置最大重复次数，也可以设置为不限制。
- 客户端同步：可重复技能次数、剩余次数和技能点显示会同步到技能树界面。
- 奖励复用：重复加点会复用原技能奖励逻辑，同时提供单独的重复加点事件。

## 依赖

| 依赖 | 版本要求 |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.0+ |
| KubeJS | 2001.6.5-build.16+ |
| Pufferfish's Skills | 0.17.0+ |

## 安装

1. 安装 Minecraft Forge 1.20.1。
2. 安装 KubeJS、Pufferfish's Skills 和它们所需的前置。
3. 将 PufferfishSkillsJS 放入 `mods` 文件夹。
4. 启动游戏或服务器后，在 `kubejs/server_scripts` 中编写脚本。

## 快速开始

把一个技能设置为可重复加点：

```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true)
})
```

设置可重复加点，并限制最多点击 5 次：

```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5)
})
```

监听重复加点事件：

```javascript
PufferfishSkillsEvents.skillRepeatUnlock(event => {
    const player = event.getPlayer()
    const categoryId = event.getCategoryId()
    const skillId = event.getSkillId()
    const repeatCount = event.getRepeatCount()

    player.tell(`技能 ${categoryId}:${skillId} 已重复加点 ${repeatCount} 次`)
})
```

## 事件

### 技能解锁

```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    const player = event.getPlayer()
    const categoryId = event.getCategoryId()
    const skillId = event.getSkillId()

    console.log(`${player.getName().getString()} 解锁了 ${categoryId}:${skillId}`)
})
```

### 技能锁定

```javascript
PufferfishSkillsEvents.skillLock(event => {
    const player = event.getPlayer()
    const categoryId = event.getCategoryId()
    const skillId = event.getSkillId()

    console.log(`${player.getName().getString()} 的技能被锁定：${categoryId}:${skillId}`)
})
```

### 分类解锁和锁定

```javascript
PufferfishSkillsEvents.categoryUnlock(event => {
    event.getPlayer().tell(`已解锁分类：${event.getCategoryId()}`)
})

PufferfishSkillsEvents.categoryLock(event => {
    event.getPlayer().tell(`已锁定分类：${event.getCategoryId()}`)
})
```

### 重复加点

```javascript
PufferfishSkillsEvents.skillRepeatUnlock(event => {
    const remaining = RepeatableSkills.getRemainingRepeats(
        event.getPlayer(),
        event.getCategoryId().toString(),
        event.getSkillId()
    )

    event.getPlayer().tell(`重复加点次数：${event.getRepeatCount()}，剩余次数：${remaining}`)
})
```

## API

### PufferfishSkills

```javascript
const category = PufferfishSkills.getCategory("puffish_skills:combat")
const categories = PufferfishSkills.getCategories()

const unlocked = PufferfishSkills.isSkillUnlocked(player, "puffish_skills:combat", "strength_1")
const success = PufferfishSkills.unlockSkill(player, "puffish_skills:combat", "strength_1")

PufferfishSkills.forceUnlockSkill(player, "puffish_skills:combat", "strength_1")
PufferfishSkills.lockSkill(player, "puffish_skills:combat", "strength_1")
PufferfishSkills.repeatUnlockSkill(player, "puffish_skills:combat", "rage_stack")

PufferfishSkills.unlockCategory(player, "puffish_skills:magic")
PufferfishSkills.lockCategory(player, "puffish_skills:magic")

PufferfishSkills.addPoints(player, "puffish_skills:combat", "puffish_skills:starting", 5)
PufferfishSkills.setPoints(player, "puffish_skills:combat", "puffish_skills:starting", 10)

PufferfishSkills.openScreen(player)
PufferfishSkills.openCategoryScreen(player, "puffish_skills:combat")
```

### RepeatableSkills

```javascript
RepeatableSkills.setSkillType("puffish_skills:combat", "rage_stack", "repeatable")
RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true)
RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5)
RepeatableSkills.setRepeatLimit("puffish_skills:combat", "rage_stack", 5)

const type = RepeatableSkills.getSkillType("puffish_skills:combat", "rage_stack")
const repeatable = RepeatableSkills.isRepeatable("puffish_skills:combat", "rage_stack")
const limit = RepeatableSkills.getRepeatLimit("puffish_skills:combat", "rage_stack")

const count = RepeatableSkills.getRepeatCount(player, "puffish_skills:combat", "rage_stack")
const remaining = RepeatableSkills.getRemainingRepeats(player, "puffish_skills:combat", "rage_stack")

RepeatableSkills.repeatUnlock(player, "puffish_skills:combat", "rage_stack")
```

### SkillExperience

```javascript
const totalExp = SkillExperience.getTotalExperience(player, "puffish_skills:combat")
const level = SkillExperience.getLevel(player, "puffish_skills:combat")
const progress = SkillExperience.getProgressToNextLevel(player, "puffish_skills:combat")

SkillExperience.addExperience(player, "puffish_skills:combat", 100)
SkillExperience.setExperience(player, "puffish_skills:combat", 500)
```

## 示例

### 可重复技能

```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5)
})

PufferfishSkillsEvents.skillRepeatUnlock(event => {
    if (event.getCategoryId().toString() !== "puffish_skills:combat") return
    if (event.getSkillId() !== "rage_stack") return

    const player = event.getPlayer()
    const count = event.getRepeatCount()
    const remaining = RepeatableSkills.getRemainingRepeats(player, "puffish_skills:combat", "rage_stack")

    player.tell(`怒气层数提升到了 ${count}，剩余可点击次数：${remaining}`)
})
```

### 自定义解锁条件

```javascript
ServerEvents.tick(event => {
    event.server.getPlayers().forEach(player => {
        if (player.getStats().getValue("minecraft:killed", "minecraft:wither") <= 0) return
        if (PufferfishSkills.isSkillUnlocked(player, "puffish_skills:combat", "wither_slayer")) return

        PufferfishSkills.forceUnlockSkill(player, "puffish_skills:combat", "wither_slayer")
    })
})
```

## 开发

```powershell
.\gradlew.bat build
```

项目结构：

```text
src/main/java/com/hp/skilljs/
├── client/           客户端缓存和界面相关逻辑
├── event/            KubeJS 事件
├── integration/      KubeJS API 包装
├── mixin/            Mixin 注入
├── network/          网络同步
├── repeatable/       可重复技能逻辑
└── PufferfishSkillsJS.java
```

## 许可证

MIT License

## 致谢

- [Pufferfish's Skills](https://www.curseforge.com/minecraft/mc-mods/puffish-skills)
- [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs)

---

## English

[中文](#pufferfishskillsjs) | English

PufferfishSkillsJS is a KubeJS addon for Pufferfish's Skills. It gives modpack authors scriptable access to skill tree events, player skill data, category operations, skill points, experience, and repeatable skill clicks.

## Features

- Event system: listen for category unlocks, category locks, skill unlocks, skill locks, and repeatable skill clicks.
- KubeJS API: query and modify player skills, categories, points, experience, and progress.
- Repeatable skills: let an already unlocked skill consume skill points again and trigger its rewards again.
- Repeat limits: configure a maximum click count, or leave a repeatable skill unlimited.
- Client sync: repeat counts, remaining repeats, and effective skill points are synced to the skill screen.
- Reward reuse: repeatable clicks reuse the original skill rewards and also fire a dedicated repeat event.

## Dependencies

| Dependency | Required version |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.0+ |
| KubeJS | 2001.6.5-build.16+ |
| Pufferfish's Skills | 0.17.0+ |

## Installation

1. Install Minecraft Forge 1.20.1.
2. Install KubeJS, Pufferfish's Skills, and their required dependencies.
3. Put PufferfishSkillsJS in the `mods` folder.
4. Start the game or server, then add scripts under `kubejs/server_scripts`.

## Quick Start

Make a skill repeatable:

```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true)
})
```

Make a skill repeatable with a maximum of 5 clicks:

```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5)
})
```

Listen for repeatable clicks:

```javascript
PufferfishSkillsEvents.skillRepeatUnlock(event => {
    const player = event.getPlayer()
    const categoryId = event.getCategoryId()
    const skillId = event.getSkillId()
    const repeatCount = event.getRepeatCount()

    player.tell(`Skill ${categoryId}:${skillId} has been repeated ${repeatCount} times`)
})
```

## Events

### Skill Unlock

```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    const player = event.getPlayer()
    const categoryId = event.getCategoryId()
    const skillId = event.getSkillId()

    console.log(`${player.getName().getString()} unlocked ${categoryId}:${skillId}`)
})
```

### Skill Lock

```javascript
PufferfishSkillsEvents.skillLock(event => {
    const player = event.getPlayer()
    const categoryId = event.getCategoryId()
    const skillId = event.getSkillId()

    console.log(`${player.getName().getString()}'s skill was locked: ${categoryId}:${skillId}`)
})
```

### Category Unlock and Lock

```javascript
PufferfishSkillsEvents.categoryUnlock(event => {
    event.getPlayer().tell(`Unlocked category: ${event.getCategoryId()}`)
})

PufferfishSkillsEvents.categoryLock(event => {
    event.getPlayer().tell(`Locked category: ${event.getCategoryId()}`)
})
```

### Repeatable Skill Click

```javascript
PufferfishSkillsEvents.skillRepeatUnlock(event => {
    const remaining = RepeatableSkills.getRemainingRepeats(
        event.getPlayer(),
        event.getCategoryId().toString(),
        event.getSkillId()
    )

    event.getPlayer().tell(`Repeat count: ${event.getRepeatCount()}, remaining: ${remaining}`)
})
```

## API

### PufferfishSkills

```javascript
const category = PufferfishSkills.getCategory("puffish_skills:combat")
const categories = PufferfishSkills.getCategories()

const unlocked = PufferfishSkills.isSkillUnlocked(player, "puffish_skills:combat", "strength_1")
const success = PufferfishSkills.unlockSkill(player, "puffish_skills:combat", "strength_1")

PufferfishSkills.forceUnlockSkill(player, "puffish_skills:combat", "strength_1")
PufferfishSkills.lockSkill(player, "puffish_skills:combat", "strength_1")
PufferfishSkills.repeatUnlockSkill(player, "puffish_skills:combat", "rage_stack")

PufferfishSkills.unlockCategory(player, "puffish_skills:magic")
PufferfishSkills.lockCategory(player, "puffish_skills:magic")

PufferfishSkills.addPoints(player, "puffish_skills:combat", "puffish_skills:starting", 5)
PufferfishSkills.setPoints(player, "puffish_skills:combat", "puffish_skills:starting", 10)

PufferfishSkills.openScreen(player)
PufferfishSkills.openCategoryScreen(player, "puffish_skills:combat")
```

### RepeatableSkills

```javascript
RepeatableSkills.setSkillType("puffish_skills:combat", "rage_stack", "repeatable")
RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true)
RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5)
RepeatableSkills.setRepeatLimit("puffish_skills:combat", "rage_stack", 5)

const type = RepeatableSkills.getSkillType("puffish_skills:combat", "rage_stack")
const repeatable = RepeatableSkills.isRepeatable("puffish_skills:combat", "rage_stack")
const limit = RepeatableSkills.getRepeatLimit("puffish_skills:combat", "rage_stack")

const count = RepeatableSkills.getRepeatCount(player, "puffish_skills:combat", "rage_stack")
const remaining = RepeatableSkills.getRemainingRepeats(player, "puffish_skills:combat", "rage_stack")

RepeatableSkills.repeatUnlock(player, "puffish_skills:combat", "rage_stack")
```

### SkillExperience

```javascript
const totalExp = SkillExperience.getTotalExperience(player, "puffish_skills:combat")
const level = SkillExperience.getLevel(player, "puffish_skills:combat")
const progress = SkillExperience.getProgressToNextLevel(player, "puffish_skills:combat")

SkillExperience.addExperience(player, "puffish_skills:combat", 100)
SkillExperience.setExperience(player, "puffish_skills:combat", 500)
```

## Examples

### Repeatable Skill

```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable("puffish_skills:combat", "rage_stack", true, 5)
})

PufferfishSkillsEvents.skillRepeatUnlock(event => {
    if (event.getCategoryId().toString() !== "puffish_skills:combat") return
    if (event.getSkillId() !== "rage_stack") return

    const player = event.getPlayer()
    const count = event.getRepeatCount()
    const remaining = RepeatableSkills.getRemainingRepeats(player, "puffish_skills:combat", "rage_stack")

    player.tell(`Rage stack increased to ${count}. Remaining clicks: ${remaining}`)
})
```

### Custom Unlock Condition

```javascript
ServerEvents.tick(event => {
    event.server.getPlayers().forEach(player => {
        if (player.getStats().getValue("minecraft:killed", "minecraft:wither") <= 0) return
        if (PufferfishSkills.isSkillUnlocked(player, "puffish_skills:combat", "wither_slayer")) return

        PufferfishSkills.forceUnlockSkill(player, "puffish_skills:combat", "wither_slayer")
    })
})
```

## Development

```powershell
.\gradlew.bat build
```

Project layout:

```text
src/main/java/com/hp/skilljs/
├── client/           Client cache and UI integration
├── event/            KubeJS events
├── integration/      KubeJS API wrappers
├── mixin/            Mixin injections
├── network/          Network synchronization
├── repeatable/       Repeatable skill logic
└── PufferfishSkillsJS.java
```

## License

MIT License

## Credits

- [Pufferfish's Skills](https://www.curseforge.com/minecraft/mc-mods/puffish-skills)
- [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs)
