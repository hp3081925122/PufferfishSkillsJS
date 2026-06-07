# 使用示例

本页收集了各种实用的 PufferfishSkillsJS 代码示例。

## 目录

- [可重复技能](#可重复技能)
- [事件监听](#事件监听)
- [进度联动](#进度联动)
- [经验系统](#经验系统)
- [自定义命令](#自定义命令)

---

## 可重复技能

### 无限叠加的属性加成

```javascript
ServerEvents.loaded(event => {
    // 设置为无限可重复技能
    RepeatableSkills.setRepeatable('puffish_skills:combat', 'rage_stack', true)
})

PufferfishSkillsEvents.skillRepeatUnlock(event => {
    if (event.getCategoryId().toString() !== 'puffish_skills:combat') return
    if (event.getSkillId() !== 'rage_stack') return
    
    let player = event.getPlayer()
    let count = event.getRepeatCount()
    
    // 每次增加 0.5 攻击力
    player.getAttribute('minecraft:generic.attack_damage')
        .addPermanentModifier({
            name: 'rage_stack_' + count,
            amount: 0.5,
            operation: 'addition'
        })
    
    player.tell(`怒气层数: ${count}，攻击力 +0.5！`)
})
```

### 有次数限制的临时效果

```javascript
ServerEvents.loaded(event => {
    // 最多使用 5 次
    RepeatableSkills.setRepeatable('puffish_skills:magic', 'mana_boost', true, 5)
})

PufferfishSkillsEvents.skillRepeatUnlock(event => {
    if (event.getCategoryId().toString() !== 'puffish_skills:magic') return
    if (event.getSkillId() !== 'mana_boost') return
    
    let player = event.getPlayer()
    let count = event.getRepeatCount()
    let remaining = RepeatableSkills.getRemainingRepeats(player, 'puffish_skills:magic', 'mana_boost')
    
    // 给予 5 分钟的效果
    player.potionEffects.add('minecraft:regeneration', 6000, 1)
    
    if (remaining > 0) {
        player.tell(`生命恢复激活！还可使用 ${remaining} 次`)
    } else {
        player.tell('生命恢复已达到最大使用次数！')
    }
})
```

### 递增奖励系统

```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable('puffish_skills:mining', 'lucky_miner', true)
})

PufferfishSkillsEvents.skillRepeatUnlock(event => {
    if (event.getCategoryId().toString() !== 'puffish_skills:mining') return
    if (event.getSkillId() !== 'lucky_miner') return
    
    let player = event.getPlayer()
    let count = event.getRepeatCount()
    
    // 奖励随次数递增
    let diamondCount = Math.min(count, 10)  // 最多 10 个
    player.give(Item.of('minecraft:diamond', diamondCount))
    
    player.tell(`幸运矿工！获得 ${diamondCount} 颗钻石！`)
})
```

---

## 事件监听

### 技能解锁时给予物品奖励

```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer()
    let skillId = event.getSkillId()
    
    // 根据技能 ID 给予不同奖励
    switch (skillId) {
        case 'health_boost_1':
            player.give(Item.of('minecraft:golden_apple'))
            player.tell('解锁生命提升！获得金苹果！')
            break
        case 'speed_boost_1':
            player.give(Item.of('minecraft:feather', 16))
            player.tell('解锁速度提升！获得羽毛！')
            break
        case 'fire_resistance':
            player.give(Item.of('minecraft:fire_charge', 8))
            player.tell('解锁防火！获得火焰弹！')
            break
    }
})
```

### 技能解锁时添加游戏阶段

```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer()
    let skillId = event.getSkillId()
    
    // 为不同技能添加不同的阶段
    let stageMap = {
        'diamond_mining': 'can_mine_diamond',
        'nether_access': 'nether_unlocked',
        'dragon_slayer': 'ender_dragon_defeated'
    }
    
    if (stageMap[skillId]) {
        player.stages.add(stageMap[skillId])
    }
})
```

### 解锁分类时的剧情对话

```javascript
PufferfishSkillsEvents.categoryUnlock(event => {
    let player = event.getPlayer()
    let categoryId = event.getCategoryId().toString()
    
    switch (categoryId) {
        case 'puffish_skills:combat':
            player.tell(Text.red('⚔️ 战斗之道已向你敞开！'))
            player.tell('通过战斗获取经验，提升你的战斗能力！')
            break
        case 'puffish_skills:magic':
            player.tell(Text.purple('✨ 魔法的秘密已揭晓！'))
            player.tell('探索奥术的奥秘，掌握强大的魔法！')
            break
        case 'puffish_skills:mining':
            player.tell(Text.gold('⛏️ 采矿之路已开启！'))
            player.tell('深入地下，寻找珍贵的矿石！')
            break
    }
})
```

---

## 进度联动

### 根据游戏进度解锁技能

```javascript
ServerEvents.tick(event => {
    event.server.getPlayers().forEach(player => {
        // 击杀凋灵后解锁技能
        if (player.getStats().getValue('minecraft:killed', 'minecraft:wither') > 0) {
            if (!PufferfishSkills.isSkillUnlocked(player, 'puffish_skills:combat', 'wither_slayer')) {
                PufferfishSkills.forceUnlockSkill(player, 'puffish_skills:combat', 'wither_slayer')
                player.tell('凋灵杀手技能已解锁！')
            }
        }
        
        // 到达下界后解锁分类
        if (player.level.dimension === 'minecraft:nether') {
            if (!PufferfishSkills.isCategoryUnlocked(player, 'puffish_skills:exploration')) {
                PufferfishSkills.unlockCategory(player, 'puffish_skills:exploration')
                player.tell('探索分类已解锁！')
            }
        }
    })
})
```

### 根据游戏阶段给予点数

```javascript
ServerEvents.tick(event => {
    event.server.getPlayers().forEach(player => {
        let sourceId = 'kubejs:stage_reward'
        
        // 检查每个阶段并给予一次性奖励
        let stages = [
            { stage: 'enter_nether', points: 5 },
            { stage: 'kill_ender_dragon', points: 10 },
            { stage: 'full_diamond_armor', points: 5 }
        ]
        
        stages.forEach(({ stage, points }) => {
            let flagStage = stage + '_points_awarded'
            if (player.stages.has(stage) && !player.stages.has(flagStage)) {
                PufferfishSkills.addPoints(player, 'puffish_skills:combat', sourceId, points)
                player.stages.add(flagStage)
                player.tell(`达到里程碑！获得 ${points} 点技能点数！`)
            }
        })
    })
})
```

---

## 经验系统

### 击杀怪物获取战斗经验

```javascript
EntityEvents.death(event => {
    let entity = event.getEntity()
    let killer = entity.getKillCredit()
    
    if (!killer || !killer.isPlayer()) return
    
    let player = killer
    
    // 根据怪物类型和难度给予经验
    let expMap = {
        'minecraft:zombie': 10,
        'minecraft:skeleton': 15,
        'minecraft:creeper': 20,
        'minecraft:spider': 12,
        'minecraft:witch': 30,
        'minecraft:blaze': 25,
        'minecraft:ender_dragon': 500
    }
    
    let exp = expMap[entity.getType().toString()]
    if (exp) {
        SkillExperience.addExperience(player, 'puffish_skills:combat', exp)
        player.tell(`战斗经验 +${exp}`)
    }
})
```

### 挖矿获取采矿经验

```javascript
BlockEvents.broken(event => {
    let player = event.getPlayer()
    if (!player) return
    
    let blockId = event.getBlock().id
    
    // 根据矿石类型给予经验
    let expMap = {
        'minecraft:coal_ore': 5,
        'minecraft:deepslate_coal_ore': 6,
        'minecraft:iron_ore': 10,
        'minecraft:deepslate_iron_ore': 12,
        'minecraft:gold_ore': 15,
        'minecraft:deepslate_gold_ore': 18,
        'minecraft:diamond_ore': 30,
        'minecraft:deepslate_diamond_ore': 35,
        'minecraft:ancient_debris': 50
    }
    
    let exp = expMap[blockId]
    if (exp) {
        SkillExperience.addExperience(player, 'puffish_skills:mining', exp)
    }
})
```

### 等级奖励系统

```javascript
ServerEvents.tick(event => {
    event.server.getPlayers().forEach(player => {
        let level = SkillExperience.getLevel(player, 'puffish_skills:combat')
        
        // 等级奖励配置
        let rewards = {
            5: { item: 'minecraft:iron_sword', message: '战斗等级 5！获得铁剑！' },
            10: { item: 'minecraft:diamond_sword', message: '战斗等级 10！获得钻石剑！' },
            20: { item: 'minecraft:netherite_sword', message: '战斗等级 20！获得下界合金剑！' }
        }
        
        // 检查是否达到奖励等级
        for (let [rewardLevel, reward] of Object.entries(rewards)) {
            let flagStage = 'combat_reward_' + rewardLevel
            if (level >= parseInt(rewardLevel) && !player.stages.has(flagStage)) {
                player.stages.add(flagStage)
                player.give(Item.of(reward.item))
                player.tell(reward.message)
            }
        }
    })
})
```

---

## 自定义命令

### 技能管理命令

```javascript
ServerEvents.commandRegistry(event => {
    const { commands: Commands, arguments: Arguments } = event
    
    // 重置技能命令
    event.register(
        Commands.literal('skills')
            .requires(source => source.hasPermission(2))
            .then(Commands.literal('reset')
                .executes(context => {
                    let player = context.getSource().getPlayerOrException()
                    PufferfishSkills.resetAll(player)
                    player.tell('所有技能已重置！')
                    return 1
                })
            )
            .then(Commands.literal('unlockall')
                .executes(context => {
                    let player = context.getSource().getPlayerOrException()
                    let categories = PufferfishSkills.getCategories()
                    categories.forEach(category => {
                        category.unlock(player)
                        category.unlockAllSkills(player)
                    })
                    player.tell('所有技能已解锁！')
                    return 1
                })
            )
            .then(Commands.literal('addpoints')
                .then(Commands.argument('category', Arguments.STRING.create(event))
                .then(Commands.argument('source', Arguments.STRING.create(event))
                .then(Commands.argument('count', Arguments.INTEGER.create(event))
                    .executes(context => {
                        let player = context.getSource().getPlayerOrException()
                        let category = Arguments.STRING.getResult(context, 'category')
                        let source = Arguments.STRING.getResult(context, 'source')
                        let count = Arguments.INTEGER.getResult(context, 'count')
                        
                        PufferfishSkills.addPoints(player, category, source, count)
                        player.tell(`已添加 ${count} 点技能点数！`)
                        return 1
                    })
                )))
            )
    )
})
```

### 可重复技能管理命令

```javascript
ServerEvents.commandRegistry(event => {
    const { commands: Commands, arguments: Arguments } = event
    
    event.register(
        Commands.literal('repeatable')
            .requires(source => source.hasPermission(2))
            .then(Commands.literal('set')
                .then(Commands.argument('category', Arguments.STRING.create(event))
                .then(Commands.argument('skill', Arguments.STRING.create(event))
                .then(Commands.argument('limit', Arguments.INTEGER.create(event))
                    .executes(context => {
                        let category = Arguments.STRING.getResult(context, 'category')
                        let skill = Arguments.STRING.getResult(context, 'skill')
                        let limit = Arguments.INTEGER.getResult(context, 'limit')
                        
                        RepeatableSkills.setRepeatable(category, skill, true, limit)
                        context.getSource().sendSuccess(Text.of(`技能 ${skill} 已设为可重复，上限 ${limit} 次`), false)
                        return 1
                    })
                )))
            )
            .then(Commands.literal('clear')
                .then(Commands.argument('category', Arguments.STRING.create(event))
                .then(Commands.argument('skill', Arguments.STRING.create(event))
                    .executes(context => {
                        let player = context.getSource().getPlayerOrException()
                        let category = Arguments.STRING.getResult(context, 'category')
                        let skill = Arguments.STRING.getResult(context, 'skill')
                        
                        RepeatableSkills.clearRepeatData(player, category, skill)
                        player.tell('重复数据已清除！')
                        return 1
                    })
                )))
    )
})
```

---

## 下一步

- [常见问题](./FAQ) - 查看常见问题解答