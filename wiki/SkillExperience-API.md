# SkillExperience API

`SkillExperience` 是专门用于管理技能经验系统的 API 对象。

## 经验系统概述

Pufferfish's Skills 支持为每个技能分类设置独立的经验和等级系统，本 API 提供了完整的操作支持。

## 查询经验数据

### 基本查询

```javascript
// 获取总经验值
let totalExp = SkillExperience.getTotalExperience(player, 'puffish_skills:combat')

// 获取当前等级
let level = SkillExperience.getLevel(player, 'puffish_skills:combat')

// 获取当前等级的经验值
let currentExp = SkillExperience.getCurrentExperience(player, 'puffish_skills:combat')

// 获取升级到下一级所需经验
let required = SkillExperience.getRequiredForNextLevel(player, 'puffish_skills:combat')

// 获取升级进度（百分比 0-100）
let progress = SkillExperience.getProgressToNextLevel(player, 'puffish_skills:combat')
```

### 等级需求查询

```javascript
// 获取指定等级所需经验
let requiredForLevel = SkillExperience.getRequiredForLevel('puffish_skills:combat', 5)

// 获取从 1 级升到指定等级总共需要的经验
let requiredTotal = SkillExperience.getRequiredTotalForLevel('puffish_skills:combat', 5)
```

## 修改经验数据

### 经验操作

```javascript
// 设置总经验值
SkillExperience.setTotalExperience(player, 'puffish_skills:combat', 1000)

// 设置总经验值的简写别名
SkillExperience.setExperience(player, 'puffish_skills:combat', 1000)

// 添加经验值
SkillExperience.addExperience(player, 'puffish_skills:combat', 100)

// 移除经验值
SkillExperience.removeExperience(player, 'puffish_skills:combat', 50)
```

### 等级操作

```javascript
// 设置等级（会自动计算所需经验）
SkillExperience.setLevel(player, 'puffish_skills:combat', 5)

// 强制升级一级（如果经验足够则不会生效）
let leveledUp = SkillExperience.levelUp(player, 'puffish_skills:combat')
```

## 实际应用示例

### 打怪获得技能经验

```javascript
EntityEvents.death(event => {
    let entity = event.getEntity()
    let killer = entity.getKillCredit()
    
    if (killer && killer.isPlayer()) {
        let player = killer
        
        // 根据怪物类型给予不同经验
        let expAmount = 0
        if (entity.getType() === 'minecraft:zombie') expAmount = 10
        if (entity.getType() === 'minecraft:skeleton') expAmount = 15
        if (entity.getType() === 'minecraft:creeper') expAmount = 20
        if (entity.getType() === 'minecraft:ender_dragon') expAmount = 500
        
        if (expAmount > 0) {
            SkillExperience.addExperience(player, 'puffish_skills:combat', expAmount)
            player.tell(`战斗经验 +${expAmount}`)
        }
    }
})
```

### 挖矿获得技能经验

```javascript
BlockEvents.broken(event => {
    let player = event.getPlayer()
    if (!player) return
    
    let block = event.getBlock()
    let expAmount = 0
    
    // 根据方块类型给予不同经验
    if (block.id === 'minecraft:coal_ore') expAmount = 5
    if (block.id === 'minecraft:iron_ore') expAmount = 10
    if (block.id === 'minecraft:gold_ore') expAmount = 15
    if (block.id === 'minecraft:diamond_ore') expAmount = 30
    
    if (expAmount > 0) {
        SkillExperience.addExperience(player, 'puffish_skills:mining', expAmount)
    }
})
```

### 达到特定等级时解锁奖励

```javascript
ServerEvents.tick(event => {
    event.server.getPlayers().forEach(player => {
        let level = SkillExperience.getLevel(player, 'puffish_skills:combat')
        
        // 达到 10 级给予特殊物品
        if (level >= 10 && !player.stages.has('combat_level_10')) {
            player.stages.add('combat_level_10')
            player.give(Item.of('minecraft:diamond_sword'))
            player.tell('达到战斗等级 10！获得钻石剑！')
        }
    })
})
```

### 等级重置

```javascript
// 提供一个重置等级的命令
ServerEvents.commandRegistry(event => {
    const { commands: Commands, arguments: Arguments } = event
    
    event.register(
        Commands.literal('resetexp')
            .requires(source => source.hasPermission(2))
            .executes(context => {
                let player = context.getSource().getPlayerOrException()
                
                // 重置战斗分类的经验
                SkillExperience.setTotalExperience(player, 'puffish_skills:combat', 0)
                player.tell('战斗经验已重置！')
                
                return 1
            })
    )
})
```

## 注意事项

- 经验系统是 Pufferfish's Skills 的可选功能，不是所有分类都配置了经验系统
- 在使用经验 API 前，建议先检查该分类是否配置了经验系统
- 经验配置在 Pufferfish's Skills 的数据包中定义

## 下一步

- [使用示例](./Examples) - 查看更多经验系统的应用示例
